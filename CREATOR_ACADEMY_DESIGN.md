# 创作者学院 - 设计方案

## 一、需求概述

- 后台新增/编辑/删除/查询图文教程，支持分类管理与上架
- 小程序端展示已上架内容与分类导航

## 二、接口设计

- 文章
  - GET `/admin/article/page`
  - GET `/admin/article/{id}`
  - POST `/admin/article`
  - PUT `/admin/article`
  - DELETE `/admin/article/{id}`
- 分类
  - GET `/admin/article-category/tree`
  - POST `/admin/article-category`
  - PUT `/admin/article-category`
  - DELETE `/admin/article-category/{id}`

## 三、前端页面

- 路由与组件键名
  - `/academy/article` → `ArticleManagement`
  - `/academy/category` → `ArticleCategoryManagement`
- 页面要点
  - 文章：列表筛选、分类选择、内容编辑
  - 分类：树形展示、增删改

## 四、上架与可见性

- 文章状态字段用于控制是否在小程序端展示
- 小程序端按分类加载并显示内容

