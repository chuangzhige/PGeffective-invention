package com.cz.admin.entity;

import java.util.List;

public class UserProfileDTO {
     private String userId;
    private String openId;
    private String name;
    private String email;
    private String mobile;
    private String avatar;
    private List<String> departmentIds;
    private String jobTitle;
    private String employeeNo;
    private boolean manager;
    private String status;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public List<String> getDepartmentIds() { return departmentIds; }
    public void setDepartmentIds(List<String> departmentIds) { this.departmentIds = departmentIds; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

   public boolean isManager() { 
    return manager;
}
    public void setManager(boolean manager) {  // 参数名与字段名一致
    this.manager = manager;  // 正确引用实际字段
}
}