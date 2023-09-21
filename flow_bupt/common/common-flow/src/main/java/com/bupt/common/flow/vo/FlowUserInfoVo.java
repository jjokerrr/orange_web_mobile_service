package com.bupt.common.flow.vo;

import lombok.Data;

import java.util.Date;

/**
 * 流程任务的用户信息。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Data
public class FlowUserInfoVo {

    /**
     * 用户Id。
     */
    private Long userId;

    /**
     * 登录用户名。
     */
    private String loginName;

    /**
     * 用户部门Id。
     */
    private Long deptId;

    /**
     * 用户显示名称。
     */
    private String showName;

    /**
     * 用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)。
     */
    private Integer userType;

    /**
     * 用户头像的Url。
     */
    private String headImageUrl;

    /**
     * 用户状态(0: 正常 1: 锁定)。
     */
    private Integer userStatus;

    /**
     * 最后审批时间。
     */
    private Date lastApprovalTime;

    /**
     * 用户邮箱。
     */
    private String email;

    /**
     * 用户手机。
     */
    private String mobile;
}
