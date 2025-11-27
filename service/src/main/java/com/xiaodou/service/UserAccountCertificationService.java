package com.xiaodou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaodou.model.UserAccountCertification;
import com.xiaodou.model.dto.certification.CertificationAuditDTO;
import com.xiaodou.model.dto.certification.CertificationSubmitDTO;
import com.xiaodou.model.query.CertificationQuery;
import com.xiaodou.model.vo.CertificationVO;

/**
 * <p>
 * 用户账号认证申请表 服务类
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
public interface UserAccountCertificationService extends IService<UserAccountCertification> {

    /**
     * 用户提交认证申请
     * @param submitDTO 提交信息
     * @param userId 申请人ID
     * @return 申请记录ID
     */
    String submit(CertificationSubmitDTO submitDTO, String userId);

    /**
     * 管理员审核认证申请
     * @param auditDTO 审核信息
     * @param auditorId 审核人ID
     */
    void audit(CertificationAuditDTO auditDTO, String auditorId);

    /**
     * 分页查询认证申请列表
     * @param query 查询条件
     * @return 认证申请VO分页列表
     */
        IPage<CertificationVO> pageList(CertificationQuery query);
    
        /**
         * 获取单个申请详情
         * @param id 申请ID
         * @return 认证申请VO
         */
        CertificationVO getDetail(String id);
    
        /**
         * 根据用户和平台获取最新的认证申请
         * @param userId 用户ID
         * @param platform 平台
         * @return 认证申请VO
         */
        CertificationVO getByUserAndPlatform(String userId, String platform);
    }
    