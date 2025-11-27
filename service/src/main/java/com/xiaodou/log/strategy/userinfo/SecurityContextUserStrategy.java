package com.xiaodou.log.strategy.userinfo;

import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.log.model.LogUser;
import com.xiaodou.model.auth.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全上下文用户信息策略
 *
 * @author xiaodou V=>dddou117
 * @version V1.0
 * @since 2025/10/9
 */
@Component
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SecurityContextUserStrategy implements UserInfoStrategy {
    @Override
    public LogUser resolveUser() {
        try {
            // 方式1：使用 UserContextHolder
            String userId = UserContextHolder.getUserId();
            String tenantId = UserContextHolder.getTenantId();

            if (userId != null) {
                return LogUser.of(userId, tenantId);
            }

            // 方式2：使用 SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(
                authentication.getPrincipal())) {

                Object principal = authentication.getPrincipal();
                if (principal instanceof UserInfoVO user) {
                    return LogUser.of(user.getUserId(), user.getTenantId());
                } else if (principal instanceof String) {
                    return LogUser.of((String)principal, null);
                }
            }
        } catch (Exception e) {
            log.debug("从SecurityContext获取用户信息失败: {}", e.getMessage());
        }

        return LogUser.anonymous();
    }

    @Override
    public String getStrategyName() {
        return "SECURITY_CONTEXT";
    }
}
