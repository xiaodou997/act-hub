# 数据埋点与后台查询 - 设计方案

## 一、需求概述

- 小程序端上报用户行为事件
- 后台分页查询原始事件明细

## 二、接口设计

- 上报
  - POST `/api/track`，Body：`{ eventName, properties }`
- 查询
  - GET `/admin/tracking-event/page`

## 三、事件模型

- eventName、userId、tenantId、properties、createdAt

## 四、前端页面

- 路由与组件键名：`/tracking/event` → `TrackingEventManagement`
- 列表展示事件名、用户ID、租户ID、属性、时间

## 五、处理流程

- 接收后写入消息队列，后台异步入库

