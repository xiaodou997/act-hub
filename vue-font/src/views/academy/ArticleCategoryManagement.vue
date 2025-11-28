<template>
  <div class="page-container">
    <div class="page-header">
      <h2>教程分类管理</h2>
      <el-button type="primary" @click="openCreate">新建分类</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="treeData" row-key="id" default-expand-all border>
        <el-table-column prop="name" label="分类名称" min-width="240" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" align-center>
      <el-form :model="formData" label-width="90px">
        <el-form-item label="分类名称">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" />
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
import { articleCategoryApi } from '@/api/articleCategory'

const treeData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建分类')
const formData = reactive({})

const loadTree = async () => {
  try { treeData.value = await articleCategoryApi.getTree() || [] } catch { ElMessage.error('获取分类失败') }
}

const openCreate = () => { dialogTitle.value = '新建分类'; Object.assign(formData, { id: '', name: '', description: '' }); dialogVisible.value = true }

const openEdit = async (row) => { dialogTitle.value = '编辑分类'; Object.assign(formData, row); dialogVisible.value = true }

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除分类"${row.name}"吗？`, '确认删除', { type: 'warning' }).then(async () => {
    try { await articleCategoryApi.delete(row.id); ElMessage.success('删除成功'); loadTree() } catch { ElMessage.error('删除失败') }
  })
}

const handleSubmit = async () => {
  try {
    if (formData.id) { await articleCategoryApi.update(formData) } else { await articleCategoryApi.create(formData) }
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadTree()
  } catch { ElMessage.error('提交失败') }
}

onMounted(loadTree)
</script>
<style scoped lang="scss">
.page-header{display:flex;justify-content:space-between;align-items:center}
</style>

