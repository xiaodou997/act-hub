package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.Reward;
import com.xiaodou.model.dto.reward.RewardCreateDTO;
import com.xiaodou.model.dto.reward.RewardUpdateDTO;
import com.xiaodou.model.query.RewardQuery;
import com.xiaodou.model.vo.RewardVO;

/**
 * <p>
 * 奖品配置表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
public interface RewardService extends IService<Reward> {

    /**
     * 创建奖品
     * @param createDTO 奖品创建DTO
     * @param creatorId 创建人ID
     * @return 创建成功的奖品ID
     */
    String createReward(RewardCreateDTO createDTO, String creatorId);

    /**
     * 更新奖品
     * @param updateDTO 奖品更新DTO
     * @return 是否更新成功
     */
    boolean updateReward(RewardUpdateDTO updateDTO);

    /**
     * 删除奖品（逻辑删除，更新状态为归档）
     * @param id 奖品ID
     * @return 是否删除成功
     */
    boolean deleteReward(String id);

    /**
     * 分页查询奖品列表
     * @param query 查询条件
     * @return 奖品VO分页列表
     */
    IPage<RewardVO> pageListRewards(RewardQuery query);

    /**
     * 获取奖品详情
     * @param id 奖品ID
     * @return 奖品VO
     */
    RewardVO getRewardDetail(String id);
}