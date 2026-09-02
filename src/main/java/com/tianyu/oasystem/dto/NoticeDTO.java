package com.tianyu.oasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticeDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过 100 个字符")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String content;

    @NotNull(message = "请选择公告类型")
    private Integer type;

    private Integer topFlag = 0;

    /** true 保存后立即发布，false 下架状态（草稿） */
    private Boolean publish = true;
}
