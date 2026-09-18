package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUser {

    private Long userId;
    private String username;
    private String password;
    private String role;
    private Long empId;
    private String avatarUrl;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
