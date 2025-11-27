package com.xiaodou.model.dto.user;

import lombok.Data;

/**
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/2
 */
@Data
public class UserPageDTO {

    private Integer pageNum = 1;
    private Integer pageSize = 20;

    private String username;
    private String tenantName;
    private String tenantId;
    private Byte status;
    private String remark;
}
