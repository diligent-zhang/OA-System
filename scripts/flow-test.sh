#!/bin/bash
# Flow approval module self-test (ASCII-only payloads to avoid Windows console encoding)
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

# 1. employee can list enabled flow defs
R=$(curl -s $BASE/flow/defs -H "$LA")
check "defs-enabled-list" '"defId":1' "$R"

# 2. employee submits leave (days=1 -> dept leader fallback admin, no branch)
R=$(curl -s -X POST $BASE/flow/instances -H "$LA" -H 'Content-Type: application/json' \
  -d '{"defId":1,"title":"UnitTest leave","formData":{"leave_type":"sick","begin_date":"2026-09-21","end_date":"2026-09-21","days":1,"reason":"unit test asc"}}')
check "leave-submit" '"code":200' "$R"
IID=$(echo "$R" | grep -o '"data":[0-9]*' | cut -d: -f2)

# 3. admin has a todo for the new instance
R=$(curl -s "$BASE/flow/instances/my-todo?pageSize=10" -H "$AA")
check "admin-todo-hit" "UnitTest leave" "$R"
TID=$(echo "$R" | grep -o '"taskId":[0-9]*' | head -1 | cut -d: -f2)

# 4. lina cannot operate admin's task (403)
R=$(curl -s -X PUT $BASE/flow/tasks/$TID/approve -H "$LA" -H 'Content-Type: application/json' -d '{"comment":"nope"}')
check "cross-user-task-forbidden" '"code":403' "$R"

# 5. admin approves -> flow APPROVED (single node)
R=$(curl -s -X PUT $BASE/flow/tasks/$TID/approve -H "$AA" -H 'Content-Type: application/json' -d '{"comment":"ok"}')
check "approve-ok" '"code":200' "$R"
R=$(curl -s "$BASE/flow/instances/mine?pageSize=5" -H "$LA")
check "instance-approved" '"APPROVED"' "$R"

# 6. branch: leave with days=5 -> second approval round (人事负责人 ROLE=ADMIN)
R=$(curl -s -X POST $BASE/flow/instances -H "$LA" -H 'Content-Type: application/json' \
  -d '{"defId":1,"title":"Branch leave 5days","formData":{"leave_type":"sick","begin_date":"2026-10-09","end_date":"2026-10-13","days":5,"reason":"branch test"}}')
check "branch-submit" '"code":200' "$R"
R=$(curl -s "$BASE/flow/instances/my-todo?pageSize=10" -H "$AA")
check "branch-first-todo" "Branch leave 5days" "$R"
TID1=$(echo "$R" | grep -o '"taskId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X PUT $BASE/flow/tasks/$TID1/approve -H "$AA" -H 'Content-Type: application/json' -d '{"comment":"round1"}')
check "branch-first-approve" '"code":200' "$R"
R=$(curl -s "$BASE/flow/instances/my-todo?pageSize=10" -H "$AA")
check "branch-second-todo" "Branch leave 5days" "$R"
TID2=$(echo "$R" | grep -o '"taskId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X PUT $BASE/flow/tasks/$TID2/approve -H "$AA" -H 'Content-Type: application/json' -d '{"comment":"round2"}')
check "branch-second-approve" '"code":200' "$R"
R=$(curl -s "$BASE/flow/instances/mine?pageSize=10" -H "$LA")
check "branch-instance-approved" '"APPROVED"' "$R"

# 7. transfer: lina submits overtime -> admin transfers to zhangwei
R=$(curl -s -X POST $BASE/flow/instances -H "$LA" -H 'Content-Type: application/json' \
  -d '{"defId":2,"title":"Overtime transfer","formData":{"work_date":"2026-09-20","start_time":"18:30","end_time":"21:30","hours":3,"reason":"transfer test"}}')
check "overtime-submit" '"code":200' "$R"
R=$(curl -s "$BASE/flow/instances/my-todo?pageSize=10" -H "$AA")
TID3=$(echo "$R" | grep -o '"taskId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X PUT $BASE/flow/tasks/$TID3/transfer -H "$AA" -H 'Content-Type: application/json' -d '{"toUserId":2,"comment":"please handle"}')
check "transfer-ok" '"code":200' "$R"
ZW=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"zhangwei","password":"123456"}')
ZT=$(tok "$ZW"); ZA="Authorization: Bearer $ZT"
R=$(curl -s "$BASE/flow/instances/my-todo?pageSize=10" -H "$ZA")
check "transferee-todo-hit" "Overtime transfer" "$R"
ZTID=$(echo "$R" | grep -o '"taskId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X PUT $BASE/flow/tasks/$ZTID/approve -H "$ZA" -H 'Content-Type: application/json' -d '{"comment":"done"}')
check "transferee-approve" '"code":200' "$R"

# 8. reject
R=$(curl -s -X POST $BASE/flow/instances -H "$LA" -H 'Content-Type: application/json' \
  -d '{"defId":3,"title":"Travel reject","formData":{"city":"BJ","begin_date":"2026-11-01","end_date":"2026-11-03","days":3,"reason":"reject test"}}')
check "travel-submit" '"code":200' "$R"
R=$(curl -s "$BASE/flow/instances/my-todo?pageSize=10" -H "$AA")
TID4=$(echo "$R" | grep -o '"taskId":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s -X PUT $BASE/flow/tasks/$TID4/reject -H "$AA" -H 'Content-Type: application/json' -d '{"comment":"not allowed"}')
check "reject-ok" '"code":200' "$R"
R=$(curl -s "$BASE/flow/instances/mine?pageSize=10" -H "$LA")
check "instance-rejected" '"REJECTED"' "$R"

# 9. withdraw (approving only)
R=$(curl -s -X POST $BASE/flow/instances -H "$LA" -H 'Content-Type: application/json' \
  -d '{"defId":2,"title":"Withdraw test","formData":{"work_date":"2026-09-22","start_time":"19:00","end_time":"22:00","hours":3,"reason":"withdraw"}}')
check "withdraw-submit" '"code":200' "$R"
IID2=$(echo "$R" | grep -o '"data":[0-9]*' | cut -d: -f2)
R=$(curl -s -X PUT $BASE/flow/instances/$IID2/withdraw -H "$LA")
check "withdraw-ok" '"code":200' "$R"
R=$(curl -s "$BASE/flow/instances/mine?pageSize=10" -H "$LA")
check "instance-withdrawn" '"WITHDRAWN"' "$R"

# 10. portal summary counts
R=$(curl -s $BASE/portal/summary -H "$LA")
check "portal-summary" '"todoCount":' "$R"
R=$(curl -s $BASE/messages/unread-count -H "$LA")
check "unread-count" '"code":200' "$R"

# 11. admin archive
R=$(curl -s "$BASE/admin/flow/instances?status=APPROVED&pageSize=10" -H "$AA")
check "archive-approved" '"total":' "$R"

echo "----------------------------------------"
echo "FLOW PASS=$pass FAIL=$fail"