# 消息通知 - 设计方案

## 一、需求概述

- 用户查看奖励发放、审核结果等系统消息
- 支持未读/已读标识与标记已读

## 二、接口设计

- GET `/notification/page`
- POST `/notification/{id}/read`

## 三、前端页面

- 路由与组件键名：`/notification` → `NotificationCenter`
- 列表展示标题、内容、时间、状态，支持标记已读

## 四、通知来源

- 审核通过/拒绝、奖励发放成功/失败、系统公告

