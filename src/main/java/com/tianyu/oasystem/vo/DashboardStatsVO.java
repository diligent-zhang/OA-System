package com.tianyu.oasystem.vo;

import lombok.Data;

/** 仪表盘统计卡片数据 */
@Data
public class DashboardStatsVO {

    private Integer empTotal;
    private Integer onJobTotal;
    private Integer deptTotal;
    private Integer noticeTotal;
}
