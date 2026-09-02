USE oa_system;
INSERT INTO emp_employee (emp_id, emp_no, name, gender, phone, email, dept_id, hire_date, status)
VALUES (9, 'EMP20260000', '系统管理员', 1, '13800000000', 'admin@tianyu.com', 5, '2026-01-01', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);
UPDATE sys_user SET emp_id = 9 WHERE username = 'admin';
