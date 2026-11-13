<template>
  <el-card shadow="never">
    <el-empty :description="`「${title}」将在后续阶段实现`" />
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '该模块')
</script>
