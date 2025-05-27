<template>
  <a-card title="考勤记录" :loading="loading">
    <template #extra>
      <a-space>
        <a-button @click="handleRefresh" size="small">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>

      </a-space>
    </template>

    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      :scroll="{ x: 800 }"
      size="small"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ record.status }}
          </a-tag>
        </template>
        
        <template v-if="column.key === 'workHours'">
          <span :class="getWorkHoursClass(record.workHours)">
            {{ record.workHours }}小时
          </span>
        </template>
        
        <template v-if="column.key === 'overtime'">
          <span v-if="Number(record.overtime) > 0" class="text-purple-600 font-medium">
            {{ record.overtime }}小时
          </span>
          <span v-else class="text-gray-400">-</span>
        </template>
        
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleViewDetail(record)">
              详情
            </a-button>
            <a-button 
              type="link" 
              size="small" 
              danger 
              @click="handleCorrection(record)"
              v-if="canCorrect(record)"
            >
              申请更正
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'

interface AttendanceRecord {
  id: number
  date: string
  checkIn: string
  checkOut: string
  status: string
  workHours: string
  overtime: string
}

const props = defineProps<{
  loading: boolean
  data: AttendanceRecord[]
}>()

const emit = defineEmits<{
  refresh: []
}>()

// 表格列配置
const columns = [
  {
    title: '日期',
    dataIndex: 'date',
    key: 'date',
    width: 120,
    sorter: (a: AttendanceRecord, b: AttendanceRecord) => 
      dayjs(a.date).valueOf() - dayjs(b.date).valueOf(),
    defaultSortOrder: 'descend' as const
  },
  {
    title: '签到时间',
    dataIndex: 'checkIn',
    key: 'checkIn',
    width: 100
  },
  {
    title: '签退时间',
    dataIndex: 'checkOut',
    key: 'checkOut',
    width: 100
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    filters: [
      { text: '正常', value: '正常' },
      { text: '迟到', value: '迟到' },
      { text: '早退', value: '早退' },
      { text: '缺勤', value: '缺勤' },
      { text: '请假', value: '请假' }
    ],
    onFilter: (value: string, record: AttendanceRecord) => record.status === value
  },
  {
    title: '工作时长',
    key: 'workHours',
    width: 100
  },
  {
    title: '加班时长',
    key: 'overtime',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right' as const
  }
]

// 分页配置
const pagination = computed(() => ({
  current: 1,
  pageSize: 10,
  total: props.data.length,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => 
    `第 ${range[0]}-${range[1]} 条/共 ${total} 条`
}))

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    '正常': 'green',
    '迟到': 'orange',
    '早退': 'orange',
    '缺勤': 'red',
    '请假': 'blue',
    '加班': 'purple'
  }
  return colorMap[status] || 'default'
}

// 获取工作时长样式
const getWorkHoursClass = (workHours: string) => {
  const hours = Number(workHours)
  if (hours >= 8) {
    return 'text-green-600 font-medium'
  } else if (hours >= 6) {
    return 'text-orange-600'
  } else {
    return 'text-red-600'
  }
}

// 判断是否可以申请更正
const canCorrect = (record: AttendanceRecord) => {
  const recordDate = dayjs(record.date)
  const today = dayjs()
  const diffDays = today.diff(recordDate, 'day')
  
  // 只能更正7天内的记录
  return diffDays <= 7 && diffDays >= 0
}

// 刷新数据
const handleRefresh = () => {
  emit('refresh')
}



// 查看详情
const handleViewDetail = (record: AttendanceRecord) => {
  message.info(`查看 ${record.date} 的考勤详情`)
}

// 申请更正
const handleCorrection = (record: AttendanceRecord) => {
  message.info(`申请更正 ${record.date} 的考勤记录`)
}
</script>

<style scoped>
:deep(.ant-table-tbody > tr > td) {
  padding: 8px 16px;
}

:deep(.ant-table-thead > tr > th) {
  padding: 12px 16px;
  background-color: #fafafa;
}
</style> 