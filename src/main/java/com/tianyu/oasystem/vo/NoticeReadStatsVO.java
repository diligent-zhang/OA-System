package com.tianyu.oasystem.vo;

import lombok.Data;

import java.util.List;

/** 公告已读统计 */
@Data
public class NoticeReadStatsVO {

    /** 应读人数（启用中的账号） */
    private Long totalUsers;
    /** 已读人数 */
    private Long readCount;
    /** 未读人数 */
    private Long notReadCount;
    private List<String> readUsers;
    private List<String> notReadUsers;
}