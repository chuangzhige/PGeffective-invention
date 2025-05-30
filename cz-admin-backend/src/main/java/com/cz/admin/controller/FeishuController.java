package com.cz.admin.controller;

import com.cz.admin.service.FeishuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/feishu")
@CrossOrigin(origins = "*")
public class FeishuController {
    
    @Autowired
    private FeishuService feishuService;

    /**
     * 测试飞书连接
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> result = feishuService.testConnection();
        return ResponseEntity.ok(result);
    }

    /**
     * 获取租户访问令牌 (代理接口)
     */
    @PostMapping("/tenant-access-token")
    public ResponseEntity<Map<String, Object>> getTenantAccessToken() {
        try {
            String token = feishuService.getTenantAccessToken();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tenant_access_token", token);
            response.put("message", "成功获取租户访问令牌");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取令牌失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取用户信息 (代理接口)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String userId) {
        Map<String, Object> result = feishuService.getUserInfo(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取考勤记录 (代理接口)
     */
    @PostMapping("/attendance/records")
    public ResponseEntity<Map<String, Object>> getAttendanceRecords(
            @RequestParam String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        // 自动使用默认员工类型（正式员工），前端无需传递
        Map<String, Object> result = feishuService.getAttendanceRecords(userId, startDate, endDate, 1);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取考勤结果记录 - 直接使用用户ID，返回1条记录
     */
    @PostMapping("/attendance/results")
    public ResponseEntity<Map<String, Object>> getAttendanceResults(
            @RequestParam String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        // 直接使用传入的用户ID，不进行任何转换，限制返回1条记录
        Map<String, Object> result = feishuService.getAttendanceResults(userId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    /**
     * Webhook接口
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "received");
        response.put("message", "Webhook payload received successfully");
        response.put("receivedAt", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取API配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("apiVersion", "v1");
        config.put("endpoints", Map.of(
            "test", "/feishu/test",
            "tenantAccessToken", "/feishu/tenant-access-token",
            "userInfo", "/feishu/user/{userId}",
            "attendanceRecords", "/feishu/attendance/records",
            "employeeTypes", "/feishu/employee-types",
            "webhook", "/feishu/webhook",
            "config", "/feishu/config"
        ));
        config.put("status", "active");
        config.put("proxyEnabled", true);
        config.put("corsEnabled", true);
        return ResponseEntity.ok(config);
    }

    /**
     * 获取员工类型说明
     */
    @GetMapping("/employee-types")
    public ResponseEntity<Map<String, Object>> getEmployeeTypes() {
        Map<String, Object> employeeTypes = new HashMap<>();
        employeeTypes.put("1", "正式员工");
        employeeTypes.put("2", "实习生");
        employeeTypes.put("3", "外包");
        employeeTypes.put("4", "劳务");
        employeeTypes.put("5", "顾问");
        
        Map<String, Object> response = new HashMap<>();
        response.put("employee_types", employeeTypes);
        response.put("default", 1);
        response.put("description", "考勤查询时的员工类型参数");
        response.put("usage", "在考勤记录查询时可以指定employeeType参数，默认为1(正式员工)");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "feishu-integration");
        health.put("proxy", "enabled");
        return ResponseEntity.ok(health);
    }
} 