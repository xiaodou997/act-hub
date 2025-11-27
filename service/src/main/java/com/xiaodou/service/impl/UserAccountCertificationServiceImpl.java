package com.xiaodou.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaodou.exception.AppException;
import com.xiaodou.mapper.UserAccountCertificationMapper;
import com.xiaodou.model.UserAccountCertification;
import com.xiaodou.model.dto.certification.CertificationAuditDTO;
import com.xiaodou.model.dto.certification.CertificationSubmitDTO;
import com.xiaodou.model.query.CertificationQuery;
import com.xiaodou.model.vo.CertificationVO;
import com.xiaodou.result.ResultCodeEnum;
import com.xiaodou.service.UserAccountCertificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountCertificationServiceImpl extends ServiceImpl<UserAccountCertificationMapper, UserAccountCertification> implements UserAccountCertificationService {

    private final ObjectMapper objectMapper;
    private final UserAccountCertificationMapper certificationMapper;

    @Override
    public String submit(CertificationSubmitDTO submitDTO, String userId) {
        // 重新提交
        if (StringUtils.hasText(submitDTO.getId())) {
            UserAccountCertification existingCert = this.getById(submitDTO.getId());
            if (existingCert == null) {
                throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "要重新提交的申请不存在");
            }
            if (!userId.equals(existingCert.getUserId())) {
                throw new AppException(ResultCodeEnum.FORBIDDEN, "只能操作自己的申请");
            }
            if (!UserAccountCertification.CertificationStatus.REJECTED.getCode().equals(existingCert.getStatus())) {
                throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "只有“已拒绝”的申请才能重新提交");
            }

            // 更新信息并重新提交
            BeanUtils.copyProperties(submitDTO, existingCert);
            existingCert.setStatus(UserAccountCertification.CertificationStatus.PENDING.getCode());
            // 清空上一次的审核信息
            existingCert.setAuditorId(null);
            existingCert.setAuditNotes(null);
            existingCert.setAuditedAt(null);
            this.updateById(existingCert);
            return existingCert.getId();
        }
        // 新提交
        else {
            // 检查是否已有待审核或已通过的申请
            long existingCount = this.lambdaQuery()
                .eq(UserAccountCertification::getUserId, userId)
                .eq(UserAccountCertification::getPlatform, submitDTO.getPlatform())
                .in(UserAccountCertification::getStatus,
                    UserAccountCertification.CertificationStatus.PENDING.getCode(),
                    UserAccountCertification.CertificationStatus.APPROVED.getCode())
                .count();

            if (existingCount > 0) {
                throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "您已提交过该平台的认证申请，请勿重复提交");
            }

            UserAccountCertification newCert = new UserAccountCertification();
            BeanUtils.copyProperties(submitDTO, newCert);
            newCert.setUserId(userId);
            newCert.setStatus(UserAccountCertification.CertificationStatus.PENDING.getCode());
            this.save(newCert);
            return newCert.getId();
        }
    }

    @Override
    @Transactional
    public void audit(CertificationAuditDTO auditDTO, String auditorId) {
        UserAccountCertification certification = this.getById(auditDTO.getId());
        if (certification == null) {
            throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "认证申请不存在");
        }
        if (!UserAccountCertification.CertificationStatus.PENDING.getCode().equals(certification.getStatus())) {
            throw new AppException(ResultCodeEnum.BUSINESS_ERROR, "该申请不是“待审核”状态，无法操作");
        }
        if (UserAccountCertification.CertificationStatus.REJECTED.equals(auditDTO.getStatus()) && !StringUtils.hasText(auditDTO.getAuditNotes())) {
            throw new AppException(ResultCodeEnum.PARAM_VALID_ERROR, "拒绝时必须填写审核备注");
        }

        // 1. 读取并反序列化现有的 auditHistory
        List<UserAccountCertification.AuditHistoryEntry> history = new ArrayList<>();
        if (StringUtils.hasText(certification.getAuditHistory())) {
            try {
                history = objectMapper.readValue(certification.getAuditHistory(), new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.error("反序列化审核历史失败，certificationId: {}", certification.getId(), e);
            }
        }

        // 2. 创建新的历史条目
        UserAccountCertification.AuditHistoryEntry currentEntry = new UserAccountCertification.AuditHistoryEntry();
        currentEntry.setAccountId(certification.getAccountId());
        currentEntry.setAccountName(certification.getAccountName());
        currentEntry.setHomepageUrl(certification.getHomepageUrl());
        currentEntry.setScreenshotUrls(certification.getScreenshotUrls());
        currentEntry.setSubmittedAt(certification.getUpdatedAt()); // 使用更新时间作为本次提交时间
        currentEntry.setAuditStatus(auditDTO.getStatus().getCode());
        currentEntry.setAuditNotes(auditDTO.getAuditNotes());
        currentEntry.setAuditorId(auditorId);
        currentEntry.setAuditedAt(LocalDateTime.now());
        history.add(currentEntry);

        // 3. 更新主记录
        UserAccountCertification update = new UserAccountCertification();
        update.setId(certification.getId());
        update.setStatus(auditDTO.getStatus().getCode());
        update.setAuditNotes(auditDTO.getAuditNotes());
        update.setAuditorId(auditorId);
        update.setAuditedAt(currentEntry.getAuditedAt());
        try {
            update.setAuditHistory(objectMapper.writeValueAsString(history));
        } catch (JsonProcessingException e) {
            log.error("序列化审核历史失败，certificationId: {}", certification.getId(), e);
            throw new AppException(ResultCodeEnum.INTERNAL_SERVER_ERROR, "保存审核历史失败");
        }

        this.updateById(update);

        // 4. 如果审核通过，可以触发后续操作，例如更新 wechat_user 表
        if (UserAccountCertification.CertificationStatus.APPROVED.equals(auditDTO.getStatus())) {
            // wechatUserService.updateUserVerifiedInfo(certification.getUserId(), ...);
            log.info("用户 {} 的平台 {} 账号认证已通过", certification.getUserId(), certification.getPlatform());
        }
    }

    @Override
    public IPage<CertificationVO> pageList(CertificationQuery query) {
        Page<CertificationVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return certificationMapper.selectPageWithDetails(page, query);
    }

        @Override

        public CertificationVO getDetail(String id) {

            // 使用自定义的关联查询方法获取详情

            CertificationVO vo = certificationMapper.selectDetailById(id);

            if (vo == null) {

                throw new AppException(ResultCodeEnum.DATA_NOT_FOUND, "认证申请不存在");

            }

            return vo;

        }

    

        @Override

        public CertificationVO getByUserAndPlatform(String userId, String platform) {

            UserAccountCertification certification = this.lambdaQuery()

                .eq(UserAccountCertification::getUserId, userId)

                .eq(UserAccountCertification::getPlatform, platform)

                .orderByDesc(UserAccountCertification::getCreatedAt)

                .last("LIMIT 1")

                .one();

    

            if (certification == null) {

                return null; // 或者可以抛出 DATA_NOT_FOUND 异常，具体取决于业务需求

            }

            return CertificationVO.fromEntity(certification);

        }

    }

    