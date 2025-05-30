package com.cz.admin.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(length = 255)
    private String avatar;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.COMMON;
    
    @Column(length = 255)
    private String background;
    
    @Column(length = 255)
    private String github;
    
    @Column(length = 100)
    private String feishuOpenId;  // 飞书OpenId
    
    @Column(length = 100)
    private String feishuUserId;  // 飞书用户ID
    
    @Column(length = 20)
    private String mobile;        // 手机号
    
    @Column(length = 100)
    private String realName;      // 真实姓名
    
    @Column(length = 500)
    private String departmentIds; // 部门ID列表(JSON字符串)
    
    @Column
    private Boolean isActivated = true; // 是否激活
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 用户角色枚举
     */
    public enum Role {
        ADMIN("创智管理员"),
        CZ_MEMBER("创智成员"), 
        COMMON("普通用户");
        
        private final String description;
        
        Role(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 