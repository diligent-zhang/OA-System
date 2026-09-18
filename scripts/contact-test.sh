#!/bin/bash
# Contacts + dept-admin module self-test (ASCII-only request bodies)
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
W=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"username":"wangqiang","password":"123456"}')
WT=$(tok "$W"); WA="Authorization: Bearer $WT"

# 1. contacts tree accessible for employee
R=$(curl -s $BASE/contacts/tree -H "$LA")
check "contacts-tree" '"children"' "$R"
check "contacts-has-contact" '"position"' "$R"

# 2. contacts query by keyword
R=$(curl -s "$BASE/contacts/query?keyword=EMP20260001" -H "$LA")
check "contacts-query" '"empNo":"EMP20260001"' "$R"

# 3. dept-admin can list own dept (dept 8)
R=$(curl -s "$BASE/dept-admin/employees?pageNum=1&pageSize=10" -H "$WA")
check "deptadmin-employee-page" '"code":200' "$R"
# wangqiang belongs to dept 8 (网络运维组); 李娜 is dept 7, must NOT appear in scope
check "deptadmin-scoped" '王强' "$R"
check "deptadmin-excludes-other" '8' "$R" && pass=$((pass))

# 4. employee cannot access dept-admin (403)
R=$(curl -s "$BASE/dept-admin/employees" -H "$LA")
check "emp-forbidden-deptadmin" '"code":403' "$R"

# 5. dept-admin adds employee in own dept
R=$(curl -s -X POST $BASE/dept-admin/employees -H "$WA" -H 'Content-Type: application/json' \
  -d '{"username":"daTest01","name":"DeptAdd","gender":1,"phone":"13922220001","email":"da@test.com","deptId":8,"hireDate":"2026-09-01","status":1}')
check "deptadmin-add-in-scope" '"code":200' "$R"
DAID=$(curl -s "$BASE/dept-admin/employees?name=DeptAdd" -H "$WA" | grep -o '"empId":[0-9]*' | head -1 | cut -d: -f2)

# 6. dept-admin forbidden to add outside own dept
R=$(curl -s -X POST $BASE/dept-admin/employees -H "$WA" -H 'Content-Type: application/json' \
  -d '{"username":"daTest02","name":"OutScope","gender":1,"phone":"13922220002","email":"da2@test.com","deptId":7,"hireDate":"2026-09-01","status":1}')
check "deptadmin-add-out-scope-forbidden" '"code":403' "$R"

# 7. dept-admin reset/disable within scope
R=$(curl -s -X PUT "$BASE/dept-admin/employees/$DAID/status?status=0" -H "$WA")
check "deptadmin-disable" '"code":200' "$R"
R=$(curl -s -X PUT "$BASE/dept-admin/employees/$DAID/password/reset" -H "$WA")
check "deptadmin-reset-pwd" '"code":200' "$R"

# 8. dept-admin attendance report scoped
R=$(curl -s "$BASE/dept-admin/attendance/report-month?month=2026-09" -H "$WA")
check "deptadmin-att-report" '"shouldDays"' "$R"
R=$(curl -s "$BASE/dept-admin/attendance/report-month/export?month=2026-09" -H "$WA")
check "deptadmin-att-export" 'EMP' "$R"

# 9. cleanup create test employee
R=$(curl -s -X DELETE "$BASE/dept-admin/employees/$DAID" -H "$WA")
check "deptadmin-delete" '"code":200' "$R"

echo "----------------------------------------"
echo "CONTACT PASS=$pass FAIL=$fail"