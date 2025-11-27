package com.xiaodou.model.vo;

import com.xiaodou.model.AiApplication;
import com.xiaodou.utils.DateTimeUtils;
import lombok.Data;

/**
 * AI应用视图对象
 * <p>
 * 用于向前端返回AI应用信息，时间字段转换为毫秒时间戳
 * </p>
 */
@Data
public class AiApplicationVO {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用描述
     */
    private String description;

    /**
     * Spring Bean 名称
     */
    private String handlerBean;

    /**
     * 应用类型ID
     */
    private String typeId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 入参 JSON Schema
     */
    private String paramSchema;

    /**
     * 出参 JSON Schema
     */
    private String resultSchema;

    /**
     * 应用版本
     */
    private String version;

    /**
     * 单次调用价格（单位：分，0=免费）
     */
    private Integer price;

    /**
     * 是否启用（1=启用，0=禁用）
     */
    private Byte enabled;

    /**
     * 执行超时时间（毫秒）
     */
    private Integer timeoutMs;

    /**
     * 系统级扩展配置
     */
    private String config;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    /**
     * 从实体对象转换
     */
    public static AiApplicationVO fromEntity(AiApplication application) {
        if (application == null) {
            return null;
        }
        AiApplicationVO vo = new AiApplicationVO();
        vo.setId(application.getId());
        vo.setName(application.getName());
        vo.setDescription(application.getDescription());
        vo.setHandlerBean(application.getHandlerBean());
        vo.setTypeId(application.getTypeId());
        vo.setTenantId(application.getTenantId());
        vo.setParamSchema(application.getParamSchema());
        vo.setResultSchema(application.getResultSchema());
        vo.setVersion(application.getVersion());
        vo.setPrice(application.getPrice());
        vo.setEnabled(application.getEnabled());
        vo.setTimeoutMs(application.getTimeoutMs());
        vo.setConfig(application.getConfig());
        vo.setCreatedAt(DateTimeUtils.toTimestampAtUTC8(application.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestampAtUTC8(application.getUpdatedAt()));
        return vo;
    }
}
