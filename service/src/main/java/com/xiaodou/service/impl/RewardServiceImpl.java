package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.RewardMapper;
import com.xiaodou.model.Reward;
import com.xiaodou.model.dto.reward.RewardCreateDTO;
import com.xiaodou.model.dto.reward.RewardUpdateDTO;
import com.xiaodou.model.query.RewardQuery;
import com.xiaodou.model.vo.RewardVO;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.RewardService;
import com.xiaodou.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 奖品配置表 服务实现类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RewardServiceImpl extends ServiceImpl<RewardMapper, Reward> implements RewardService {

    @Override
    @Transactional
    public String createReward(RewardCreateDTO createDTO, String creatorId) {
        Reward reward = new Reward();
        BeanUtils.copyProperties(createDTO, reward);
        reward.setCreatorId(creatorId);
        // 初始状态如果DTO中未指定，默认为草稿
        if (reward.getStatus() == null) {
            reward.setStatus(Reward.RewardStatus.DRAFT.getCode());
        }
        // 初始已发放数量为0
        reward.setIssuedQuantity(0);

        boolean saved = this.save(reward);
        if (!saved) {
            throw new AppException(ResultCodeEnum.OPERATION_FAILED, "创建奖品失败");
        }
        return reward.getId();
    }

    @Override
    @Transactional
    public boolean updateReward(RewardUpdateDTO updateDTO) {
        Reward existingReward = this.getById(updateDTO.getId());
        if (existingReward == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "奖品不存在");
        }

        Reward reward = new Reward();
        BeanUtils.copyProperties(updateDTO, reward);
        // 确保ID被设置，MyBatis-Plus才能根据ID更新
        reward.setId(updateDTO.getId());

        // 检查总库存是否小于已发放数量
        if (updateDTO.getTotalQuantity() != null && updateDTO.getTotalQuantity() < existingReward.getIssuedQuantity()) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "总库存不能小于已发放数量");
        }

        return this.updateById(reward);
    }

    @Override
    @Transactional
    public boolean deleteReward(String id) {
        Reward existingReward = this.getById(id);
        if (existingReward == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "奖品不存在");
        }
        // 逻辑删除：将状态更新为归档
        if (Reward.RewardStatus.ARCHIVED.getCode().equals(existingReward.getStatus())) {
            return true; // 已经是归档状态，无需重复操作
        }
        Reward update = new Reward();
        update.setId(id);
        update.setStatus(Reward.RewardStatus.ARCHIVED.getCode());
        return this.updateById(update);
    }

    @Override
    public IPage<RewardVO> pageListRewards(RewardQuery query) {
        Page<Reward> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Reward> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(query.getName()), Reward::getName, query.getName());
        wrapper.eq(query.getType() != null, Reward::getType, query.getType());
        wrapper.eq(query.getStatus() != null, Reward::getStatus, query.getStatus());
        wrapper.ge(query.getStartDate() != null, Reward::getStartDate, DateTimeUtils.toLocalDateTimeAtUTC8(query.getStartDate())); // 大于等于开始时间
        wrapper.le(query.getEndDate() != null, Reward::getEndDate, DateTimeUtils.toLocalDateTimeAtUTC8(query.getEndDate()));     // 小于等于结束时间

        // 排除已归档的奖品，除非明确查询归档状态
        if (query.getStatus() == null || !Reward.RewardStatus.ARCHIVED.getCode().equals(query.getStatus())) {
            wrapper.ne(Reward::getStatus, Reward.RewardStatus.ARCHIVED.getCode());
        }

        wrapper.orderByDesc(Reward::getCreatedAt); // 按创建时间倒序

        IPage<Reward> rewardPage = this.page(page, wrapper);
        return rewardPage.convert(RewardVO::fromEntity);
    }

    @Override
    public RewardVO getRewardDetail(String id) {
        Reward reward = this.getById(id);
        if (reward == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "奖品不存在");
        }
        return RewardVO.fromEntity(reward);
    }
}