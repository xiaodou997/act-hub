<template>
  <page>
    <slot />
  </page>
</template>
<script setup>
// 可加入全局初始化
</script>
<style>
:root{
  --primary:#1a85f2;
}
page{background:#f5f7fa;color:#333}
button.primary{background:var(--primary);color:#fff}
</style>

