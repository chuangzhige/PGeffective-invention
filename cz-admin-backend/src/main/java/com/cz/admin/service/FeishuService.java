package com.cz.admin.service;

import com.cz.admin.config.FeishuConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.attendance.v1.model.*;
import java.util.HashMap;
import com.lark.oapi.core.request.RequestOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FeishuService {
    
    @Autowired
    private FeishuConfig feishuConfig;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 缓存的租户访问令牌
    private String cachedTenantAccessToken;
    private long tokenExpireTime;
    
    /**
     * 创建通用的HTTP请求头
     */
    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }
    
    /**
     * 处理API响应
     */
    private Map<String, Object> handleApiResponse(ResponseEntity<String> response, String operation) {
        Map<String, Object> result = new HashMap<>();
        
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode responseNode = objectMapper.readTree(response.getBody());
                int code = responseNode.get("code").asInt();
                
                if (code == 0) {
                    result.put("success", true);
                    result.put("data", responseNode.get("data"));
                    result.put("message", operation + "成功");
                } else {
                    result.put("success", false);
                    result.put("message", operation + "失败: " + responseNode.get("msg").asText());
                    result.put("code", code);
                }
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", operation + "失败: JSON解析错误");
                result.put("error_type", "JsonParseError");
            }
        } else {
            result.put("success", false);
            result.put("message", operation + "失败: HTTP " + response.getStatusCode());
        }
        
        return result;
    }
    
    /**
     * 处理异常
     */
    private Map<String, Object> handleException(Exception e, String operation) {
        log.error(operation + "异常", e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", operation + "失败: " + e.getMessage());
        result.put("error_type", e.getClass().getSimpleName());
        return result;
    }
    
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
     * 获取考勤结果（返回完整日期范围数据）
     */
    public Map<String, Object> getAttendanceResults(String userId, String startDate, String endDate) {
        try {
            // 计算日期范围天数
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            long expectedDays = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1; // 包含结束日期
            
            // 日期格式转换（YYYY-MM-DD → YYYYMMDD）
            int checkDateFrom = Integer.parseInt(startDate.replace("-", ""));
            int checkDateTo = Integer.parseInt(endDate.replace("-", ""));
            
            log.info("获取用户: {} 从 {} 到 {} 的考勤记录，预期天数: {}", userId, startDate, endDate, expectedDays);
            
            String token = getTenantAccessToken();
            HttpHeaders headers = createHeaders(token);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_ids", new String[]{userId});
            requestBody.put("check_date_from", checkDateFrom);
            requestBody.put("check_date_to", checkDateTo);
            requestBody.put("need_overtime_result", true);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            String url = feishuConfig.getBaseUrl() + "/open-apis/attendance/v1/user_tasks/query?employee_type=employee_no";
            
            log.info("获取考勤任务 URL: {}, 参数: {}", url, requestBody);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            Map<String, Object> result = handleApiResponse(response, "获取考勤结果");
            
            // 直接返回飞书API的原始数据格式，添加日期范围信息和数据一致性验证
            if (result.get("success").equals(true) && result.get("data") != null) {
                JsonNode root = (JsonNode) result.get("data");
                JsonNode taskResults = root.get("user_task_results");
                
                // 验证数据一致性
                int actualRecords = 0;
                if (taskResults != null && taskResults.isArray()) {
                    actualRecords = taskResults.size();
                }
                
                // 构建返回结果，保持飞书API的原始数据结构
                Map<String, Object> finalResult = new HashMap<>();
                finalResult.put("success", true);
                finalResult.put("code", 0);
                finalResult.put("dateRange", startDate + " 至 " + endDate);
                finalResult.put("expectedDays", expectedDays);
                finalResult.put("actualRecords", actualRecords);
                
                // 数据一致性状态
                if (actualRecords == 0) {
                    finalResult.put("dataConsistency", "EMPTY");
                    finalResult.put("consistencyMessage", "未找到考勤记录");
                } else if (actualRecords == expectedDays) {
                    finalResult.put("dataConsistency", "CONSISTENT");
                    finalResult.put("consistencyMessage", String.format("数据一致：预期 %d 天，实际 %d 条记录", expectedDays, actualRecords));
                } else {
                    finalResult.put("dataConsistency", "INCONSISTENT");
                    finalResult.put("consistencyMessage", String.format("数据不一致：预期 %d 天，实际 %d 条记录", expectedDays, actualRecords));
                }
                
                // 直接返回飞书API的原始数据结构
                finalResult.put("data", result.get("data"));
                
                return finalResult;
            } else {
                // API调用失败，返回错误信息
                return result;
            }    
        } catch (java.time.format.DateTimeParseException e) {
            log.error("日期解析失败: {}", e.getMessage());
            return Map.of(
                "success", false,
                "code", 400,
                "msg", "日期格式错误，应为YYYY-MM-DD格式",
                "data", (Object) null
            );
        } catch (NumberFormatException e) {
            log.error("日期格式转换失败: {}", e.getMessage());
            return Map.of(
                "success", false,
                "code", 400,
                "msg", "日期格式错误，应为YYYY-MM-DD",
                "data", (Object) null
            );
        } catch (Exception e) {
            log.error("获取考勤结果异常: {}", e.getMessage());
            return Map.of(
                "success", false,
                "code", 500,
                "msg", "系统错误: " + e.getMessage(),
                "data", (Object) null
            );
        }
    }
    
    /**
     * 获取全员考勤数据
     * 该方法会先获取所有用户列表，然后批量获取考勤数据
     */
    public Map<String, Object> getAllAttendanceData(String date) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 先获取 tenant_access_token
            String tenantAccessToken = getTenantAccessToken();
            
            // 2. 获取部门用户列表（这里简化处理，实际可能需要遍历多个部门）
            String userListUrl = feishuConfig.getBaseUrl() + "/contact/v3/users";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tenantAccessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> userResponse = restTemplate.exchange(
                userListUrl, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode userResponseNode = mapper.readTree(userResponse.getBody());
            
            if (userResponseNode.get("code").asInt() != 0) {
                throw new RuntimeException("获取用户列表失败: " + userResponseNode.get("msg").asText());
            }
            
            // 3. 提取用户ID列表
            List<String> userIds = new ArrayList<>();
            JsonNode users = userResponseNode.get("data").get("items");
            
            if (users != null && users.isArray()) {
                for (JsonNode user : users) {
                    String userId = user.get("user_id").asText();
                    userIds.add(userId);
                }
            }
            
            // 4. 如果没有用户，返回空结果
            if (userIds.isEmpty()) {
                result.put("success", true);
                result.put("data", new ArrayList<>());
                result.put("total", 0);
                result.put("message", "未找到任何用户");
                return result;
            }
            
            // 5. 批量获取考勤数据
            String attendanceUrl = feishuConfig.getBaseUrl() + "/attendance/v1/user_stats_datas/query";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_ids", userIds);
            requestBody.put("start_date", date);
            requestBody.put("end_date", date);
            
            HttpEntity<String> attendanceEntity = new HttpEntity<>(
                mapper.writeValueAsString(requestBody), 
                headers
            );
            
            ResponseEntity<String> attendanceResponse = restTemplate.exchange(
                attendanceUrl, 
                HttpMethod.POST, 
                attendanceEntity, 
                String.class
            );
            
            JsonNode attendanceResponseNode = mapper.readTree(attendanceResponse.getBody());
            
            if (attendanceResponseNode.get("code").asInt() != 0) {
                // 如果考勤数据获取失败，返回用户列表但考勤数据为空
                List<Map<String, Object>> attendanceList = new ArrayList<>();
                
                for (JsonNode user : users) {
                    Map<String, Object> attendanceRecord = new HashMap<>();
                    attendanceRecord.put("userId", user.get("user_id").asText());
                    attendanceRecord.put("employeeName", user.get("name").asText());
                    attendanceRecord.put("date", date);
                    attendanceRecord.put("checkInResult", "Unknown");
                    attendanceRecord.put("checkOutResult", "Unknown");
                    attendanceRecord.put("checkInTime", "");
                    attendanceRecord.put("checkOutTime", "");
                    attendanceRecord.put("workDuration", "");
                    attendanceRecord.put("location", "");
                    attendanceRecord.put("department", user.has("department_ids") ? 
                        user.get("department_ids").get(0).asText() : "");
                    
                    attendanceList.add(attendanceRecord);
                }
                
                result.put("success", true);
                result.put("data", attendanceList);
                result.put("total", attendanceList.size());
                result.put("message", "获取用户列表成功，但考勤数据获取失败: " + 
                    attendanceResponseNode.get("msg").asText());
                return result;
            }
            
            // 6. 处理考勤数据并与用户信息合并
            List<Map<String, Object>> attendanceList = new ArrayList<>();
            JsonNode attendanceData = attendanceResponseNode.get("data").get("user_datas");
            
            // 创建用户信息映射
            Map<String, JsonNode> userMap = new HashMap<>();
            for (JsonNode user : users) {
                userMap.put(user.get("user_id").asText(), user);
            }
            
            if (attendanceData != null && attendanceData.isArray()) {
                for (JsonNode userData : attendanceData) {
                    String userId = userData.get("user_id").asText();
                    JsonNode userInfo = userMap.get(userId);
                    
                    Map<String, Object> attendanceRecord = new HashMap<>();
                    attendanceRecord.put("userId", userId);
                    attendanceRecord.put("employeeName", userInfo != null ? 
                        userInfo.get("name").asText() : "未知员工");
                    attendanceRecord.put("date", date);
                    
                    // 解析考勤记录
                    JsonNode records = userData.get("datas");
                    if (records != null && records.isArray() && records.size() > 0) {
                        JsonNode dayRecord = records.get(0);
                        JsonNode checkInRecord = dayRecord.get("check_in_record");
                        JsonNode checkOutRecord = dayRecord.get("check_out_record");
                        
                        // 签到信息
                        if (checkInRecord != null && !checkInRecord.isNull()) {
                            attendanceRecord.put("checkInResult", checkInRecord.get("result").asText());
                            attendanceRecord.put("checkInTime", checkInRecord.get("time").asText());
                            attendanceRecord.put("location", checkInRecord.get("location_name").asText(""));
                        } else {
                            attendanceRecord.put("checkInResult", "Absent");
                            attendanceRecord.put("checkInTime", "");
                        }
                        
                        // 签退信息
                        if (checkOutRecord != null && !checkOutRecord.isNull()) {
                            attendanceRecord.put("checkOutResult", checkOutRecord.get("result").asText());
                            attendanceRecord.put("checkOutTime", checkOutRecord.get("time").asText());
                        } else {
                            attendanceRecord.put("checkOutResult", "Todo");
                            attendanceRecord.put("checkOutTime", "");
                        }
                        
                        // 计算工作时长
                        String checkInTime = (String) attendanceRecord.get("checkInTime");
                        String checkOutTime = (String) attendanceRecord.get("checkOutTime");
                        if (!checkInTime.isEmpty() && !checkOutTime.isEmpty()) {
                            try {
                                long checkIn = Long.parseLong(checkInTime);
                                long checkOut = Long.parseLong(checkOutTime);
                                double duration = (checkOut - checkIn) / (1000.0 * 60 * 60);
                                attendanceRecord.put("workDuration", String.format("%.1f", duration));
                            } catch (NumberFormatException e) {
                                attendanceRecord.put("workDuration", "");
                            }
                        } else {
                            attendanceRecord.put("workDuration", "");
                        }
                    } else {
                        // 无考勤记录
                        attendanceRecord.put("checkInResult", "Absent");
                        attendanceRecord.put("checkOutResult", "Absent");
                        attendanceRecord.put("checkInTime", "");
                        attendanceRecord.put("checkOutTime", "");
                        attendanceRecord.put("workDuration", "");
                        attendanceRecord.put("location", "");
                    }
                    
                    attendanceRecord.put("department", userInfo != null && userInfo.has("department_ids") ? 
                        userInfo.get("department_ids").get(0).asText() : "");
                    
                    attendanceList.add(attendanceRecord);
                }
            }
            
            result.put("success", true);
            result.put("data", attendanceList);
            result.put("total", attendanceList.size());
            result.put("message", "获取全员考勤数据成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取全员考勤数据失败: " + e.getMessage());
            result.put("error_type", "ServerError");
            throw e;
        }
        
        return result;
    }
    
    /**
     * 批量获取多个用户的考勤结果
     */
    public Map<String, Object> getBatchAttendanceResults(List<String> userIds, String startDate, String endDate) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String tenantAccessToken = getTenantAccessToken();
            String url = feishuConfig.getBaseUrl() + "/attendance/v1/user_stats_datas/query";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_ids", userIds);
            requestBody.put("start_date", startDate);
            requestBody.put("end_date", endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tenantAccessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ObjectMapper mapper = new ObjectMapper();
            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode responseNode = mapper.readTree(response.getBody());
            
            if (responseNode.get("code").asInt() != 0) {
                result.put("success", false);
                result.put("message", "批量获取考勤数据失败: " + responseNode.get("msg").asText());
                result.put("error_code", responseNode.get("code").asInt());
                return result;
            }
            
            result.put("success", true);
            result.put("data", responseNode.get("data"));
            result.put("message", "批量获取考勤数据成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量获取考勤数据失败: " + e.getMessage());
            result.put("error_type", "ServerError");
            throw e;
        }
        
        return result;
    }
    
    /**
     * 获取考勤组列表
     */
    public Map<String, Object> getAttendanceGroups(Integer pageSize) {
        try {
            String token = getTenantAccessToken();
            HttpHeaders headers = createHeaders(token);
            
            // 构建请求URL，添加page_size参数
            String url = feishuConfig.getBaseUrl() + "/open-apis/attendance/v1/groups";
            if (pageSize != null) {
                url += "?page_size=" + pageSize;
            }
            
            log.info("获取考勤组列表，URL: {}", url);
            
            // 使用GET方法，不需要请求体
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
            );
            
            log.info("飞书API响应状态: {}", response.getStatusCode());
            log.info("飞书API响应内容: {}", response.getBody());
            
            return handleApiResponse(response, "获取考勤组列表");
            
        } catch (Exception e) {
            log.error("获取考勤组列表异常", e);
            return handleException(e, "获取考勤组列表");
        }
    }
    
    /**
     * 获取考勤组成员列表
     */
    public Map<String, Object> getAttendanceGroupMembers(String groupId, String employeeType, String deptType, Integer pageSize, Integer memberClockType) {
        try {
            String token = getTenantAccessToken();
            HttpHeaders headers = createHeaders(token);
            
            // 根据飞书官方SDK示例，正确的API路径
            String url = feishuConfig.getBaseUrl() + "/open-apis/attendance/v1/groups/" + groupId + "/users";
            
            // 构建查询参数
            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append("?");
            
            if (employeeType != null) {
                urlBuilder.append("employee_type=").append(employeeType).append("&");
            }
            if (deptType != null) {
                urlBuilder.append("dept_type=").append(deptType).append("&");
            }
            if (pageSize != null) {
                urlBuilder.append("page_size=").append(pageSize).append("&");
            }
            if (memberClockType != null) {
                urlBuilder.append("member_clock_type=").append(memberClockType).append("&");
            }
            
            // 移除最后一个&符号
            if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
                urlBuilder.setLength(urlBuilder.length() - 1);
            }
            
            String finalUrl = urlBuilder.toString();
            log.info("获取考勤组成员列表，URL: {}", finalUrl);
            
            // 使用GET方法，不需要请求体
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
            );
            
            log.info("飞书API响应状态: {}", response.getStatusCode());
            log.info("飞书API响应内容: {}", response.getBody());
            
            return handleApiResponse(response, "获取考勤组成员列表");
            
        } catch (Exception e) {
            log.error("获取考勤组成员列表异常", e);
            return handleException(e, "获取考勤组成员列表");
        }
    }
} 