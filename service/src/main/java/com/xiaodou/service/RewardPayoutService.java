package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.RewardPayout;
import com.xiaodou.model.dto.reward.RewardPayoutDTO;
import com.xiaodou.model.query.RewardPayoutQuery;
import com.xiaodou.model.vo.RewardPayoutVO;

/**
 * <p>
 * 奖励发放记录表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
public interface RewardPayoutService extends IService<RewardPayout> {

    /**
     * 批量发放奖励
     * @param payoutDTO 发放请求DTO
     * @param operatorId 操作员ID
     * @return 成功发放的数量
     */
    int distributeRewards(RewardPayoutDTO payoutDTO, String operatorId);

    /**
     * 分页查询奖励发放记录
     * @param query 查询条件
     * @return 发放记录VO分页列表
     */
    IPage<RewardPayoutVO> pageListPayouts(RewardPayoutQuery query);
}