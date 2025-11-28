import request from '@/api/request'

export const articleCategoryApi = {
  getTree: () => request({ url: '/admin/article-category/tree', method: 'get' }),
  create: (data) => request({ url: '/admin/article-category', method: 'post', data }),
  update: (data) => request({ url: '/admin/article-category', method: 'put', data }),
  delete: (id) => request({ url: `/admin/article-category/${id}`, method: 'delete' }),
}
