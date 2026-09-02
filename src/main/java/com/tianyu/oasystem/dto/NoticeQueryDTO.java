package com.tianyu.oasystem.dto;

import lombok.Data;

/** 公告分页查询参数 */
@Data
public class NoticeQueryDTO {

    private String title;
    private Integer type;
    private String beginTime;
    private String endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
