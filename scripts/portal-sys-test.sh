#!/bin/bash
# Portal / message / notice attachments / sys-admin self-test (ASCII-only)
BASE=http://localhost:8080/api
pass=0; fail=0
check() {
  if echo "$3" | grep -q "$2"; then echo "PASS $1"; pass=$((pass+1)); else echo "FAIL $1 -> $3"; fail=$((fail+1)); fi
}
tok() { echo "$1" | sed 's/.*"token":"//; s/".*//'; }

A=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"123456"}')
AT=$(tok "$A"); AA="Authorization: Bearer $AT"
L=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"lina","password":"123456"}')
LT=$(tok "$L"); LA="Authorization: Bearer $LT"

# 1. portal summary
R=$(curl -s $BASE/portal/summary -H "$LA")
check "portal-summary" '"todoCount":' "$R"
check "portal-processing" '"processingCount":' "$R"

# 2. submit a flow so messages + todo get created, then resume
R=$(curl -s -X POST $BASE/flow/instances -H "$LA" -H 'Content-Type: application/json' \
  -d '{"defId":1,"title":"PortalMsg test","formData":{"leave_type":"sick","begin_date":"2026-09-28","end_date":"2026-09-28","days":1,"reason":"portal msg"}}')
check "flow-submit-for-msg" '"code":200' "$R"

# 3. admin received a message (审批提醒)
R=$(curl -s "$BASE/messages?pageNum=1&pageSize=10" -H "$AA")
check "admin-messages-list" '"msgType":1' "$R"
check "message-bizlink-flow-detail" '/flow/detail/' "$R"
MID=$(echo "$R" | grep -o '"msgId":[0-9]*' | head -1 | cut -d: -f2)

# 4. unread count > 0
R=$(curl -s $BASE/messages/unread-count -H "$AA")
check "unread-count" '"data":' "$R"

# 5. mark one read
R=$(curl -s -X PUT $BASE/messages/$MID/read -H "$AA")
check "mark-read" '"code":200' "$R"

# 6. admin op-logs (LOGIN type exists because login wrote a log)
R=$(curl -s "$BASE/admin/op-logs?opType=LOGIN&pageSize=10" -H "$AA")
check "op-logs-page" '"code":200' "$R"
check "op-logs-login" '"LOGIN"' "$R"

# 7. my op-logs (lina has LOGIN + FLOW)
R=$(curl -s "$BASE/user/op-logs?pageSize=10" -H "$LA")
check "my-op-logs" '"code":200' "$R"

# 8. sys-config list
R=$(curl -s $BASE/admin/sys-config -H "$AA")
check "sys-config" '"message_push_enabled"' "$R"

# 9. update config value (attachment size)
R=$(curl -s -X PUT $BASE/admin/sys-config -H "$AA" -H 'Content-Type: application/json' -d '{"configKey":"attachment_max_size","configValue":"25"}')
check "config-update" '"code":200' "$R"
R=$(curl -s $BASE/admin/sys-config -H "$AA")
check "config-value-persist" '"25"' "$R"
# restore
curl -s -X PUT $BASE/admin/sys-config -H "$AA" -H 'Content-Type: application/json' -d '{"configKey":"attachment_max_size","configValue":"20"}' >/dev/null

# 10. notice with attachment + read stats
echo "attachment content for notice" > /tmp/notice-att.txt
R=$(curl -s -X POST $BASE/file/upload -H "$AA" -F "file=@/tmp/notice-att.txt" -F "bizType=notice" -F "bizId=0")
check "attach-upload" '"code":200' "$R"
FID=$(echo "$R" | grep -o '"fileId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X POST $BASE/admin/notices -H "$AA" -H 'Content-Type: application/json' \
  -d "{\"title\":\"PortalNoticeTest\",\"content\":\"notice with attachment\",\"type\":1,\"topFlag\":0,\"attachmentFileIds\":[$FID]}")
check "notice-with-attach" '"code":200' "$R"
NID=$(curl -s "$BASE/admin/notices?title=PortalNoticeTest" -H "$AA" | grep -o '"noticeId":[0-9]*' | head -1 | cut -d: -f2)

# 11. employee reads notice detail (生成已读记录)
R=$(curl -s $BASE/notices/$NID -H "$LA")
check "employee-read-notice" '"PortalNoticeTest"' "$R"

# 12. admin read stats
R=$(curl -s $BASE/admin/notices/$NID/read-stats -H "$AA")
check "read-stats" '"totalUsers":' "$R"
check "read-stats-readCount" '"readCount":' "$R"

# 13. avatar upload
echo "fake png" > /tmp/avatar-test.png
R=$(curl -s -X POST $BASE/user/avatar -H "$LA" -F "file=@/tmp/avatar-test.png")
check "avatar-upload" '"code":200' "$R"
R=$(curl -s $BASE/user/profile -H "$LA")
check "avatar-in-profile" '"avatarUrl"' "$R"

# 14. role management: promote a temp employee then demote (use existing zhangwei as DP Admin then revert? avoid flakiness: use dept admin add + promote test employee)
R=$(curl -s -X POST $BASE/admin/employees -H "$AA" -H 'Content-Type: application/json' \
  -d '{"username":"roleTest01","name":"RoleTester","gender":1,"phone":"13933330001","email":"rt@test.com","deptId":6,"hireDate":"2026-09-01","status":1}')
RUID=$(curl -s "$BASE/admin/employees?name=RoleTester" -H "$AA" | grep -o '"userId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X PUT $BASE/admin/users/$RUID/role -H "$AA" -H 'Content-Type: application/json' -d '{"role":"DEPT_ADMIN"}')
check "promote-dept-admin" '"code":200' "$R"
R=$(curl -s "$BASE/admin/employees?name=RoleTester" -H "$AA")
check "role-promoted-to-deptadmin" '"DEPT_ADMIN"' "$R"
REID=$(echo "$R" | grep -o '"empId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X PUT $BASE/admin/users/$RUID/role -H "$AA" -H 'Content-Type: application/json' -d '{"role":"EMPLOYEE"}')
check "demote-role" '"code":200' "$R"
R=$(curl -s "$BASE/admin/employees?name=RoleTester" -H "$AA")
check "role-demoted-to-employee" '"EMPLOYEE"' "$R"
R=$(curl -s -X DELETE $BASE/admin/employees/$REID -H "$AA")
check "cleanup-role-tester" '"code":200' "$R"

# cleanup notice
R=$(curl -s -X DELETE $BASE/admin/notices/$NID -H "$AA")
check "cleanup-notice" '"code":200' "$R"

echo "----------------------------------------"
echo "PORTAL/SYS PASS=$pass FAIL=$fail"