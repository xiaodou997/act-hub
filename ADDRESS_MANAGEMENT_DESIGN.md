# 收货地址管理 - 设计方案

## 一、需求概述

- 用户维护收货地址用于实物奖品发货

## 二、接口设计

- GET `/address/page`
- GET `/address/{id}`
- POST `/address`
- PUT `/address`
- DELETE `/address/{id}`

## 三、字段建议

- id、userId、recipientName、phone、region、addressLine、zip、isDefault、createdAt、updatedAt

## 四、前端页面

- 路由与组件键名：`/address` → `AddressManagement`
- 功能：列表、新增、编辑、删除、默认地址设置

## 五、发货流程对接

- 奖励发放为实物时校验用户是否有默认地址
- 在发放记录中保存地址快照以便追踪

