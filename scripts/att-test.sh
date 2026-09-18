#!/bin/bash
# Attendance module self-test (ASCII-only payloads)
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

# 1. active rule exists (seed 09:00-18:00)
R=$(curl -s $BASE/att/rule/active -H "$LA")
check "active-rule" '"09:00"' "$R"

# 2. lina punches in (同日重复运行已打过卡也视为通过)
R=$(curl -s -X POST $BASE/att/punch -H "$LA" -H 'Content-Type: application/json' -d '{"type":"IN","latitude":39.9,"longitude":116.4}')
if echo "$R" | grep -q '"code":200' || echo "$R" | grep -q '已打过上班卡'; then echo "PASS punch-in"; pass=$((pass+1)); else echo "FAIL punch-in -> $R"; fail=$((fail+1)); fi

# 3. duplicate punch-in rejected (未打时二次打卡被拒；已打时亦被拒)
R=$(curl -s -X POST $BASE/att/punch -H "$LA" -H 'Content-Type: application/json' -d '{"type":"IN"}')
check "dup-punch-in-rejected" '"code":400' "$R"

# 4. punch out (同日重复运行已打过也视为通过)
R=$(curl -s -X POST $BASE/att/punch -H "$LA" -H 'Content-Type: application/json' -d '{"type":"OUT"}')
if echo "$R" | grep -q '"code":200' || echo "$R" | grep -q '已打过下班卡'; then echo "PASS punch-out"; pass=$((pass+1)); else echo "FAIL punch-out -> $R"; fail=$((fail+1)); fi

# 5. personal records of this month contain today
R=$(curl -s "$BASE/att/my-records" -H "$LA")
check "my-records" '"recordDate"' "$R"

# 6. admin rules list
R=$(curl -s $BASE/admin/att/rules -H "$AA")
check "admin-rules" '"ruleId":1' "$R"

# 7. admin reports (full company)
R=$(curl -s "$BASE/admin/att/report-month?month=2026-09" -H "$AA")
check "report-month" '"shouldDays"' "$R"
check "report-has-lina" '"李娜"' "$R"

# 8. export CSV
R=$(curl -s "$BASE/admin/att/report-month/export?month=2026-09" -H "$AA")
check "export-csv" 'EMP' "$R"
check "export-has-header" '工号' "$R"

# 9. leave approval links to attendance: lina submits 2-day leave, admin approves
R=$(curl -s -X POST $BASE/flow/instances -H "$LA" -H 'Content-Type: application/json' \
  -d '{"defId":1,"title":"AttLink leave","formData":{"leave_type":"sick","begin_date":"2026-09-24","end_date":"2026-09-25","days":2,"reason":"link test"}}')
IID=$(echo "$R" | grep -o '"data":[0-9]*' | cut -d: -f2)
R=$(curl -s "$BASE/flow/instances/my-todo?pageSize=10" -H "$AA")
TID=$(echo "$R" | grep -o '"taskId":[0-9]*' | head -1 | cut -d: -f2)
curl -s -X PUT $BASE/flow/tasks/$TID/approve -H "$AA" -H 'Content-Type: application/json' -d '{"comment":"ok"}' >/dev/null
R=$(curl -s "$BASE/att/my-records?month=2026-09" -H "$LA")
check "leave-linked-to-att" '"LEAVE"' "$R"

# 10. overtime approval links: submit overtime, approve, record OVERTIME
R=$(curl -s -X POST $BASE/flow/instances -H "$LA" -H 'Content-Type: application/json' \
  -d '{"defId":2,"title":"AttLink overtime","formData":{"work_date":"2026-09-19","start_time":"18:30","end_time":"21:30","hours":3,"reason":"ot test"}}')
R=$(curl -s "$BASE/flow/instances/my-todo?pageSize=10" -H "$AA")
TID=$(echo "$R" | grep -o '"taskId":[0-9]*' | head -1 | cut -d: -f2)
curl -s -X PUT $BASE/flow/tasks/$TID/approve -H "$AA" -H 'Content-Type: application/json' -d '{"comment":"ok"}' >/dev/null
R=$(curl -s "$BASE/att/my-records?month=2026-09" -H "$LA")
check "overtime-linked" '"OVERTIME"' "$R"
check "overtime-hours-note" '3' "$R"

echo "----------------------------------------"
echo "ATT PASS=$pass FAIL=$fail"