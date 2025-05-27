import { defHttp } from '@/utils/http/axios'

// 飞书考勤API配置
const FEISHU_API_BASE = 'https://open.feishu.cn/open-apis'

// 飞书应用配置
export const FEISHU_CONFIG = {
  APP_ID: 'cli_a8b80b73f6bc5013',
  APP_SECRET: 'osgCKn8CUYNU3H14fY3X53RQfi5UkxKU'
}

// 打卡流水数据接口
export interface AttendanceFlow {
  user_id: string
  creator_id: string
  location_name: string
  check_time: string
  check_type: string
  comment: string
  record_id: string
  longitude: number
  latitude: number
  ssid?: string
  bssid?: string
  device_id?: string
}

// 打卡流水查询参数
export interface UserFlowsQueryParams {
  user_ids: string[]
  check_time_from: string
  check_time_to: string
}

// 打卡流水查询响应
export interface UserFlowsQueryResponse {
  code: number
  msg: string
  data: {
    user_flow_results: AttendanceFlow[]
  }
}

// 获取tenant_access_token
export function getTenantAccessToken() {
  return defHttp.post<{
    code: number
    msg: string
    tenant_access_token: string
    expire: number
  }>({
    url: '/api/feishu/auth/tenant_access_token',
    data: {
      app_id: FEISHU_CONFIG.APP_ID,
      app_secret: FEISHU_CONFIG.APP_SECRET
    }
  })
}

// 批量查询打卡流水
export function queryUserFlows(params: UserFlowsQueryParams, token: string) {
  return defHttp.post<UserFlowsQueryResponse>({
    url: '/api/feishu/attendance/user_flows/query',
    data: params,
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json; charset=utf-8'
    },
    params: {
      employee_type: 'employee_id',
      include_terminated_user: false
    }
  })
}

// 获取用户打卡记录（封装方法）
export async function getUserAttendanceRecords(
  userIds: string[], 
  startTime: string, 
  endTime: string
) {
  try {
    // 1. 获取access token
    const tokenResponse = await getTenantAccessToken()
    if (tokenResponse.code !== 0) {
      throw new Error(`获取token失败: ${tokenResponse.msg}`)
    }

    // 2. 查询打卡流水
    const flowsResponse = await queryUserFlows({
      user_ids: userIds,
      check_time_from: startTime,
      check_time_to: endTime
    }, tokenResponse.tenant_access_token)

    if (flowsResponse.code !== 0) {
      throw new Error(`查询打卡流水失败: ${flowsResponse.msg}`)
    }

    return flowsResponse.data.user_flow_results
  } catch (error) {
    console.error('获取用户打卡记录失败:', error)
    throw error
  }
}

// 转换打卡记录为本地格式
export function transformAttendanceRecord(flow: AttendanceFlow) {
  const checkTime = new Date(parseInt(flow.check_time) * 1000)
  const date = checkTime.toISOString().split('T')[0]
  const time = checkTime.toTimeString().split(' ')[0]
  
  // 判断打卡类型和状态
  let type = '上班打卡'
  let status = '正常'
  
  const hour = checkTime.getHours()
  if (hour < 12) {
    type = '上班打卡'
    status = hour > 9 ? '迟到' : '正常'
  } else {
    type = '下班打卡'
    status = hour < 17 ? '早退' : '正常'
  }

  return {
    id: flow.record_id,
    userId: flow.user_id,
    date: date,
    checkIn: type === '上班打卡' ? time : '',
    checkOut: type === '下班打卡' ? time : '',
    checkTime: checkTime.toLocaleString('zh-CN'),
    location: flow.location_name || '未知地点',
    type: type,
    status: status,
    workHours: '8.0', // 需要根据实际打卡记录计算
    overtime: '0',
    longitude: flow.longitude,
    latitude: flow.latitude,
    comment: flow.comment || '',
    // 打卡方式相关信息
    ssid: flow.ssid,
    bssid: flow.bssid,
    device_id: flow.device_id
  }
} 