package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.model.TaskParticipation;
import com.xiaodou.model.query.TaskParticipationQuery;
import com.xiaodou.model.vo.TaskParticipationVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 任务参与记录表 Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-17
 */
public interface TaskParticipationMapper extends BaseMapper<TaskParticipation> {

    /**
     * 自定义分页查询，关联微信用户表以获取用户昵称
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 分页的视图对象
     */
    IPage<TaskParticipationVO> selectPageWithDetails(IPage<TaskParticipationVO> page, @Param("query") TaskParticipationQuery query);

    /**
     * 根据任务ID和状态查询用户ID列表
     * @param taskId 任务ID
     * @param status 状态
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM task_participation WHERE task_id = #{taskId} AND status = #{status}")
    List<String> selectUserIdsByTaskAndStatus(@Param("taskId") String taskId, @Param("status") byte status);
}
