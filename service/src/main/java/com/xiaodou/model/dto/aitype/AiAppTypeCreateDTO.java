package com.xiaodou.model.dto.aitype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI应用类型创建数据传输对象
 *
 * @author xiaodou V=>dddou117
 * @since 2025/5/9
 * @version 1.0
 */
@Data
@Schema(description = "创建智能体类型请求")
@JsonIgnoreProperties(ignoreUnknown = true) // 👈 添加这一行
public class AiAppTypeCreateDTO {

    /**
     * 类型名称，不能为空
     */
    @NotBlank(message = "类型名称不能为空")
    @Schema(description = "类型名称", example = "文本处理")
    private String name;

    /**
     * 类型描述信息
     */
    @Schema(description = "类型描述", example = "处理文本相关的AI智能体")
    private String description;

    /**
     * 状态：1-启用，0-禁用，默认为启用状态
     */
    @Schema(description = "状态：true-启用，false-禁用", example = "true")
    private Byte status = 1;
}