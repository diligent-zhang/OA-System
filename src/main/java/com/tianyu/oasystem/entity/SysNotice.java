package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysNotice {

    private Long noticeId;
    private String title;
    private String content;
    private Integer type;
    private Integer topFlag;
    private Integer status;
    private Long publisherId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
