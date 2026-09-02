import org.springframework.security.crypto.bcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 登录密码明文/密文转换工具（BCrypt 单向散列）。
 * 说明：BCrypt 不可逆，无法从密文直接还原明文；
 *       reverse / reverse-db 采用候选密码校验反查，仅能命中常见密码或项目默认密码。
 *
 * 用法：
 *   hash    <明文>                   明文 -> 密文
 *   check   <明文> <密文>            校验明文是否为该密文的原密码
 *   reverse <密文>                   用候选字典反查单个密文
 *   reverse-db                       从 stdin 读取 "用户名\t密文" 逐行反查（配合 mysql 输出）
 */
public class PasswordTool {

    private static final String[] DICT = {
            "123456", "12345678", "123456789", "1234567890", "123123", "666666", "888888",
            "admin", "admin123", "password", "passw0rd", "qwerty", "abc123", "test1234",
            "654321", "000000", "1234567", "111111"
    };

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                usage();
                return;
            }
            switch (args[0]) {
                case "hash" -> { require(args, 2); hash(args[1]); }
                case "check" -> { require(args, 3); check(args[1], args[2]); }
                case "reverse" -> { require(args, 2); reverse(args[1]); }
                case "reverse-db" -> reverseDb();
                default -> usage();
            }
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    private static void hash(String plain) {
        System.out.println("明文: " + plain);
        System.out.println("密文: " + BCrypt.hashpw(plain, BCrypt.gensalt(10)));
    }

    private static void check(String plain, String hash) {
        boolean ok = BCrypt.checkpw(plain, hash);
        System.out.println(ok
                ? "匹配：明文「" + plain + "」就是该密文的原密码"
                : "不匹配：明文「" + plain + "」不是该密文的原密码");
    }

    private static void reverse(String hash) {
        for (String p : DICT) {
            if (BCrypt.checkpw(p, hash)) {
                System.out.println("命中明文: " + p);
                return;
            }
        }
        System.out.println("候选字典未命中。BCrypt 为单向散列，无法逆向，只能通过候选密码校验反查。");
    }

    private static void reverseDb() throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        int total = 0, hit = 0;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\t");
            String username = parts[0];
            String hash = parts[parts.length - 1];
            total++;
            String plain = match(hash);
            hit += plain != null ? 1 : 0;
            System.out.printf("%-14s -> %s%n", username, plain == null ? "未命中字典" : plain);
        }
        System.out.printf("%n共 %d 个账号，命中 %d 个。%n", total, hit);
    }

    private static String match(String hash) {
        for (String p : DICT) {
            if (BCrypt.checkpw(p, hash)) {
                return p;
            }
        }
        return null;
    }

    private static void require(String[] args, int n) {
        if (args.length < n) {
            usage();
            System.exit(1);
        }
    }

    private static void usage() {
        System.out.println("用法:");
        System.out.println("  hash    <明文>                明文 -> 密文");
        System.out.println("  check   <明文> <密文>         校验明文是否为该密文原密码");
        System.out.println("  reverse <密文>                候选字典反查单个密文");
        System.out.println("  reverse-db                    连库反查全部账号（由启动脚本调 mysql）");
        System.out.println();
        System.out.println("示例: password-tool hash 123456");
        System.out.println("      password-tool check 123456 $2a$10$VMkl...");
        System.out.println("      password-tool reverse $2a$10$VMkl...");
    }
}
