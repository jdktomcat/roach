package com.roach.base.controller;

import com.roach.tool.DataUtil;
import com.roach.tool.RedisUtil;
import com.roach.tool.StringUtil;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

import static com.roach.base.bean.ResultInfo.*;

/**
 * 配置控制类
 *
 * @author jdktomcat
 */
@Component
public class ConfController {

    public String getConfInfo() {
        try {
            Jedis jedis = DataUtil.getCurrentJedisObject();
            if (null != jedis) {
                return getOkByJson(RedisUtil.getRedisConfig(jedis));
            } else {
                return getNoByJson("连接已断开");
            }
        } catch (Exception e) {
            return exception(e);
        }
    }

    public String setConfInfo(String conf) {
        try {
            Jedis jedis = DataUtil.getCurrentJedisObject();
            if (null != jedis) {
                Map<String, String> confMap = new HashMap<>();
                String[] confArray = conf.split("&");
                for (String str : confArray) {
                    confMap.put(StringUtil.getKeyString(str, "="), StringUtil.getValueString(str, "="));
                }
                RedisUtil.setRedisConfig(jedis, confMap);
                return getOkByJson("修改配置成功");
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exceptionByMsgs("服务端不支持修改某些配置项");
        }
    }

}
