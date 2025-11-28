package com.xiaodou.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaodou.auth.util.UserContextHolder;
import com.xiaodou.model.TaskParticipation;
import com.xiaodou.result.Result;
import com.xiaodou.service.TaskParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Tag(name = "小程序-任务参与", description = "提交作品链接与查询我的任务")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MiniParticipationController {

    private final TaskParticipationService participationService;

    private static final Set<String> ALLOWED_DOMAINS = new HashSet<>(Arrays.asList(
            "xhslink.com", "v.douyin.com", "m.toutiao.com", "zhuanlan.zhihu.com"
    ));

    @PostMapping("/task-participation/{id}/submit")
    @Operation(summary = "提交作品链接")
    public Result<Void> submit(@PathVariable String id, @RequestBody SubmitDTO dto) {
        String userId = UserContextHolder.getUserId();
        TaskParticipation p = participationService.getById(id);
        if (p == null || !userId.equals(p.getUserId())) {
            return Result.fail(403, "无权限提交");
        }
        if (!isValidUrl(dto.getLink())) {
            return Result.fail("链接格式错误或域名不在允许列表");
        }
        TaskParticipation u = new TaskParticipation();
        u.setId(id);
        u.setSubmittedLink(dto.getLink());
        u.setSubmittedContent(dto.getContent());
        u.setSubmittedAt(LocalDateTime.now());
        u.setStatus((byte)2); // 已提交
        participationService.updateById(u);
        return Result.success();
    }

    @GetMapping("/my/tasks")
    @Operation(summary = "分页查询我的任务")
    public Result<IPage<TaskParticipation>> myTasks(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) Byte status) {
        String userId = UserContextHolder.getUserId();
        LambdaQueryWrapper<TaskParticipation> q = new LambdaQueryWrapper<>();
        q.eq(TaskParticipation::getUserId, userId);
        if (status != null) q.eq(TaskParticipation::getStatus, status);
        q.orderByDesc(TaskParticipation::getUpdatedAt);
        IPage<TaskParticipation> page = participationService.page(new Page<>(pageNum, pageSize), q);
        return Result.success(page);
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.length() < 8) return false;
        try {
            String u = url.toLowerCase();
            if (!(u.startsWith("http://") || u.startsWith("https://"))) return false;
            String host = u.replaceFirst("^https?://", "").split("/", 2)[0];
            for (String d : ALLOWED_DOMAINS) {
                if (host.endsWith(d)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static class SubmitDTO {
        public String link;
        public String content;
        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}

