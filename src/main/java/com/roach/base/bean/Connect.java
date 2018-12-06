package com.roach.base.bean;

import lombok.Data;

/**
 * 连接信息类
 *
 * @author jdktomcat
 */
@Data
public class Connect {

    /**
     * 主键
     */
    private String id;

    /**
     * 说明
     */
    private String text;

    /**
     * 时间
     */
    private String time;

    /**
     * 模式：0-单机 1-集群 2-哨兵
     */
    private String mode;

    /**
     * 类型：0默认，1：ssh
     */
    private String type;

    /**
     * ssh用户名
     */
    private String sshName;

    /**
     * 主机常连接地址
     */
    private String redisHost;

    /**
     * 主机SSH连接地址
     */
    private String sshHost;

    /**
     * redis端口
     */
    private String redisPort;

    /**
     * ssh端口
     */
    private String sshPort;

    /**
     * redis密码
     */
    private String redisPass;

    /**
     * ssh密码
     */
    private String sshPass;

    /**
     * ssh登录私钥
     */
    private String sshPrivateKey;

    /**
     * 是否启用ssl
     */
    private String onSsl;

}
