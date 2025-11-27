package com.xiaodou.model.dto.certification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 用户提交认证请求DTO
 */
@Data
@Schema(description = "用户提交认证请求DTO")
public class CertificationSubmitDTO {

    @Schema(description = "已存在的申请ID（用于重新提交）")
    private String id;

    @Schema(description = "认证平台 (例如: DOUYIN, XIAOHONGSHU)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "认证平台不能为空")
    private String platform;

    @Schema(description = "用户在平台上的唯一ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "平台账号ID不能为空")
    private String accountId;

    @Schema(description = "用户在平台上的昵称")
    private String accountName;

    @Schema(description = "个人主页链接", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "个人主页链接不能为空")
    private String homepageUrl;

    @Schema(description = "截图URL列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "至少需要一张截图")
    private List<String> screenshotUrls;
}
