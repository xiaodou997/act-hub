package com.xiaodou.controller.api;

import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.dto.certification.CertificationSubmitDTO;
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
 * C端-账号认证接口
 */
@Tag(name = "C端-账号认证", description = "C端用户提交认证申请和查询状态的接口")
@RestController
@RequestMapping("/api/certification")
@RequiredArgsConstructor
public class CertificationApiController {

    private final UserAccountCertificationService certificationService;

    @Operation(summary = "提交或重新提交认证申请", description = "用户提交新的认证申请，或在被拒绝后重新提交")
    @PostMapping("/submit")
    public Result<String> submitCertification(@Valid @RequestBody CertificationSubmitDTO submitDTO) {
        String userId = UserContextHolder.getUserId();
        String certificationId = certificationService.submit(submitDTO, userId);
        return Result.success(certificationId);
    }

    @Operation(summary = "查询我的认证状态", description = "根据平台类型查询当前用户的最新认证申请状态")
    @Parameter(name = "platform", description = "平台类型 (例如: DOUYIN, XIAOHONGSHU)", required = true)
    @GetMapping("/status")
    public Result<CertificationVO> getMyCertificationStatus(@RequestParam String platform) {
        String userId = UserContextHolder.getUserId();
        CertificationVO vo = certificationService.getByUserAndPlatform(userId, platform);
        return Result.success(vo);
    }
}
