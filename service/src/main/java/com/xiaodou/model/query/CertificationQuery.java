package com.xiaodou.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 认证申请查询对象
 */
@Data
@Schema(description = "认证申请查询对象")
public class CertificationQuery {

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "申请人ID")
    private String userId;

    @Schema(description = "认证平台")
    private String platform;

    @Schema(description = "审核状态（0:待审核, 1:已通过, 2:已拒绝）")
    private Byte status;

    @Schema(description = "平台账号ID或昵称（模糊查询）")
    private String accountKeyword;
}
