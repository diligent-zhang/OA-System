#!/bin/bash
# OA backend API self-test (ASCII-only payloads to avoid Windows console encoding issues)
BASE=http://localhost:8080/api
pass=0; fail=0
check() {
  if echo "$3" | grep -q "$2"; then echo "PASS $1"; pass=$((pass+1)); else echo "FAIL $1 -> $3"; fail=$((fail+1)); fi
}
tok() { echo "$1" | sed 's/.*"token":"//; s/".*//'; }

# 1. admin login
R=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"123456"}')
check "admin-login" '"code":200' "$R"
TOKEN=$(tok "$R"); AUTH="Authorization: Bearer $TOKEN"

# 2. bad password
R=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"bad"}')
check "wrong-pwd-rejected" '"code":400' "$R"

# 3. employee token -> admin api 403
R=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"lina","password":"123456"}')
EMP=$(tok "$R")
R=$(curl -s $BASE/admin/employees -H "Authorization: Bearer $EMP")
check "employee-forbidden-403" '"code":403' "$R"

# 4. no token 401
R=$(curl -s $BASE/admin/employees)
check "no-token-401" '"code":401' "$R"

# 5. employee paging + filters
R=$(curl -s "$BASE/admin/employees?pageNum=1&pageSize=5" -H "$AUTH")
check "employee-page" '"total":' "$R"
R=$(curl -s "$BASE/admin/employees?empNo=EMP2026&deptId=6&status=1" -H "$AUTH")
check "employee-multifilter" '"code":200' "$R"

# 6. dept CRUD + guards
R=$(curl -s $BASE/depts/tree -H "$AUTH")
check "dept-tree" '"code":200' "$R"
R=$(curl -s -X POST $BASE/admin/depts -H "$AUTH" -H 'Content-Type: application/json' -d '{"deptName":"DeptDelTest","parentId":1,"sortNo":9}')
check "dept-add" '"code":200' "$R"
R=$(curl -s "$BASE/admin/depts?keyword=DeptDelTest" -H "$AUTH")
DID=$(echo "$R" | grep -o '"deptId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X PUT $BASE/admin/depts/$DID -H "$AUTH" -H 'Content-Type: application/json' -d '{"deptName":"DeptDelTest2","parentId":1,"sortNo":8}')
check "dept-update" '"code":200' "$R"
R=$(curl -s -X DELETE $BASE/admin/depts/$DID -H "$AUTH")
check "dept-delete-empty" '"code":200' "$R"
R=$(curl -s -X DELETE $BASE/admin/depts/6 -H "$AUTH")
check "dept-delete-nonempty-blocked" '"code":400' "$R"

# 7. notice lifecycle
R=$(curl -s "$BASE/notices?pageNum=1&pageSize=5" -H "Authorization: Bearer $EMP")
check "employee-notice-list" '"code":200' "$R"
R=$(curl -s -X POST $BASE/admin/notices -H "$AUTH" -H 'Content-Type: application/json' -d '{"title":"CITestNotice","content":"auto test content","type":1,"topFlag":1}')
check "notice-publish" '"code":200' "$R"
R=$(curl -s "$BASE/admin/notices?title=CITestNotice" -H "$AUTH")
check "notice-search-hit" 'auto test content' "$R"
NID=$(echo "$R" | grep -o '"noticeId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X PUT $BASE/admin/notices/$NID/toggle -H "$AUTH")
check "notice-offline" '"code":200' "$R"
R=$(curl -s $BASE/notices/$NID -H "Authorization: Bearer $EMP")
check "offline-hidden-from-employee" '"code":403' "$R"
R=$(curl -s $BASE/notices/$NID -H "$AUTH")
check "offline-visible-to-admin" '"code":200' "$R"
R=$(curl -s -X DELETE $BASE/admin/notices/$NID -H "$AUTH")
check "notice-delete" '"code":200' "$R"

# 8. employee full lifecycle
R=$(curl -s -X POST $BASE/admin/employees -H "$AUTH" -H 'Content-Type: application/json' -d '{"username":"citest01","name":"CITester","gender":1,"phone":"13911112222","email":"ci@test.com","deptId":6,"hireDate":"2026-09-01","status":1}')
check "employee-add" '"code":200' "$R"
R=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"citest01","password":"123456"}')
check "new-emp-default-pwd-login" '"code":200' "$R"
EID=$(curl -s "$BASE/admin/employees?name=CITester" -H "$AUTH" | grep -o '"empId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X PUT "$BASE/admin/employees/$EID/status?status=0" -H "$AUTH")
check "account-disable" '"code":200' "$R"
R=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"citest01","password":"123456"}')
check "disabled-login-blocked" '"code":403' "$R"
R=$(curl -s -X PUT "$BASE/admin/employees/$EID/status?status=1" -H "$AUTH")
check "account-enable" '"code":200' "$R"
R=$(curl -s -X PUT $BASE/admin/employees/$EID/password/reset -H "$AUTH")
check "reset-password" '"code":200' "$R"
R=$(curl -s -X PUT $BASE/admin/employees/$EID -H "$AUTH" -H 'Content-Type: application/json' -d '{"username":"citest01","name":"CITester2","gender":1,"phone":"13911112222","email":"ci@test.com","deptId":7,"hireDate":"2026-09-01","status":1}')
check "employee-update" '"code":200' "$R"
R=$(curl -s -X DELETE $BASE/admin/employees/$EID -H "$AUTH")
check "employee-delete" '"code":200' "$R"

# 9. batch delete
curl -s -X POST $BASE/admin/employees -H "$AUTH" -H 'Content-Type: application/json' -d '{"username":"cibatch1","name":"BatchA","gender":1,"phone":"13911112223","email":"b1@test.com","deptId":6,"hireDate":"2026-09-01","status":1}' >/dev/null
curl -s -X POST $BASE/admin/employees -H "$AUTH" -H 'Content-Type: application/json' -d '{"username":"cibatch2","name":"BatchB","gender":1,"phone":"13911112224","email":"b2@test.com","deptId":6,"hireDate":"2026-09-01","status":1}' >/dev/null
IDS=$(curl -s "$BASE/admin/employees?name=Batch&pageSize=10" -H "$AUTH" | grep -o '"empId":[0-9]*' | cut -d: -f2 | paste -sd, -)
R=$(curl -s -X DELETE $BASE/admin/employees/batch -H "$AUTH" -H 'Content-Type: application/json' -d "[$IDS]")
check "employee-batch-delete" '"code":200' "$R"

# 10. profile + password
R=$(curl -s $BASE/user/profile -H "Authorization: Bearer $EMP")
check "my-profile" '"empNo"' "$R"
R=$(curl -s -X PUT $BASE/user/password -H "Authorization: Bearer $EMP" -H 'Content-Type: application/json' -d '{"oldPassword":"wrongpwd","newPassword":"abc123456"}')
check "old-pwd-verified" '"code":400' "$R"
R=$(curl -s -X PUT $BASE/user/profile -H "Authorization: Bearer $EMP" -H 'Content-Type: application/json' -d '{"phone":"13600001111","email":"lina-tmp@tianyu.com"}')
check "profile-update" '"code":200' "$R"
curl -s -X PUT $BASE/user/profile -H "Authorization: Bearer $EMP" -H 'Content-Type: application/json' -d '{"phone":"13800000002","email":"lina@tianyu.com"}' >/dev/null

# 11. dashboard
R=$(curl -s $BASE/admin/dashboard/stats -H "$AUTH")
check "dashboard-stats" '"empTotal":9' "$R"
R=$(curl -s $BASE/admin/dashboard/dept-emp -H "$AUTH")
check "dept-emp-chart" '"value"' "$R"
R=$(curl -s $BASE/admin/dashboard/latest-notices -H "$AUTH")
check "latest-notices" '"code":200' "$R"

# 12. register flow (dynamic username keeps the suite idempotent)
RU="reg$RANDOM"
R=$(curl -s -X POST $BASE/auth/register -H 'Content-Type: application/json' -d "{\"username\":\"$RU\",\"password\":\"test1234\",\"name\":\"RegTester\",\"gender\":0,\"phone\":\"13722223333\",\"email\":\"reg@test.com\",\"deptId\":7}")
check "register" '"code":200' "$R"
R=$(curl -s "$BASE/auth/username-exists?username=$RU")
check "username-exists-true" '"data":true' "$R"
R=$(curl -s -X POST $BASE/auth/register -H 'Content-Type: application/json' -d "{\"username\":\"$RU\",\"password\":\"test1234\",\"name\":\"RegTester\",\"gender\":0,\"phone\":\"13722223333\",\"email\":\"reg@test.com\"}")
check "dup-register-rejected" '"code":400' "$R"
R=$(curl -s -X POST $BASE/auth/register -H 'Content-Type: application/json' -d '{"username":"ab","password":"123456","name":"x","gender":1,"phone":"123","email":"x"}')
check "param-validation-400" '"code":400' "$R"
R=$(curl -s -X POST $BASE/auth/register -H 'Content-Type: application/json' -d '{"username":"lina","password":"test1234","name":"Taken","gender":1,"phone":"13722223333","email":"a@b.com"}')
check "occupied-username-rejected" '"code":400' "$R"
R=$(curl -s -X POST $BASE/auth/register -H 'Content-Type: application/json' -d '{"username":"regtest02","password":"test1234","name":"ParentDept","gender":1,"phone":"13722223334","email":"reg2@test.com","deptId":1}')
check "parent-dept-rejected" '"code":400' "$R"
R=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d "{\"username\":\"$RU\",\"password\":\"test1234\"}")
check "registered-account-login" '"role":"EMPLOYEE"' "$R"
REID=$(echo "$R" | grep -o '"empId":[0-9]*' | cut -d: -f2)
R=$(curl -s -X DELETE $BASE/admin/employees/$REID -H "$AUTH")
check "cleanup-registered" '"code":200' "$R"

# 13. CORS preflight
R=$(curl -s -X OPTIONS $BASE/auth/login -H 'Origin: http://localhost:5173' -H 'Access-Control-Request-Method: POST' -H 'Access-Control-Request-Headers: authorization,content-type' -D - -o /dev/null)
check "cors-preflight" 'Access-Control-Allow-Origin' "$R"

echo "----------------------------------------"
echo "PASS=$pass FAIL=$fail"
