package com.xiaodou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.model.UserAccountCertification;
import com.xiaodou.model.query.CertificationQuery;
import com.xiaodou.model.vo.CertificationVO;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户账号认证申请表 Mapper 接口
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
public interface UserAccountCertificationMapper extends BaseMapper<UserAccountCertification> {

    /**
     * 自定义分页查询，关联用户表等
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页的视图对象
     */
    IPage<CertificationVO> selectPageWithDetails(IPage<CertificationVO> page, @Param("query") CertificationQuery query);

    /**
     * 根据ID查询单个详情，关联用户表等
     * @param id 申请ID
     * @return 视图对象
     */
    CertificationVO selectDetailById(@Param("id") String id);
}
