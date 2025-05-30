package com.cz.admin.service;

import com.cz.admin.config.FeishuConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FeishuService {
    
    @Autowired
    private FeishuConfig feishuConfig;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();
    
    // 缓存的租户访问令牌
    private String cachedTenantAccessToken;
    private long tokenExpireTime;
    
    /**
     * 获取租户访问令牌
     */
    public String getTenantAccessToken() {
        try {
            // 检查缓存的token是否有效
            if (cachedTenantAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
                log.info("使用缓存的tenant access token");
                return cachedTenantAccessToken;
            }
            
            log.info("获取新的tenant access token");
            
            // 准备请求参数
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("app_id", feishuConfig.getAppId());
            requestBody.put("app_secret", feishuConfig.getAppSecret());
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 创建请求实体
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            String url = feishuConfig.getBaseUrl() + "/open-apis/auth/v3/tenant_access_token/internal";
            log.info("请求URL: {}", url);
            log.info("请求参数: app_id={}", feishuConfig.getAppId());
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonObject jsonResponse = gson.fromJson(response.getBody(), JsonObject.class);
                
                if (jsonResponse.get("code").getAsInt() == 0) {
                    String token = jsonResponse.get("tenant_access_token").getAsString();
                    int expire = jsonResponse.get("expire").getAsInt();
                    
                    // 缓存token，提前10分钟过期
                    cachedTenantAccessToken = token;
                    tokenExpireTime = System.currentTimeMillis() + (expire - 600) * 1000L;
                    
                    log.info("成功获取tenant access token，有效期: {} 秒", expire);
                    return token;
                } else {
                    String errorMsg = jsonResponse.get("msg").getAsString();
                    log.error("获取token失败: {}", errorMsg);
                    throw new RuntimeException("获取飞书token失败: " + errorMsg);
                }
            } else {
                log.error("HTTP请求失败: {}", response.getStatusCode());
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("获取飞书token异常", e);
            throw new RuntimeException("获取飞书token异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户信息
     */
    public Map<String, Object> getUserInfo(String userId) {
        try {
            log.info("开始获取用户信息, userId: {}", userId);
            String token = getTenantAccessToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            String url = feishuConfig.getBaseUrl() + "/open-apis/contact/v3/users/" + userId;
            log.info("获取用户信息，URL: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            
            log.info("飞书API响应状态: {}", response.getStatusCode());
            log.info("飞书API响应内容: {}", response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // 将JSON字符串转换为Map，避免JsonObject序列化问题
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonResponse = gson.fromJson(response.getBody(), Map.class);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", jsonResponse);
                return result;
            } else {
                log.warn("获取用户信息HTTP状态异常: {}", response.getStatusCode());
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "HTTP请求失败: " + response.getStatusCode());
                return result;
            }
            
        } catch (Exception e) {
            log.error("获取用户信息异常, userId: {}", userId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取用户信息失败: " + e.getMessage());
            result.put("error_type", e.getClass().getSimpleName());
            return result;
        }
    }
    
    /**
     * 获取考勤记录
     */
    public Map<String, Object> getAttendanceRecords(String userId, String startDate, String endDate) {
        // 自动使用默认员工类型：1=正式员工
        return getAttendanceRecords(userId, startDate, endDate, 1);
    }
    
    /**
     * 获取考勤记录 (重载方法，支持指定员工类型)
     */
    public Map<String, Object> getAttendanceRecords(String userId, String startDate, String endDate, Integer employeeType) {
        try {
            String token = getTenantAccessToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            // 构建查询参数 - 根据飞书API文档要求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_ids", new String[]{userId});
            requestBody.put("check_date_from", startDate);
            requestBody.put("check_date_to", endDate);
            requestBody.put("employee_type", employeeType != null ? employeeType : 1); // 默认正式员工
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            String url = feishuConfig.getBaseUrl() + "/open-apis/attendance/v1/user_daily_shifts/query";
            log.info("获取考勤记录，URL: {}", url);
            log.info("查询参数: userId={}, startDate={}, endDate={}, employee_type={}", 
                    userId, startDate, endDate, employeeType != null ? employeeType : 1);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // 将JSON字符串转换为Map，避免JsonObject序列化问题
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonResponse = gson.fromJson(response.getBody(), Map.class);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", jsonResponse);
                return result;
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "获取考勤记录失败: " + response.getStatusCode());
                return result;
            }
            
        } catch (Exception e) {
            log.error("获取考勤记录异常", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取考勤记录失败: " + e.getMessage());
            result.put("error_details", e.getClass().getSimpleName());
            return result;
        }
    }
    
    /**
     * 测试飞书连接
     */
    public Map<String, Object> testConnection() {
        try {
            String token = getTenantAccessToken();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "飞书连接测试成功");
            result.put("token_preview", token.substring(0, Math.min(token.length(), 20)) + "...");
            result.put("timestamp", System.currentTimeMillis());
            result.put("config", Map.of(
                "app_id", feishuConfig.getAppId(),
                "base_url", feishuConfig.getBaseUrl(),
                "app_secret_length", feishuConfig.getAppSecret().length()
            ));
            
            return result;
            
        } catch (Exception e) {
            log.error("飞书连接测试失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "飞书连接测试失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }
    }
    
    /**
     * 获取考勤结果记录 (使用用户任务查询API)
     * 
     * @param userId 用户ID (employee_no)
     * @param startDate 开始日期 (YYYY-MM-DD)
     * @param endDate 结束日期 (YYYY-MM-DD)
     * @return 考勤任务数据
     */
    public Map<String, Object> getAttendanceResults(String userId, String startDate, String endDate) {
        try {
            log.info("获取考勤结果, userId: {}", userId);
            String token = getTenantAccessToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            // 构建查询参数 - 使用用户任务查询API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_ids", new String[]{userId});
            
            // 处理日期参数 - 转换为YYYYMMDD格式
            try {
                // 移除日期中的-，转换为YYYYMMDD格式
                String checkDateFrom = startDate.replace("-", "");
                String checkDateTo = endDate.replace("-", "");
                
                requestBody.put("check_date_from", Integer.parseInt(checkDateFrom));
                requestBody.put("check_date_to", Integer.parseInt(checkDateTo));
            } catch (Exception e) {
                log.warn("日期格式转换失败: {}", e.getMessage());
                // 如果转换失败，尝试直接使用
                requestBody.put("check_date_from", startDate);
                requestBody.put("check_date_to", endDate);
            }
            
            // 添加额外参数
            requestBody.put("need_overtime_result", true);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // 使用用户任务查询API，默认使用employee_no类型
            String url = feishuConfig.getBaseUrl() + "/open-apis/attendance/v1/user_tasks/query?employee_type=employee_no";
            log.info("获取考勤任务 URL: {}", url);
            log.info("请求参数: {}", requestBody);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            
            log.info("响应状态: {}", response.getStatusCode());
            log.info("响应内容: {}", response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonResponse = gson.fromJson(response.getBody(), Map.class);
                
                // 检查API返回码
                if (jsonResponse.get("code") != null) {
                    int code = ((Number) jsonResponse.get("code")).intValue();
                    if (code == 0) {
                        // 处理返回数据，限制为最新1天的结果
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) jsonResponse.get("data");
                        if (data != null && data.get("user_task_results") != null) {
                            @SuppressWarnings("unchecked")
                            java.util.List<Map<String, Object>> taskResults = 
                                (java.util.List<Map<String, Object>>) data.get("user_task_results");
                            
                            int originalCount = taskResults.size();
                            if (originalCount > 1) {
                                // 只保留最新的1天记录（列表中最后一个）
                                java.util.List<Map<String, Object>> limitedResults = 
                                    taskResults.subList(originalCount - 1, originalCount);
                                data.put("user_task_results", limitedResults);
                                log.info("原始考勤结果天数: {}, 限制返回天数: 1", originalCount);
                            }
                        }
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", true);
                        result.put("data", jsonResponse);
                        log.info("成功获取考勤任务结果");
                        return result;
                    } else {
                        log.warn("飞书API错误 - code: {}, msg: {}", code, jsonResponse.get("msg"));
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", false);
                        result.put("message", "飞书API返回错误: " + jsonResponse.get("msg"));
                        result.put("code", code);
                        return result;
                    }
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", jsonResponse);
                return result;
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "HTTP请求失败: " + response.getStatusCode());
                return result;
            }
            
        } catch (Exception e) {
            log.error("获取考勤结果失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取考勤结果失败: " + e.getMessage());
            result.put("error_type", e.getClass().getSimpleName());
            return result;
        }
    }
} 