package com.tianyu.oasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 打卡参数（定位可选） */
@Data
public class PunchDTO {

    /** IN 上班打卡 / OUT 下班打卡 */
    @NotBlank(message = "缺失打卡类型")
    @Pattern(regexp = "IN|OUT", message = "打卡类型必须为 IN 或 OUT")
    private String type;

    private Double latitude;
    private Double longitude;
    private String address;
}