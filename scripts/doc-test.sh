#!/bin/bash
# Document center module self-test (ASCII-only)
BASE=http://localhost:8080/api
SMP=/tmp/sample-doc.txt
printf 'hello doc version 1\n' > $SMP
pass=0; fail=0
check() {
  if echo "$3" | grep -q "$2"; then echo "PASS $1"; pass=$((pass+1)); else echo "FAIL $1 -> $3"; fail=$((fail+1)); fi
}
tok() { echo "$1" | sed 's/.*"token":"//; s/".*//'; }

A=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"123456"}')
AT=$(tok "$A"); AA="Authorization: Bearer $AT"
L=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"lina","password":"123456"}')
LT=$(tok "$L"); LA="Authorization: Bearer $LT"

# 1. folder tree (root folder present), admin sees canEdit
R=$(curl -s $BASE/doc/folders/tree -H "$AA")
check "doc-tree-root" '"folderId":1' "$R"
check "doc-tree-admin-edit" '"canEdit":true' "$R"

# 2. create subfolder
R=$(curl -s -X POST "$BASE/doc/folders?parentId=1&name=Manual" -H "$AA")
check "folder-create" '"code":200' "$R"
FID=$(echo "$R" | grep -o '"data":[0-9]*' | cut -d: -f2)

# 3. upload file into folder
R=$(curl -s -X POST "$BASE/doc/folders/$FID/files/upload" -H "$AA" -F "file=@$SMP" -F "overwrite=false")
check "file-upload" '"code":200' "$R"
FLE=$(echo "$R" | grep -o '"data":[0-9]*' | cut -d: -f2)

# 4. duplicate upload without overwrite rejected (same filename)
R=$(curl -s -X POST "$BASE/doc/folders/$FID/files/upload" -H "$AA" -F "file=@$SMP" -F "overwrite=false")
check "dup-upload-rejected" '"code":400' "$R"

# 5. overwrite upload with same filename -> version created
printf 'hello doc version 2 updated content\n' > $SMP
R=$(curl -s -X POST "$BASE/doc/folders/$FID/files/upload" -H "$AA" -F "file=@$SMP" -F "overwrite=true")
check "overwrite-upload" '"code":200' "$R"
R=$(curl -s "$BASE/doc/files/$FLE/versions" -H "$AA")
check "versions-kept" '"versionNo":1' "$R"
check "versions-current" '"versionNo":2' "$R"

# 6. rename file
R=$(curl -s -X PUT "$BASE/doc/files/$FLE/rename" -H "$AA" --data-urlencode "name=RenamedDoc.txt")
check "file-rename" '"code":200' "$R"

# 7. download file (attachment disposition)
R=$(curl -s -D - -o /dev/null "$BASE/doc/files/$FLE/download" -H "$AA" 2>/dev/null | grep -ic "Content-Disposition")
check "file-download" '1' "CONTENT-DISPOSITION-COUNT=$R"

# 8. list files in folder
R=$(curl -s "$BASE/doc/folders/$FID/files" -H "$AA")
check "folder-files" 'RenamedDoc' "$R"

# 9. search file by name
R=$(curl -s "$BASE/doc/files/search?name=Renamed" -H "$AA")
check "file-search" '"code":200' "$R"

# 10. lina can read root (default read all)
R=$(curl -s "$BASE/doc/folders/1/files" -H "$LA")
check "emp-read-root" '"code":200' "$R"

# 11. lina cannot edit root (admin-only default) -> 403
R=$(curl -s -X POST "$BASE/doc/folders?parentId=1&name=EmpFolder" -H "$LA")
check "emp-edit-forbidden" '"code":403' "$R"

# 12. grant EDIT to dept 7 (lina) -> lina can now create folder under root
R=$(curl -s -X POST "$BASE/doc/folders/1/perms" -H "$AA" -H 'Content-Type: application/json' -d '{"permType":"EDIT","grantType":"DEPT","grantId":7}')
check "grant-perm" '"code":200' "$R"
R=$(curl -s -X POST "$BASE/doc/folders?parentId=1&name=EmpFolder" -H "$LA")
check "emp-edit-after-grant" '"code":200' "$R"
EFID=$(echo "$R" | grep -o '"data":[0-9]*' | cut -d: -f2)

# 13. revoke perm -> lina forbidden again
R=$(curl -s -X DELETE "$BASE/doc/folders/1/perms" -H "$AA" -H 'Content-Type: application/json' -d '{"permType":"EDIT","grantType":"DEPT","grantId":7}')
check "revoke-perm" '"code":200' "$R"
R=$(curl -s -X POST "$BASE/doc/folders?parentId=1&name=EmpFolder2" -H "$LA")
check "emp-edit-after-revoke" '"code":403' "$R"

# 14. delete non-empty folder blocked
R=$(curl -s -X DELETE "$BASE/doc/folders/$FID" -H "$AA")
check "delete-nonempty-folder-blocked" '"code":400' "$R"

# 15. cleanup: delete file, empty folder, then delete folder
curl -s -X DELETE "$BASE/doc/files/$FLE" -H "$AA" >/dev/null
R=$(curl -s -X DELETE "$BASE/doc/folders/$EFID" -H "$AA")
check "delete-empty-folder" '"code":200' "$R"

echo "----------------------------------------"
echo "DOC PASS=$pass FAIL=$fail"