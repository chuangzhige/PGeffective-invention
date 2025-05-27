<template>
  <a-card title="个人考勤统计" :loading="loading">
    <template #extra>
      <a-range-picker 
        v-model:value="selectedDateRange" 
        @change="handleDateRangeChange"
        :allowClear="false"
      />
    </template>
    
    <div ref="chartRef" :style="{ width: '100%', height: '400px' }"></div>
    
    <!-- 统计信息 - 横着摆放 -->
    <div class="mt-4">
      <div class="flex justify-around items-center bg-gray-50 p-4 rounded-lg">
        <div v-for="item in data" :key="item.name" class="text-center flex-1">
          <div class="text-2xl font-bold mb-1" :style="{ color: item.color }">
            {{ item.value }}
          </div>
          <div class="text-gray-600 text-sm">{{ item.name }}</div>
        </div>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, type Ref } from 'vue'
import { useECharts } from '@/hooks/web/useECharts'
import dayjs, { type Dayjs } from 'dayjs'

interface AttendanceDataItem {
  value: number
  name: string
  color: string
}

const props = defineProps<{
  loading: boolean
  data: AttendanceDataItem[]
  dateRange: string[]
}>()

const emit = defineEmits<{
  dateChange: [dates: string[]]
}>()

const chartRef = ref<HTMLDivElement | null>(null)
const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>)

// 日期范围选择
const selectedDateRange = ref<[Dayjs, Dayjs]>([
  dayjs(props.dateRange[0]),
  dayjs(props.dateRange[1])
])

// 计算总天数
const totalDays = computed(() => {
  return props.data.reduce((sum, item) => sum + item.value, 0)
})

// 处理日期范围变化
const handleDateRangeChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates) {
    selectedDateRange.value = dates
    emit('dateChange', [
      dates[0].format('YYYY-MM-DD'),
      dates[1].format('YYYY-MM-DD')
    ])
  }
}

// 更新图表
const updateChart = () => {
  if (props.loading) return

  const chartData = props.data.map(item => ({
    value: item.value,
    name: item.name,
    itemStyle: {
      color: item.color
    }
  }))

  setOptions({
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        const percentage = ((params.value / totalDays.value) * 100).toFixed(1)
        return `${params.name}<br/>天数: ${params.value}<br/>占比: ${percentage}%`
      }
    },
    legend: {
      orient: 'vertical',
      right: 20,
      top: 'center',
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '考勤统计',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        data: chartData,
        label: {
          show: true,
          position: 'outside',
          formatter: (params: any) => {
            const percentage = ((params.value / totalDays.value) * 100).toFixed(1)
            return `${params.name}\n${params.value}天 (${percentage}%)`
          },
          fontSize: 11
        },
        labelLine: {
          show: true,
          length: 15,
          length2: 10
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          },
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        animationType: 'scale',
        animationEasing: 'elasticOut',
        animationDelay: (idx: number) => Math.random() * 200
      }
    ]
  })
}

// 监听数据变化
watch(
  () => [props.loading, props.data],
  () => {
    updateChart()
  },
  { immediate: true, deep: true }
)

// 监听日期范围变化
watch(
  () => props.dateRange,
  (newRange) => {
    selectedDateRange.value = [
      dayjs(newRange[0]),
      dayjs(newRange[1])
    ]
  }
)

onMounted(() => {
  updateChart()
})
</script>

<style scoped>
.ant-card :deep(.ant-card-extra) {
  padding: 0;
}
</style> 