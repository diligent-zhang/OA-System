package com.tianyu.oasystem.util;

import com.tianyu.oasystem.mapper.EmpEmployeeMapper;

import java.time.Year;

/** 工号生成：EMP + 年份 + 4 位序号 */
public class GenerateUtil {

    public static String nextEmpNo(EmpEmployeeMapper mapper) {
        String prefix = "EMP" + Year.now().getValue();
        String max = mapper.selectMaxEmpNo(prefix);
        int seq = 1;
        if (max != null && max.length() > prefix.length()) {
            try {
                seq = Integer.parseInt(max.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                // 工号后缀异常时重新从 1 计数，由唯一索引兜底
            }
        }
        return prefix + String.format("%04d", seq);
    }
}
