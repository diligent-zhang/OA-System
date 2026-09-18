package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoticeVO {

    private Long noticeId;
    private String title;
    private String content;
    private Integer type;
    private Integer topFlag;
    private Integer status;
    private String publisherName;

    /** 附件数量（列表） */
    private Integer attachCount;

    /** 当前用户是否已读（员工端） */
    private Boolean readFlag;

    /** 附件明细（详情） */
    private List<Object> attachments;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
