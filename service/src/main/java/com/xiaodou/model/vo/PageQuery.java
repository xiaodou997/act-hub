package com.xiaodou.model.vo;

import lombok.Data;

@Data
public class PageQuery {
    /**
     * 当前页码
     */
    private long pageNum = 1;

    /**
     * 每页数量
     */
    private long pageSize = 10;
}
