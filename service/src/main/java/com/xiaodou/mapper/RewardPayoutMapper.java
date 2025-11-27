package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.model.RewardPayout;
import com.xiaodou.model.query.RewardPayoutQuery;
import com.xiaodou.model.vo.RewardPayoutVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 奖励发放记录表 Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
public interface RewardPayoutMapper extends BaseMapper<RewardPayout> {

    /**
     * 查询指定任务已发放奖励的用户ID列表
     * @param taskId 任务ID
     * @return 用户ID列表
     */
    List<String> selectPaidUserIdsByTaskId(@Param("taskId") String taskId);

    /**
     * 自定义分页查询，关联多表
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页的视图对象
     */
    IPage<RewardPayoutVO> selectPageWithDetails(IPage<RewardPayoutVO> page, @Param("query") RewardPayoutQuery query);

}
