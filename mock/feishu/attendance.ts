import { MockMethod } from 'vite-plugin-mock'

// 模拟飞书打卡流水数据
const mockAttendanceFlows = [
  {
    user_id: 'abd754f7',
    creator_id: 'abd754f7',
    location_name: '西溪八方城',
    check_time: Math.floor(Date.now() / 1000 - 86400).toString(), // 昨天
    check_type: 'work_start',
    comment: '',
    record_id: 'record_001',
    longitude: 120.0947,
    latitude: 30.2780
  },
  {
    user_id: 'abd754f7',
    creator_id: 'abd754f7',
    location_name: '西溪八方城',
    check_time: Math.floor(Date.now() / 1000 - 86400 + 32400).toString(), // 昨天下班
    check_type: 'work_end',
    comment: '',
    record_id: 'record_002',
    longitude: 120.0947,
    latitude: 30.2780
  },
  {
    user_id: 'abd754f7',
    creator_id: 'abd754f7',
    location_name: '西溪八方城',
    check_time: Math.floor(Date.now() / 1000 - 172800 + 900).toString(), // 前天迟到
    check_type: 'work_start',
    comment: '',
    record_id: 'record_003',
    longitude: 120.0947,
    latitude: 30.2780
  },
  {
    user_id: 'abd754f8',
    creator_id: 'abd754f8',
    location_name: '西溪八方城',
    check_time: Math.floor(Date.now() / 1000 - 86400).toString(),
    check_type: 'work_start',
    comment: '',
    record_id: 'record_004',
    longitude: 120.0947,
    latitude: 30.2780
  }
]

export default [
  // 获取tenant_access_token
  {
    url: '/api/feishu/auth/tenant_access_token',
    method: 'post',
    response: () => {
      return {
        code: 0,
        msg: 'success',
        tenant_access_token: 'mock_tenant_access_token_' + Date.now(),
        expire: 7200
      }
    }
  },
  
  // 批量查询打卡流水
  {
    url: '/api/feishu/attendance/user_flows/query',
    method: 'post',
    response: ({ body }) => {
      const { user_ids, check_time_from, check_time_to } = body
      
      // 根据用户ID和时间范围过滤数据
      const filteredFlows = mockAttendanceFlows.filter(flow => {
        const isUserMatch = user_ids.includes(flow.user_id)
        const checkTime = parseInt(flow.check_time)
        const isTimeMatch = checkTime >= parseInt(check_time_from) && checkTime <= parseInt(check_time_to)
        return isUserMatch && isTimeMatch
      })
      
      return {
        code: 0,
        msg: 'success',
        data: {
          user_flow_results: filteredFlows
        }
      }
    }
  }
] as MockMethod[] 