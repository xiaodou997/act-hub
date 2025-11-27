package com.xiaodou.model.dto.tracking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "数据埋点事件上报DTO")
public class TrackingEventDTO {

    @Schema(description = "事件名称 (例如: PageView, ButtonClick)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "事件名称不能为空")
    private String eventName;

    @Schema(description = "事件相关属性 (动态JSON对象)")
    private Map<String, Object> properties;
}
