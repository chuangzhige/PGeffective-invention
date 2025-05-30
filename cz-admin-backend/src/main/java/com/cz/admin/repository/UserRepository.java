package com.cz.admin.repository;

import com.cz.admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据飞书OpenId查找用户
     */
    Optional<User> findByFeishuOpenId(String feishuOpenId);
    
    /**
     * 根据飞书用户ID查找用户
     */
    Optional<User> findByFeishuUserId(String feishuUserId);
    
    /**
     * 根据角色查找用户列表
     */
    List<User> findByRole(User.Role role);
    
    /**
     * 根据激活状态查找用户列表
     */
    List<User> findByIsActivated(Boolean isActivated);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查飞书OpenId是否存在
     */
    boolean existsByFeishuOpenId(String feishuOpenId);
    
    /**
     * 根据用户名模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 获取用户统计信息
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserRoleStatistics();
} 