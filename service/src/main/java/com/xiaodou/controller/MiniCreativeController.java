package com.xiaodou.controller;

import com.xiaodou.aiapp.AiAppExecutor;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.AiApplication;
import com.xiaodou.result.Result;
import com.xiaodou.service.AiApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "小程序-创作助手", description = "AI文案助手：根据链接生成润色文案")
@RestController
@RequestMapping("/api/creative")
@RequiredArgsConstructor
public class MiniCreativeController {

    private final AiAppExecutor aiAppExecutor;
    private final AiApplicationService aiApplicationService;

    @PostMapping("/rewrite")
    @Operation(summary = "根据链接生成文案")
    public Result<Object> rewrite(@RequestBody RewriteDTO dto) {
        String userId = UserContextHolder.getUserId();
        Long appId = pickAppId(dto.getUrl());
        if (appId == null) {
            return Result.fail("未找到合适的AI应用，请先在后台配置xhsRewrite或webRewrite应用");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("url", dto.getUrl());
        params.put("text", dto.getText());
        Object res = aiAppExecutor.execute(userId, appId, params);
        return Result.success(res);
    }

    private Long pickAppId(String url) {
        String u = url == null ? "" : url.toLowerCase();
        String name = u.contains("xhslink.com") ? "xhsRewrite" : "webRewrite";
        AiApplication app = aiApplicationService.lambdaQuery().eq(AiApplication::getName, name).eq(AiApplication::getEnabled, 1).one();
        return app == null ? null : app.getId();
    }

    public static class RewriteDTO {
        private String url;
        private String text;
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}

