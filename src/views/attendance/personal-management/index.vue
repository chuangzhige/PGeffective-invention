<template>
  <div class="attendance-management-container">
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧：个人信息 -->
      <div class="lg:col-span-1">
        <!-- 个人基本信息卡片 -->
        <a-card class="mb-4 user-info-card">
          <div class="info-container">
            <a-avatar size="large" :src="userInfo.avatar">
              {{ userInfo.username?.charAt(0) }}
            </a-avatar>
            <div class="info-content">
              <h1>{{ userInfo.username }}</h1>
              <p>{{ userInfo.position }}</p>
            </div>
          </div>
        </a-card>

        <!-- 详细信息卡片 -->
        <a-card title="详细信息" class="mb-4 detail-info-card">
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="用户ID">{{ userInfo.userId }}</a-descriptions-item>
            <a-descriptions-item label="邮箱">{{ userInfo.email }}</a-descriptions-item>
            <a-descriptions-item label="部门">{{ userInfo.department }}</a-descriptions-item>
            <a-descriptions-item label="职位">{{ userInfo.position }}</a-descriptions-item>
            <a-descriptions-item label="注册时间">{{ userInfo.joinDate }}</a-descriptions-item>
            <a-descriptions-item label="角色">
              <a-tag :color="getRoleColor(userInfo.role || '')">
                {{ getRoleText(userInfo.role || '') }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="考勤状态">
              <a-tag :color="userInfo.status === '正常' ? 'green' : 'orange'">
                {{ userInfo.status }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>


      </div>

      <!-- 右侧：考勤管理 -->
      <div class="lg:col-span-2">
        <!-- 统计卡片 -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
          <a-card>
            <a-statistic
              title="本月出勤天数"
              :value="statistics.attendanceDays"
              suffix="天"
              :value-style="{ color: '#3f8600' }"
            />
          </a-card>
          <a-card>
            <a-statistic
              title="迟到次数"
              :value="statistics.lateDays"
              suffix="次"
              :value-style="{ color: '#cf1322' }"
            />
          </a-card>
          <a-card>
            <a-statistic
              title="请假天数"
              :value="statistics.leaveDays"
              suffix="天"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </div>

        <!-- 考勤饼状图 -->
        <PersonalAttendancePie 
          :loading="loading" 
          :data="attendanceData"
          :date-range="dateRange"
          @date-change="handleDateChange"
        />
        
        <!-- 考勤记录表格 -->
        <div class="mt-4">
          <PersonalAttendanceTable 
            :loading="loading" 
            :data="attendanceRecords"
            @refresh="fetchAttendanceData"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import { getUserInfoById, getAllUser } from '@/api/sys/user'
import type { GetUserInfoModel } from '@/api/sys/model/userModel'
import { getUserAttendanceRecords, transformAttendanceRecord } from '@/api/feishu/attendance'
import type { AttendanceFlow } from '@/api/feishu/attendance'

import PersonalAttendancePie from './components/PersonalAttendancePie.vue'
import PersonalAttendanceTable from './components/PersonalAttendanceTable.vue'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const route = useRoute()

// 用户信息
const userInfo = reactive<Partial<GetUserInfoModel & {
  department: string
  position: string
  joinDate: string
  status: string
  employeeId: string
}>>({
  username: '张三',
  employeeId: 'EMP001',
  department: '技术部',
  position: '高级前端工程师',
  joinDate: '2023-01-15',
  status: '正常',
  role: 'CZ_MEMBER',
  avatar: 'https://via.placeholder.com/150',
  userId: '',
  email: ''
})

// 统计数据
const statistics = reactive({
  attendanceDays: 22,
  lateDays: 2,
  leaveDays: 1
})

// 日期范围
const dateRange = ref([
  dayjs().startOf('month').format('YYYY-MM-DD'),
  dayjs().endOf('month').format('YYYY-MM-DD')
])

// 考勤数据
const attendanceData = ref([
  { value: 20, name: '正常出勤', color: '#52c41a' },
  { value: 2, name: '迟到', color: '#faad14' },
  { value: 1, name: '请假', color: '#1890ff' },
  { value: 0, name: '缺勤', color: '#ff4d4f' }
])

// 考勤记录
const attendanceRecords = ref<any[]>([])

// 获取考勤数据
const fetchAttendanceData = async () => {
  if (!userInfo.userId) {
    console.warn('用户ID为空，无法获取考勤数据')
    return
  }

  loading.value = true
  try {
    // 计算时间范围（默认获取最近30天的数据）
    const endTime = Math.floor(Date.now() / 1000).toString()
    const startTime = Math.floor((Date.now() - 30 * 24 * 60 * 60 * 1000) / 1000).toString()
    
    // 从飞书API获取打卡流水
    const flows = await getUserAttendanceRecords(
      [userInfo.userId.toString()], 
      startTime, 
      endTime
    )
    
    // 转换数据格式
    const records = flows.map(transformAttendanceRecord)
    
    // 按日期分组，合并同一天的上下班打卡记录
    const recordsMap = new Map()
    records.forEach(record => {
      const date = record.date
      if (!recordsMap.has(date)) {
        recordsMap.set(date, {
          id: record.id,
          date: date,
          checkIn: '',
          checkOut: '',
          status: '正常',
          workHours: '0',
          overtime: '0',
          location: record.location
        })
      }
      
      const dayRecord = recordsMap.get(date)
      if (record.type === '上班打卡') {
        dayRecord.checkIn = record.checkIn
        dayRecord.status = record.status
      } else if (record.type === '下班打卡') {
        dayRecord.checkOut = record.checkOut
      }
      
      // 计算工作时长
      if (dayRecord.checkIn && dayRecord.checkOut) {
        const checkInTime = dayjs(`${date} ${dayRecord.checkIn}`)
        const checkOutTime = dayjs(`${date} ${dayRecord.checkOut}`)
        const workHours = checkOutTime.diff(checkInTime, 'hour', true)
        dayRecord.workHours = workHours.toFixed(1)
        
        // 计算加班时长（超过8小时算加班）
        const overtime = Math.max(0, workHours - 8)
        dayRecord.overtime = overtime.toFixed(1)
      }
    })
    
    attendanceRecords.value = Array.from(recordsMap.values())
      .sort((a, b) => dayjs(b.date).valueOf() - dayjs(a.date).valueOf())
    
    // 更新统计数据
    updateStatistics()
    
    message.success('考勤数据加载成功')
  } catch (error) {
    console.error('获取考勤数据失败:', error)
    message.error('加载考勤数据失败，请检查网络连接或联系管理员')
    
    // 如果API调用失败，使用模拟数据
    loadMockAttendanceData()
  } finally {
    loading.value = false
  }
}

// 模拟考勤数据（API失败时使用）
const loadMockAttendanceData = () => {
  attendanceRecords.value = [
    {
      id: 1,
      date: dayjs().subtract(1, 'day').format('YYYY-MM-DD'),
      checkIn: '09:00:00',
      checkOut: '18:30:00',
      status: '正常',
      workHours: '8.5',
      overtime: '0.5',
      location: '西溪八方城'
    },
    {
      id: 2,
      date: dayjs().subtract(2, 'day').format('YYYY-MM-DD'),
      checkIn: '09:15:00',
      checkOut: '18:00:00',
      status: '迟到',
      workHours: '8.0',
      overtime: '0',
      location: '西溪八方城'
    },
    {
      id: 3,
      date: dayjs().subtract(3, 'day').format('YYYY-MM-DD'),
      checkIn: '09:00:00',
      checkOut: '19:00:00',
      status: '正常',
      workHours: '9.0',
      overtime: '1.0',
      location: '西溪八方城'
    }
  ]
  updateStatistics()
}

// 更新统计数据
const updateStatistics = () => {
  const currentMonth = dayjs().month()
  const currentYear = dayjs().year()
  
  const monthlyRecords = attendanceRecords.value.filter(record => {
    const recordDate = dayjs(record.date)
    return recordDate.month() === currentMonth && recordDate.year() === currentYear
  })
  
  statistics.attendanceDays = monthlyRecords.length
  statistics.lateDays = monthlyRecords.filter(record => record.status === '迟到').length
  statistics.leaveDays = monthlyRecords.filter(record => record.status === '请假').length
  
  // 更新饼状图数据
  attendanceData.value = [
    { value: statistics.attendanceDays - statistics.lateDays, name: '正常出勤', color: '#52c41a' },
    { value: statistics.lateDays, name: '迟到', color: '#faad14' },
    { value: statistics.leaveDays, name: '请假', color: '#1890ff' },
    { value: 0, name: '缺勤', color: '#ff4d4f' }
  ]
}

// 处理日期范围变化
const handleDateChange = async (dates: string[]) => {
  dateRange.value = dates
  
  if (!userInfo.userId || !dates || dates.length !== 2) {
    return
  }

  loading.value = true
  try {
    // 将日期转换为时间戳
    const startTime = Math.floor(dayjs(dates[0]).valueOf() / 1000).toString()
    const endTime = Math.floor(dayjs(dates[1]).endOf('day').valueOf() / 1000).toString()
    
    // 从飞书API获取指定时间范围的打卡流水
    const flows = await getUserAttendanceRecords(
      [userInfo.userId.toString()], 
      startTime, 
      endTime
    )
    
    // 转换并处理数据
    const records = flows.map(transformAttendanceRecord)
    const recordsMap = new Map()
    
    records.forEach(record => {
      const date = record.date
      if (!recordsMap.has(date)) {
        recordsMap.set(date, {
          id: record.id,
          date: date,
          checkIn: '',
          checkOut: '',
          status: '正常',
          workHours: '0',
          overtime: '0',
          location: record.location
        })
      }
      
      const dayRecord = recordsMap.get(date)
      if (record.type === '上班打卡') {
        dayRecord.checkIn = record.checkIn
        dayRecord.status = record.status
      } else if (record.type === '下班打卡') {
        dayRecord.checkOut = record.checkOut
      }
      
      // 计算工作时长
      if (dayRecord.checkIn && dayRecord.checkOut) {
        const checkInTime = dayjs(`${date} ${dayRecord.checkIn}`)
        const checkOutTime = dayjs(`${date} ${dayRecord.checkOut}`)
        const workHours = checkOutTime.diff(checkInTime, 'hour', true)
        dayRecord.workHours = workHours.toFixed(1)
        
        const overtime = Math.max(0, workHours - 8)
        dayRecord.overtime = overtime.toFixed(1)
      }
    })
    
    attendanceRecords.value = Array.from(recordsMap.values())
      .sort((a, b) => dayjs(b.date).valueOf() - dayjs(a.date).valueOf())
    
    // 更新统计数据（基于选定时间范围）
    updateStatisticsForDateRange(dates)
    
    message.success('考勤数据已更新')
  } catch (error) {
    console.error('获取指定时间范围考勤数据失败:', error)
    message.error('获取考勤数据失败')
  } finally {
    loading.value = false
  }
}

// 根据日期范围更新统计数据
const updateStatisticsForDateRange = (dateRange: string[]) => {
  const records = attendanceRecords.value.filter(record => {
    const recordDate = dayjs(record.date)
    const startDate = dayjs(dateRange[0])
    const endDate = dayjs(dateRange[1])
    return recordDate.isAfter(startDate.subtract(1, 'day')) && recordDate.isBefore(endDate.add(1, 'day'))
  })
  
  statistics.attendanceDays = records.length
  statistics.lateDays = records.filter(record => record.status === '迟到').length
  statistics.leaveDays = records.filter(record => record.status === '请假').length
  
  // 更新饼状图数据
  attendanceData.value = [
    { value: statistics.attendanceDays - statistics.lateDays, name: '正常出勤', color: '#52c41a' },
    { value: statistics.lateDays, name: '迟到', color: '#faad14' },
    { value: statistics.leaveDays, name: '请假', color: '#1890ff' },
    { value: 0, name: '缺勤', color: '#ff4d4f' }
  ]
}



// 获取角色颜色
const getRoleColor = (role: string) => {
  const colorMap: Record<string, string> = {
    'ADMIN': 'red',
    'CZ_MEMBER': 'blue',
    'COMMON': 'default'
  }
  return colorMap[role] || 'default'
}

// 获取角色文本
const getRoleText = (role: string) => {
  const textMap: Record<string, string> = {
    'ADMIN': '创智管理员',
    'CZ_MEMBER': '创智成员',
    'COMMON': '普通用户'
  }
  return textMap[role] || '未知角色'
}

// 根据用户ID加载用户信息
const loadUserInfo = async (userId?: string) => {
  if (userId) {
    try {
      // 使用真实API获取用户信息
      const userData = await getUserInfoById(Number(userId))
      if (userData) {
        // 合并基础用户信息
        Object.assign(userInfo, {
          username: userData.username,
          userId: userData.userId,
          email: userData.email,
          role: userData.role,
          avatar: userData.avatar || 'https://via.placeholder.com/150',
          createdAt: userData.createdAt,
          // 扩展信息（可以从其他来源获取或使用默认值）
          employeeId: `EMP${String(userData.userId).padStart(3, '0')}`,
          department: getDepartmentByRole(userData.role),
          position: getPositionByRole(userData.role),
          joinDate: userData.createdAt ? userData.createdAt.split('T')[0] : '2023-01-01',
          status: '正常'
        })
      }
    } catch (error) {
      console.error('Failed to load user info:', error)
      message.error('获取用户信息失败')
    }
  } else {
    // 如果没有指定userId，尝试获取所有用户列表的第一个用户
    try {
      const allUsers = await getAllUser()
      if (allUsers && allUsers.length > 0) {
        const firstUser = allUsers[0]
        Object.assign(userInfo, {
          username: firstUser.username,
          userId: firstUser.userId,
          email: firstUser.email,
          role: firstUser.role,
          avatar: firstUser.avatar || 'https://via.placeholder.com/150',
          createdAt: firstUser.createdAt,
          employeeId: `EMP${String(firstUser.userId).padStart(3, '0')}`,
          department: getDepartmentByRole(firstUser.role),
          position: getPositionByRole(firstUser.role),
          joinDate: firstUser.createdAt ? firstUser.createdAt.split('T')[0] : '2023-01-01',
          status: '正常'
        })
      }
    } catch (error) {
      console.error('Failed to load users:', error)
    }
  }
}

// 根据角色获取部门
const getDepartmentByRole = (role: string) => {
  const departmentMap: Record<string, string> = {
    'ADMIN': '管理部',
    'CZ_MEMBER': '技术部',
    'COMMON': '业务部'
  }
  return departmentMap[role] || '未知部门'
}

// 根据角色获取职位
const getPositionByRole = (role: string) => {
  const positionMap: Record<string, string> = {
    'ADMIN': '系统管理员',
    'CZ_MEMBER': '高级工程师',
    'COMMON': '普通员工'
  }
  return positionMap[role] || '未知职位'
}

// 页面加载时获取数据
onMounted(() => {
  const userId = route.query.userId as string
  loadUserInfo(userId)
  fetchAttendanceData()
})
</script>

<style scoped>
/* 整体容器样式 */
.attendance-management-container {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(135deg, #87CEEB 0%, #ffffff 100%);
  background-attachment: fixed;
  position: relative;
}

.attendance-management-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

/* 个人信息卡片样式 */
.user-info-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.info-container {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px 0;
}

.info-content {
  flex: 1;
}

.info-content h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: bold;
  background: linear-gradient(135deg, #4682B4, #87CEEB);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.info-content p {
  margin: 5px 0 0 0;
  color: #666;
  font-size: 1rem;
}

/* 详细信息卡片样式 */
.detail-info-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.detail-info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}



/* 右侧内容区域样式 */
:deep(.ant-card) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.ant-card:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

:deep(.ant-card-head) {
  background: transparent;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

:deep(.ant-card-head-title) {
  font-weight: 600;
  color: #333;
}

/* 统计卡片样式 */
.ant-statistic {
  text-align: center;
}

:deep(.ant-statistic-title) {
  color: #666;
  font-weight: 500;
}

:deep(.ant-statistic-content) {
  font-weight: 600;
}

/* 头像样式 */
:deep(.ant-avatar) {
  border: 3px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .attendance-management-container {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .attendance-management-container {
    padding: 12px;
  }
  
  .info-content h1 {
    font-size: 1.25rem;
  }
}
</style> 