#!/usr/bin/env bash
# ============================================================
#  登录密码 明文/密文 转换工具（BCrypt 单向散列）
#  用法: ./password-tool.sh hash|check|reverse|reverse-db [参数...]
#  注意: BCrypt 不可逆，"reverse" 仅用候选字典校验反查，
#        只能命中常见密码或项目默认密码(123456)。
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA_FILE="$SCRIPT_DIR/PasswordTool.java"

# 定位 spring-security-crypto 包（本地 Maven 仓库）
CRYPTO_JAR=$(find "$HOME/.m2/repository/org/springframework/security/spring-security-crypto" \
    -name "*.jar" 2>/dev/null | head -1)

if [ -z "$CRYPTO_JAR" ]; then
    echo "[ERROR] 未找到 spring-security-crypto，请先执行 mvnw package 让 Maven 下载依赖"
    exit 1
fi
command -v java >/dev/null 2>&1 || { echo "[ERROR] 未检测到 Java (JDK 17+)"; exit 1; }

MODE="$1"
shift
case "$MODE" in
    reverse-db)
        mysql -uroot -proot --default-character-set=utf8mb4 -N -B \
            -e "SELECT username, password FROM oa_system.sys_user" 2>/dev/null \
            | java -cp "$CRYPTO_JAR" "$JAVA_FILE" reverse-db \
            || echo "[ERROR] 数据库反查失败，请确认 MySQL 已启动且 root/root 可连"
        ;;
    hash|check|reverse)
        java -cp "$CRYPTO_JAR" "$JAVA_FILE" "$MODE" "$@"
        ;;
    *)
        java -cp "$CRYPTO_JAR" "$JAVA_FILE"
        ;;
esac
