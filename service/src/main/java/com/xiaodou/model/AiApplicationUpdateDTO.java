package com.xiaodou.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * AI 应用配置主表
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-10-28
 */
@Getter
@Setter
@ToString
public class AiApplicationUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键，对外唯一应用ID
     */
    private Long id;

    /**
     * 应用名称，如：文本摘要
     */
    private String name;

    /**
     * 一句话描述
     */
    private String description;

    /**
     * Spring Bean 名称，如：summaryAiHandler
     */
    @TableField("handler_bean")
    private String handlerBean;

    /**
     * 入参 JSON Schema（用于校验）
     */
    @TableField("param_schema")
    private String paramSchema;

    /**
     * 出参 JSON Schema（用于文档/前端）
     */
    @TableField("result_schema")
    private String resultSchema;

    /**
     * 单次调用价格（单位：分，0=免费）
     */
    private Integer price;

    /**
     * 是否启用（1=启用，0=禁用）
     */
    private Byte enabled;

    /**
     * 应用分类，如：nlp, cv, audio
     */
    private String typeId;

    /**
     * 执行超时时间（毫秒）
     */
    private Integer timeoutMs;
}
