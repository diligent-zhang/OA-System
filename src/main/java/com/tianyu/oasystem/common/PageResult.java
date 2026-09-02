package com.tianyu.oasystem.common;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

/** 分页结果封装 */
@Data
public class PageResult<T> {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<T> list;

    public static <T> PageResult<T> of(List<T> list) {
        PageInfo<T> info = new PageInfo<>(list);
        PageResult<T> result = new PageResult<>();
        result.setTotal(info.getTotal());
        result.setPageNum(info.getPageNum());
        result.setPageSize(info.getPageSize());
        result.setList(info.getList());
        return result;
    }
}
