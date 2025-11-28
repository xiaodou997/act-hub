import { createApi } from '@/api/baseApi'

const rewardApiBase = createApi('/reward')

export const rewardApi = {
  getPage: (params) => rewardApiBase.getPage(params),
  getById: (id) => rewardApiBase.getById(id),
  create: (data) => rewardApiBase.create(data),
  update: (data) => rewardApiBase.update(data),
  delete: (id) => rewardApiBase.delete(id),
}
