package com.roach.tool;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.util.Properties;

/**
 * 邮箱工具类
 *
 * @author jdktomcat
 */
public class MailUtil {

    private static final String MAIL_TIPS = "REDIS-软件使用反馈";
    private static final String FROM_USER = "jdktomcat@163.com";
    private static final String LOOK_USER = "tqvivo@gmail.com";

    /**
     * 发送邮件
     */
    public static boolean sendMail(String user, String msgs) {
        try {
            if (StringUtils.isEmpty(user)) {
                user = "";
            } else {
                user = "[" + user + "]";
            }
            SimpleMailMessage mainMessage = new SimpleMailMessage();
            mainMessage.setFrom(FROM_USER);
            mainMessage.setTo(LOOK_USER);
            mainMessage.setSubject(MAIL_TIPS + user);
            mainMessage.setText(msgs);
            getJavaMailSender().send(mainMessage);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 获取JavaMailSender实例
     */
    private static JavaMailSenderImpl getJavaMailSender() {
        Properties properties = new Properties();
        // 是否显示调试信息(可选)
        properties.put("mail.debug", "false");
        // 是否使用认证
        properties.put("mail.smtp.auth", "true");
        // 超时时间
        properties.put("mail.smtp.timeout ", "5000");
        // 是否使用ssl加密
        properties.put("mail.smtp.starttls.enable", "true");
        // 是否必须要求使用ssl加密
        properties.put("mail.smtp.starttls.required", "true");
        // 设置SSL工厂
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setJavaMailProperties(properties);
        javaMailSender.setPort(465);
        javaMailSender.setHost("smtp.qq.com");
        javaMailSender.setProtocol("smtp");
        javaMailSender.setUsername("vau.ting@qq.com");
        javaMailSender.setPassword("cqovxmueltcuebgf");
        javaMailSender.setDefaultEncoding("UTF-8");
        return javaMailSender;
    }

}
