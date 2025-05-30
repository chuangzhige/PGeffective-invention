package com.cz.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "feishu")
public class FeishuConfig {
    
    /**
     * 飞书应用ID
     */
    private String appId;
    
    /**
     * 飞书应用密钥
     */
    private String appSecret;
    
    /**
     * 飞书API基础URL
     */
    private String baseUrl;
} 