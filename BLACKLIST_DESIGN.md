# 黑名单管理 - 设计方案

## 一、需求概述

- 运营可将特定用户加入黑名单，黑名单用户无法领取或参与任务
- 支持按用户ID、手机号、昵称查询与维护

## 二、接口设计

- GET `/admin/blacklist/page`
- POST `/admin/blacklist`
- DELETE `/admin/blacklist/{id}`
- GET `/admin/blacklist/check/{userId}`

## 三、前端页面

- 路由与组件键名：`/system/blacklist` → `BlacklistManagement`
- 功能：查询、添加、移除

## 四、客户端策略

- 微信小程序端登录后，黑名单用户任务列表为空或领取提示无权限
- 服务端在任务列表与领取接口处进行拦截

## 五、权限

- 需 `blacklist:view`、`blacklist:edit`

