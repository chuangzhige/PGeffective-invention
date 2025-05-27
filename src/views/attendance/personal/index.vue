<template>
  <div>
    <a-card title="多维表格数据展示">
      <a-descriptions bordered column="1">
        <a-descriptions-item label="App ID">{{ appData.appId }}</a-descriptions-item>
        <a-descriptions-item label="App 名称">{{ appData.appName }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ appData.createTime }}</a-descriptions-item>
      </a-descriptions>
      <a-button type="primary" @click="fetchBitableData">刷新数据</a-button>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'

// 假设后端已实现 /api/feishu/bitable/app 接口，返回飞书多维表格应用数据
const appData = ref({
  appId: '',
  appName: '',
  createTime: ''
})

async function fetchBitableData() {
  try {
    const resp = await fetch('/api/feishu/bitable/app')
    const data = await resp.json()
    if (data.success) {
      appData.value = data.data
      message.success('数据获取成功')
    } else {
      message.error('获取失败: ' + (data.msg || '未知错误'))
    }
  } catch (e) {
    message.error('请求异常')
  }
}

// 页面加载时自动获取一次
fetchBitableData()
</script>