/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.client.web.view;

import io.nuls.client.web.view.listener.WindowCloseEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

/**
 * 印链桌面客户端（钱包）
 *
 * @author ln
 */
public class WebViewBootstrap extends Application implements ActionListener {

    private static final Logger log = LoggerFactory.getLogger(WebViewBootstrap.class);

    private static final String APP_ICON = "/image/icon.png";
    private static final String APP_TITLE = "NULS";

    private boolean hideTip;

    private TrayIcon trayIcon;
    private Stage stage;

    /**
     * 程序入口
     */
    public static void startWebView(String[] args) {
        String os = System.getProperty("os.name").toUpperCase();
        if (!os.startsWith("WINDOWS") && !os.startsWith("Mac OS")) {
            return;
        }
        launch(args);
    }

    /**
     * 启动方法
     */
    @Override
    public void start(final Stage stage) throws Exception {

        this.stage = stage;

        //设置程序标题
        stage.setTitle(APP_TITLE);

        stage.setResizable(false);

        //设置程序图标
        stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
        if (isMac()) {
            java.awt.Image dockIcon = new ImageIcon(getClass().getResource(APP_ICON)).getImage();
            try {
                Class<?> cls = Class.forName("com.apple.eawt.Application");
                Object application = cls.newInstance().getClass().getMethod("getApplication").invoke(null);
                application.getClass().getMethod("setDockIconImage", java.awt.Image.class).invoke(application, dockIcon);
            } catch (Exception e) {
            }

        } else {
            //设置程序标题
            stage.setTitle(APP_TITLE);
            //设置程序图标
            stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
        }

        //初始化系统托盘
        initSystemTray();

        //初始化容器
        initContainer();

        //初始化监听器
        initListener();

    }


    private void showRealPage() throws IOException {
        //显示界面
        show();
    }

    /**
     * 停止
     */
    @Override
    public void stop() throws Exception {
        System.exit(0);
    }

    /*
     * 初始化界面容器
     */
    private void initContainer() throws IOException {
        String url = "http://127.0.0.1:8001";

        VBox root = new VBox();
        root.setId("root");
        root.getChildren().addAll(new Browser(url));

//        Region root = new Browser();
//        new TestDragListener(stage).enableDrag(root);
        Scene scene = new Scene(root, 800, 560, Color.web("#666970"));
        stage.setScene(scene);
        show();
    }

    /*
     * 初始化监听器
     */
    private void initListener() {
        //当点击"X"关闭窗口按钮时，如果系统支持托盘，则最小化到托盘，否则关闭
        stage.addEventHandler(WindowCloseEvent.ACTION, event -> {
            if (!(event instanceof WindowCloseEvent)) {
                return;
            }
            if (SystemTray.isSupported()) {
                //隐藏，可双击托盘恢复
                hide();
                if (!hideTip && !isMac()) {
                    hideTip = true;
                    TrayIcon[] trayIcons = SystemTray.getSystemTray().getTrayIcons();
                    if (trayIcons != null && trayIcons.length > 0) {
                        trayIcons[0].displayMessage("温馨提示", "印链客户端已最小化到系统托盘，双击可再次显示", MessageType.INFO);
                    }
                }
            } else {
                //退出程序
                exit();
            }
        });
    }

    private boolean isMac() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.indexOf("mac") != -1;
    }

    /*
     * 初始化系统托盘
     */
    private void initSystemTray() {
        //判断系统是否支持托盘功能
        if (SystemTray.isSupported()) {
            //获得托盘图标图片路径
            URL resource = this.getClass().getResource("/image/icon.png");
            trayIcon = new TrayIcon(new ImageIcon(resource).getImage(), "NULS", createMenu());
            //设置双击动作标识
            trayIcon.setActionCommand("db_click_tray");
            //托盘双击监听
            trayIcon.addActionListener(this);
            //图标自动适应
            trayIcon.setImageAutoSize(true);

            SystemTray sysTray = SystemTray.getSystemTray();
            try {
                sysTray.add(trayIcon);
            } catch (AWTException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /*
     * 创建托盘菜单
     */
    private PopupMenu createMenu() {
        PopupMenu popupMenu = new PopupMenu(); //创建弹出菜单对象

        //创建弹出菜单中的显示主窗体项.
        MenuItem itemShow = new MenuItem("Show");
        itemShow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                show();
            }
        });

        popupMenu.add(itemShow);
        popupMenu.addSeparator();

        //创建弹出菜单中的退出项
        MenuItem itemExit = new MenuItem("Exit");
        popupMenu.add(itemExit);

        //给退出系统添加事件监听
        itemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });


        return popupMenu;
    }

    /**
     * 事件监听处理
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();

        if ("db_click_tray".equals(command) && !stage.isIconified()) {
            //双击托盘，显示窗体
            //多次使用显示和隐藏设置false
            if (stage.isShowing()) {
                hide();
            } else {
                show();
            }
        }
    }

    /**
     * 显示窗口
     */
    public void show() {
        Platform.setImplicitExit(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.show();
            }
        });
    }

    /**
     * 隐藏窗口
     */
    public void hide() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.hide();
            }
        });
    }

    /**
     * 程序退出
     */
    public void exit() {
        SystemTray.getSystemTray().remove(trayIcon);
        Platform.exit();
    }
}