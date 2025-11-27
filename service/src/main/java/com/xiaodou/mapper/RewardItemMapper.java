package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.model.RewardItem;
import com.xiaodou.model.query.RewardItemQuery;
import com.xiaodou.model.vo.RewardItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 奖品实例表（券码/单号库存） Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
public interface RewardItemMapper extends BaseMapper<RewardItem> {

    /**
     * 自定义分页查询，关联奖品表、用户表等
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页的视图对象
     */
    IPage<RewardItemVO> selectPageWithDetails(IPage<RewardItemVO> page, @Param("query") RewardItemQuery query);

    /**
     * 查询并锁定指定数量的可用奖品实例 (悲观锁)
     * @param rewardId 奖品ID
     * @param limit 数量
     * @return 锁定的奖品实例列表
     */
    List<RewardItem> selectAndLockAvailableItems(@Param("rewardId") String rewardId, @Param("limit") int limit);

    /**
     * 批量更新奖品实例的状态
     * @param itemIds 实例ID列表
     * @param status 目标状态
     * @return 受影响的行数
     */
    int batchUpdateStatus(@Param("itemIds") List<String> itemIds, @Param("status") byte status);
}
