USE oa_system;
DELETE FROM sys_user WHERE username LIKE 'regtest%' OR username LIKE 'citest%' OR username LIKE 'cibatch%';
DELETE FROM emp_employee WHERE emp_id > 9;
