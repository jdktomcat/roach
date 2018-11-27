package com.roach.base.bean;

import lombok.Data;

/**
 * 缓存键类
 *
 * @author jdktomcat
 */
@Data
public class KeyBean {

    private long ttl;

    private long size;

    private String key;

    private String type;

    private String text;

    private String json;

    private String raws;

    private String hexs;

    private Object data;
}
