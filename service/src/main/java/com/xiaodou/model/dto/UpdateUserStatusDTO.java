package com.xiaodou.model.dto;

import lombok.Data;

@Data
public class UpdateUserStatusDTO {
    /**
     * 用户状态（1:正常, 0:禁用）
     */
    private Byte status;
}
