package com.xiaodou.model.vo;

import com.xiaodou.model.UserAccountCertification;
import com.xiaodou.utils.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 认证申请返回视图对象
 */
@Data
@Schema(description = "认证申请返回视图对象")
public class CertificationVO {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "申请人ID")
    private String userId;

    @Schema(description = "申请人昵称")
    private String userName; // 关联查询

    @Schema(description = "认证平台")
    private String platform;

    @Schema(description = "平台账号ID")
    private String accountId;

    @Schema(description = "平台账号昵称")
    private String accountName;

    @Schema(description = "个人主页链接")
    private String homepageUrl;

    @Schema(description = "截图URL列表 (JSON数组)")
    private String screenshotUrls;

    @Schema(description = "审核状态（0:待审核, 1:已通过, 2:已拒绝）")
    private Byte status;

    @Schema(description = "审核状态描述")
    private String statusDesc;

    @Schema(description = "最新审核备注")
    private String auditNotes;

    @Schema(description = "最新审核人ID")
    private String auditorId;

    @Schema(description = "最新审核人名称")
    private String auditorName; // 关联查询

    @Schema(description = "最新审核时间（毫秒时间戳）")
    private Long auditedAt;

    @Schema(description = "历史审核记录 (JSON数组)")
    private String auditHistory;

    @Schema(description = "创建时间（首次提交时间）（毫秒时间戳）")
    private Long createdAt;

    public static CertificationVO fromEntity(UserAccountCertification entity) {
        if (entity == null) {
            return null;
        }
        CertificationVO vo = new CertificationVO();
        BeanUtils.copyProperties(entity, vo);

        vo.setAuditedAt(DateTimeUtils.toTimestampAtUTC8(entity.getAuditedAt()));
        vo.setCreatedAt(DateTimeUtils.toTimestampAtUTC8(entity.getCreatedAt()));

        if (entity.getStatus() != null) {
            for (UserAccountCertification.CertificationStatus cs : UserAccountCertification.CertificationStatus.values()) {
                if (cs.getCode().equals(entity.getStatus())) {
                    vo.setStatusDesc(cs.getDesc());
                    break;
                }
            }
        }
        return vo;
    }
}
