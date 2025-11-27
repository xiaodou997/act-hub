package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaodou.model.Reward;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 奖品配置表 Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
public interface RewardMapper extends BaseMapper<Reward> {

    /**
     * 原子性地增加奖品总库存
     * @param rewardId 奖品ID
     * @param count 要增加的数量
     * @return 受影响的行数
     */
    @Update("UPDATE reward SET total_quantity = total_quantity + #{count} WHERE id = #{rewardId}")
    int increaseTotalQuantity(@Param("rewardId") String rewardId, @Param("count") int count);

    /**
     * 原子性地增加奖品已发放数量
     * @param rewardId 奖品ID
     * @param count 要增加的数量
     * @return 受影响的行数
     */
    @Update("UPDATE reward SET issued_quantity = issued_quantity + #{count} WHERE id = #{rewardId}")
    int increaseIssuedQuantity(@Param("rewardId") String rewardId, @Param("count") int count);
}
