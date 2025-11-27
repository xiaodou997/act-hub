<template>
  <div class="order-form-factory">
    <OrderCreateForm
      v-if="!isEdit"
      :form-data="formData"
      @submit="handleSubmit"
      @cancel="handleCancel"
    />
    <OrderUpdateForm v-else :form-data="formData" @submit="handleSubmit" @cancel="handleCancel" />
  </div>
</template>

<script setup>
import OrderCreateForm from './OrderCreateForm.vue'
import OrderUpdateForm from './OrderUpdateForm.vue'

const emit = defineEmits(['submit', 'cancel'])

defineProps({
  formData: {
    type: Object,
    required: true,
    default: () => ({}),
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
})

const handleSubmit = (data) => {
  // 可以在这里添加额外的处理逻辑
  emit('submit', data)
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.order-form-factory {
  height: 100%;
}
</style>
