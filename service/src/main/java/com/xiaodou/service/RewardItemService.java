package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.RewardItem;
import com.xiaodou.model.dto.reward.RewardItemImportDTO;
import com.xiaodou.model.query.RewardItemQuery;
import com.xiaodou.model.vo.RewardItemVO;

/**
 * <p>
 * 奖品实例表（券码/单号库存） 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
public interface RewardItemService extends IService<RewardItem> {

    /**
     * 批量导入奖品实例
     * @param importDTO 导入数据DTO
     * @param importerId 导入操作员ID
     */
    void importRewardItems(RewardItemImportDTO importDTO, String importerId);

    /**
     * 分页查询奖品实例列表
     * @param query 查询条件
     * @return 奖品实例VO分页列表
     */
    IPage<RewardItemVO> pageListRewardItems(RewardItemQuery query);
}