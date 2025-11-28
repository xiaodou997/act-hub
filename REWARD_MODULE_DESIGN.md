# 奖励模块（奖品配置/库存/发放中心） - 设计方案

## 一、需求概述

- 管理可用于活动结算的奖品，包含实物与虚拟券码
- 维护奖品库存：券码或快递单号
- 在活动结束后按范围批量发放奖励

## 二、数据模型

- 奖品：id、name、imageUrl、quantity、description、rules、validFrom、validTo
- 库存：rewardId、userId、phone、code|trackingNo、createdAt
- 发放记录：taskId、participationId、userId、rewardId、status、issuedAt、failReason

## 三、接口设计

- 奖品管理
  - GET `/reward/page`
  - GET `/reward/{id}`
  - POST `/reward`
  - PUT `/reward`
  - DELETE `/reward/{id}`
- 库存管理
  - GET `/reward-item/page`
  - POST `/reward-item/import`
- 发放中心
  - GET `/reward-payout/page`
  - POST `/reward-payout/distribute`

## 四、前端页面

- 路由与组件键名
  - `/reward` → `RewardManagement`
  - `/reward/item` → `RewardItemManagement`
  - `/reward/payout` → `RewardPayoutCenter`
- 页面要点
  - 奖品管理：列表/筛选/增删改，图片上传、有效期配置
  - 库存管理：支持批量导入 `userid,phone,value` 文本
  - 发放中心：选择任务、发放范围、奖品ID，提交批量发放

## 五、发放策略

- 发放按钮仅在活动状态为“已结束”时可用
- 默认范围为“审核通过”的参与记录
- 券码优先从库存未使用项中发放；实物生成快递任务并记录单号

## 六、权限与菜单

- 管理端需具备 `reward:view`、`reward:edit` 权限
- 菜单：奖品管理、库存管理、发放中心

## 七、联调说明

- 前端已实现 API 与页面，组件键已注册，后端保持现有控制器路径即可联通

