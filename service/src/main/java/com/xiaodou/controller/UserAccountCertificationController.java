package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.dto.certification.CertificationAuditDTO;
import com.xiaodou.model.query.CertificationQuery;
import com.xiaodou.model.vo.CertificationVO;
import com.xiaodou.result.Result;
import com.xiaodou.service.UserAccountCertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户账号认证申请表 前端控制器
 * </p>
 *
 * @author luoxiaodou
 * @since 2025-11-18
 */
@Tag(name = "后台-账号认证管理", description = "后台管理用户账号认证申请的接口")
@RestController
@RequestMapping("/admin/certification")
@RequiredArgsConstructor
public class UserAccountCertificationController {

    private final UserAccountCertificationService certificationService;

    @Operation(summary = "审核认证申请", description = "对用户的认证申请进行通过或拒绝操作")
    @PostMapping("/audit")
    public Result<Void> auditCertification(@Valid @RequestBody CertificationAuditDTO auditDTO) {
        String auditorId = UserContextHolder.getUserId();
        certificationService.audit(auditDTO, auditorId);
        return Result.success();
    }

    @Operation(summary = "分页查询认证申请列表", description = "根据查询条件分页获取认证申请列表")
    @GetMapping("/page")
    public Result<IPage<CertificationVO>> pageListCertifications(@Valid CertificationQuery query) {
        IPage<CertificationVO> page = certificationService.pageList(query);
        return Result.success(page);
    }

    @Operation(summary = "获取单个认证申请详情", description = "根据ID获取单个认证申请的详细信息")
    @Parameter(name = "id", description = "申请ID", required = true)
    @GetMapping("/{id}")
    public Result<CertificationVO> getCertificationDetail(@PathVariable String id) {
        CertificationVO detail = certificationService.getDetail(id);
        return Result.success(detail);
    }
}
