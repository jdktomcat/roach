package com.roach.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.roach.base.bean.Connect;
import com.roach.base.bean.KeyBean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON 工具类
 *
 * @author jdktomcat
 */
public class JsonUtil {

    private static final Pattern pattern = Pattern.compile("\\p{C}");

    public static KeyBean parseKeyBeanObject(String json) {
        try {
            if (!json.equals("")) {
                KeyBean keyBean = JSON.parseObject(json, KeyBean.class);
                return keyBean;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String unicodeToString(String unicode) {
        StringBuffer string = new StringBuffer();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            // 转换出每一个代码点
            int data = Integer.parseInt(hex[i], 16);
            // 追加成string
            string.append((char) data);
        }
        return string.toString();
    }

    public static String stringToUnicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }
        return unicode.toString();
    }


    public static String getUnicodeToString(String str) {
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            str = str.replace(m.group(), stringToUnicode(m.group()));
        }
        return str;
    }


    public static String getStringToUnicode(String str) {
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            str = str.replace(m.group(), unicodeToString(m.group()));
        }
        return str;
    }

    /**
     * 将JSON中信息转化到Connect对象属性中
     *
     * @param data    JSON数据
     * @param connect 连接信息
     */
    public static void copyConnPropertyJson(JSONObject data, Connect connect) {
        connect.setText(data.getString("text"));
        connect.setType(data.getString("type"));
        connect.setMode(data.getString("isha"));
        connect.setRedisHost(data.getString("rhost"));
        connect.setRedisPort(data.getString("rport"));
        connect.setRedisPass(data.getString("rpass"));
        connect.setSshHost(data.getString("shost"));
        connect.setSshPort(data.getString("sport"));
        connect.setSshName(data.getString("sname"));
        connect.setSshPass(data.getString("spass"));
    }

    public static void main(String[] args) throws Exception {
        String str = "admin-menu-key:haha\u000B\u0000\u0005t\u0000\u0005menus";
        System.out.println(getUnicodeToString(str));
        System.out.println(getStringToUnicode(getUnicodeToString(str)));
    }
}