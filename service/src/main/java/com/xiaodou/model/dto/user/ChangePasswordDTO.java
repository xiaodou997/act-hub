package com.xiaodou.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @ClassName: ChangePasswordDTO
 * @Description: 修改密码请求体
 * @Author: xiaodou V=>dddou117
 * @Date: 2025/5/9
 * @Version: V1.0
 * @JDK: JDK21
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordDTO {
    @Schema(description = "旧密码", required = true)
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20之间")
    private String newPassword;
}
