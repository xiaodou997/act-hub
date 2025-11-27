// src/api/role.js
import request from '@/api/request'
import { createApi } from '@/api/baseApi'

export const roleApi = {
  ...createApi('/role'),

  list() {
    return request({ url: '/role/list', method: 'get' })
  },

  getPermissionIds(roleId) {
    return request({ url: `/role/${roleId}/permissions`, method: 'get' })
  },

  getPermissions(roleId) {
    return request({ url: `/role/${roleId}/permissions/detail`, method: 'get' })
  },

  assignPermissions(roleId, permissionIds) {
    return request({
      url: '/role/permissions',
      method: 'post',
      data: { roleId, permissionIds },
    })
  },
}
