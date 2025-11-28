<template>
  <div class="page-container">
    <div class="page-header">
      <h2>收货地址管理</h2>
      <el-button type="primary" @click="openCreate">新增地址</el-button>
    </div>
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" width="60" label="#" />
        <el-table-column prop="recipientName" label="收件人" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="region" label="地区" min-width="180" show-overflow-tooltip />
        <el-table-column prop="addressLine" label="详细地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="zip" label="邮编" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" align-center>
      <el-form :model="formData" label-width="100px">
        <el-form-item label="收件人">
          <el-input v-model="formData.recipientName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="地区">
          <el-input v-model="formData.region" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="formData.addressLine" />
        </el-form-item>
        <el-form-item label="邮编">
          <el-input v-model="formData.zip" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addressApi } from '@/api/address'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增地址')
const formData = reactive({})

const loadData = async () => {
  loading.value = true
  try {
    const { records = [] } = await addressApi.getPage({ pageNum: 1, pageSize: 100 })
    tableData.value = records
  } catch { ElMessage.error('获取地址失败') } finally { loading.value = false }
}

const openCreate = () => { dialogTitle.value = '新增地址'; Object.assign(formData, { id: '', recipientName: '', phone: '', region: '', addressLine: '', zip: '' }); dialogVisible.value = true }
const openEdit = (row) => { dialogTitle.value = '编辑地址'; Object.assign(formData, row); dialogVisible.value = true }

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该地址吗？', '确认删除', { type: 'warning' }).then(async () => {
    try { await addressApi.delete(row.id); ElMessage.success('删除成功'); loadData() } catch { ElMessage.error('删除失败') }
  })
}

const handleSubmit = async () => {
  try {
    if (formData.id) { await addressApi.update(formData) } else { await addressApi.create(formData) }
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadData()
  } catch { ElMessage.error('提交失败') }
}

onMounted(loadData)
</script>
<style scoped lang="scss">
.page-header{display:flex;justify-content:space-between;align-items:center}
.table-card{margin-top:16px}
</style>

