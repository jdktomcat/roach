package com.roach.base.controller;

import com.roach.base.bean.Connect;
import com.roach.base.bean.Setting;
import com.roach.base.service.DataService;
import com.roach.core.desktop.Desktop;
import com.roach.tool.KeyUtil;
import com.roach.tool.MailUtil;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.roach.base.bean.ResultInfo.*;
import static com.roach.tool.DataUtil.getCurrentOpenConnect;
import static com.roach.tool.ItemUtil.*;

/**
 * 其他信息控制类
 *
 * @author jdktomcat
 */
@Component
public class OtherController {

    @Autowired
    private DataService dataService;

    public void changeWebview(int pageNo) {
        Connect connect = getCurrentOpenConnect();
        String pageUrl = "";
        switch (pageNo) {
            case 1:
                pageUrl = PAGE_CONNECT;
                break;
            case 2:
                if (connect.getIsha().equals("0")) {
                    pageUrl = PAGE_DATA_SINGLES;
                }
                if (connect.getIsha().equals("1")) {
                    pageUrl = PAGE_DATA_CLUSTER;
                }
                break;
            case 3:
                if (connect.getIsha().equals("0")) {
                    pageUrl = PAGE_INFO_SINGLES;
                }
                if (connect.getIsha().equals("1")) {
                    pageUrl = PAGE_INFO_CLUSTER;
                }
                break;
            case 4:
                if (connect.getIsha().equals("0")) {
                    pageUrl = PAGE_CONF_SINGLES;
                }
                if (connect.getIsha().equals("1")) {
                    pageUrl = PAGE_CONF_CLUSTER;
                }
                break;
            case 5:
                if (connect.getIsha().equals("0")) {
                    pageUrl = PAGE_MONITOR_SINGLES;
                }
                if (connect.getIsha().equals("1")) {
                    pageUrl = PAGE_MONITOR_CLUSTER;
                }
                break;
        }
        Desktop.setWebViewPage(pageUrl);
    }


    /**
     * 配置连接信息
     */
    public void initSystems() {
        try {
            int tableCount1 = this.dataService.isExistsTable("T_CONNECT");
            if (tableCount1 == 0) {
                this.dataService.createConnectTable();
            }
            int tableCount2 = this.dataService.isExistsTable("T_SETTING");
            if (tableCount2 == 0) {
                this.dataService.createSettingTable();
                //1.初始化默认主题颜色
                Setting setting01 = new Setting();
                setting01.setId(KeyUtil.getUUIDKey());
                setting01.setKeys(SETTING_THEME_COLOR);
                setting01.setVals("#D6D6D7");
                this.dataService.insertSetting(setting01);
                //初始化日志窗口
                //初始化任务栏图标
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getSetting(String keys) {
        return this.dataService.selectSetting(keys).getVals();
    }


    public String setSetting(String keys, String vals) {
        try {
            Setting setting = new Setting();
            setting.setKeys(keys);
            setting.setVals(vals);
            int flag = this.dataService.updateSetting(setting);
            if (flag == 1) {
                switch (keys) {
                    case SETTING_THEME_COLOR:
                        Color backgroundColor = Color.web(vals, 1.0);
                        BackgroundFill backgroundFill = new BackgroundFill(backgroundColor, null, null);
                        Desktop.getTopsView().setBackground(new Background(backgroundFill));
                        break;
                }
                return getOkByJson("修改设置项成功");
            } else {
                return getNoByJson("修改设置项失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return exception(e);
        }
    }

    public String sendMail(String mailAddr, String mailText) {
        try {
            boolean sendFlag = MailUtil.sendMail(mailAddr, mailText);
            if (sendFlag) {
                return getOkByJson("发送邮件成功");
            } else {
                return getNoByJson("发送邮件失败");
            }
        } catch (Exception e) {
            return exception(e);
        }
    }
}
