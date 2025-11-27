package com.xiaodou.model.vo;

import com.xiaodou.model.AiAppType;
import com.xiaodou.utils.DateTimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 工作流类型视图对象（View Object）
 * <p>
 * 用于封装工作流类型的核心信息，包含类型标识、名称、描述、状态等关键属性。
 * 主要应用于以下场景：
 * <ul>
 * <li>工作流类型管理界面展示</li>
 * <li>工作流创建时的类型选择</li>
 * <li>工作流类型权限控制</li>
 * </ul>
 * </p>
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/5/9
 */
@Data
@Schema(description = "智能体类型响应")
@Accessors(chain = true)
public class AiAppTypeVO {

    /**
     * 工作流类型唯一标识
     * <p>
     * 系统自动生成的UUID格式字符串，作为工作流类型的主键标识。
     * 格式：8-4-4-4-12的十六进制字符（如：550e8400-e29b-41d4-a716-446655440000）
     * </p>
     */
    @Schema(description = "类型ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    /**
     * 工作流类型名称
     * <p>
     * 用户可读的类型名称，用于界面展示和类型选择。
     * <b>约束：</b>
     * <ul>
     * <li>长度限制：2-50个字符</li>
     * <li>必填字段</li>
     * <li>不可重复（同一团队内）</li>
     * </ul>
     * </p>
     */
    @Schema(description = "类型名称", example = "文本处理")
    private String name;

    /**
     * 工作流类型描述
     * <p>
     * 详细说明该类型工作流的用途、特点和适用场景。
     * <b>约束：</b>
     * <ul>
     * <li>长度限制：最多500个字符</li>
     * <li>支持多语言内容</li>
     * </ul>
     * </p>
     */
    @Schema(description = "类型描述", example = "处理文本相关的AI智能体")
    private String description;

    /**
     * 工作流类型状态
     * <p>
     * 控制类型是否可用，影响基于该类型创建工作流的功能。
     * <b>状态值：</b>
     * <ul>
     * <li>1 - 启用：类型可用，可创建新工作流</li>
     * <li>0 - 禁用：类型不可用，仅可查看已有工作流</li>
     * </ul>
     * </p>
     */
    @Schema(description = "状态：1-启用，0-禁用", example = "1")
    private Byte status;

    /**
     * 关联的团队ID
     * <p>
     * 标识该工作流类型所属的团队，用于团队隔离和权限控制。
     * 格式：UUID字符串
     * </p>
     */
    @Schema(description = "关联的租户ID")
    private String tenantId;

    /**
     * 创建人ID
     * <p>
     * 记录创建该工作流类型的用户标识，用于审计和权限追溯。
     * 格式：用户唯一标识符（如：user123）
     * </p>
     */
    @Schema(description = "创建人ID", example = "user123")
    private String creatorId;

    /**
     * 创建时间（毫秒时间戳）
     */
    @Schema(description = "创建时间")
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    @Schema(description = "更新时间")
    private Long updatedAt;

    /**
     * 该类型下的工作流数量
     * <p>
     * 统计当前团队中属于该类型的工作流总数，用于展示和管理参考。
     * <b>约束：</b>
     * <ul>
     * <li>最小值：0</li>
     * <li>实时统计（非缓存值）</li>
     * </ul>
     * </p>
     */
    @Schema(description = "该类型下的工作流数量")
    private Integer workflowCount;

    /**
     * 将工作流类型实体转换为视图对象
     * <p>
     * 执行实体到VO的属性拷贝，使用Spring的BeanUtils工具类实现。
     * 转换规则：
     * <ul>
     * <li>同名属性自动拷贝</li>
     * <li>忽略null值属性</li>
     * <li>不处理关联对象</li>
     * </ul>
     * </p>
     *
     * @param type 工作流类型实体，不能为null
     * @return 转换后的视图对象，当输入为null时返回null
     */
    public static AiAppTypeVO fromEntity(AiAppType type) {
        if (type == null) {
            return null;
        }
        AiAppTypeVO vo = new AiAppTypeVO();
        BeanUtils.copyProperties(type, vo);
        vo.setCreatedAt(DateTimeUtils.toTimestampAtUTC8(type.getCreatedAt()));
        vo.setUpdatedAt(DateTimeUtils.toTimestampAtUTC8(type.getUpdatedAt()));
        return vo;
    }
}
