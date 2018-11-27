package com.roach.base.bean;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * redis服务器类
 *
 * @author jdktomcat
 */
@Data
public class RedisInfo {

    /**
     * 服务端
     */
    private String server = "";

    /**
     * 客户端
     */
    private String client = "";

    /**
     * 内存
     */
    private String memory = "";

    /**
     * 持久化
     */
    private String persistence = "";

    /**
     * 状态
     */
    private String stats = "";

    /**
     * 关系
     */
    private String replication = "";
    
    /**
     * 处理器
     */
    private String cpu = "";

    /**
     * 集群
     */
    private String cluster = "";

    /**
     * 值空间
     */
    private String keyspace = "";

    /**
     * 客户端用户
     */
    private List<ClientInfo> users = Collections.emptyList();
}
