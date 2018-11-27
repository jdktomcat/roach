package com.roach.core.desktop;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 启动引导
 *
 * @author jdktomcat
 */
public class AppGuide extends Preloader {

    @Override
    public void start(Stage primaryStage) {
        //加载启动动画
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1.0f);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(progressIndicator, 100, 100));
        primaryStage.show();
    }
}
