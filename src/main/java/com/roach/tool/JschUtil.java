package com.roach.tool;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.roach.base.bean.Connect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSH工具类
 *
 * @author jdktomcat
 */
public class JschUtil {

    private static Logger log = LoggerFactory.getLogger("JschUtil");
    
    private static Session session;

    public static void openSSH(Connect connect) throws Exception {
        closeSSH();
        session = new JSch().getSession(connect.getSshName(), connect.getSshHost(), Integer.valueOf(connect.getSshPort()));
        session.setPassword(connect.getSshPass());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        log.info("已使用SSH通道，SSH服务器版本：" + session.getServerVersion());
        int aport = session.setPortForwardingL(55555, connect.getSshHost(), Integer.valueOf(connect.getSshPort()));
        log.info(connect.getRedisHost() + ":" + aport + " -> " + connect.getSshHost() + ":" + connect.getSshPort());
    }

    private static void closeSSH() {
        try {
            if (null != session) {
                session.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
    }

}
