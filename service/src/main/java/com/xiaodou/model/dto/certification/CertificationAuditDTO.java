package com.xiaodou.model.dto.certification;

import com.xiaodou.model.UserAccountCertification;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员审核认证请求DTO
 */
@Data
@Schema(description = "管理员审核认证请求DTO")
public class CertificationAuditDTO {

    @Schema(description = "要审核的申请ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "申请ID不能为空")
    private String id;

    @Schema(description = "审核结果状态（1:通过, 2:拒绝）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审核结果不能为空")
    private UserAccountCertification.CertificationStatus status;

    @Schema(description = "审核备注（拒绝时必填）")
    private String auditNotes;
}
