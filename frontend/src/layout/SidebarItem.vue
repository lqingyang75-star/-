<template>
  <template v-if="!menu.children || menu.children.length === 0">
    <el-menu-item :index="menu.path">
      <span>{{ menu.name }}</span>
    </el-menu-item>
  </template>
  <el-sub-menu v-else :index="`dir-${menu.id}`">
    <template #title>
      <span>{{ menu.name }}</span>
    </template>
    <SidebarItem v-for="child in menu.children" :key="child.id" :menu="child" />
  </el-sub-menu>
</template>

<script setup>
defineProps({
  menu: { type: Object, required: true }
})
</script>
