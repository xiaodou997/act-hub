import { createApi } from '@/api/baseApi'
import request from '@/api/request'

export const menuApi = {
  ...createApi('/menu'),

  getUserNav() {
    return request({
      url: '/menu/nav',
      method: 'get',
    })
  },

  getMenuTree() {
    return request({
      url: '/menu/tree',
      method: 'get',
    })
  },

  updateStatus(id, status) {
    return request({
      url: `/menu/${id}/status`,
      method: 'put',
      params: { status },
    })
  },

  sort(parentId, orderedIds) {
    return request({
      url: '/menu/sort',
      method: 'put',
      data: { parentId, orderedIds },
    })
  },
}
