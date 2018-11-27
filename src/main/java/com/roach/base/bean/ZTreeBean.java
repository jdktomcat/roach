package com.roach.base.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 树状结构信息类
 *
 * @author jdktomcat
 */
@Data
public class ZTreeBean {

    private String id;

    @JSONField(name = "pId")
    private String pId;

    private String name;

    private String pattern;

    private Integer index;

    private Integer page;

    private Long count;

    private String icon;

    private boolean checked = false;

    @JSONField(name = "isParent")
    private boolean isParent;

    public ZTreeBean() {
    }

    public ZTreeBean(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ZTreeBean(String id, String pId, String name) {
        this.id = id;
        this.pId = pId;
        this.name = name;
    }

}
