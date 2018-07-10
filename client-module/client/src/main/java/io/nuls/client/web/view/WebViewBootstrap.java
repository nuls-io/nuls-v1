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

import io.nuls.client.rpc.constant.RpcConstant;
import io.nuls.kernel.cfg.NulsConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * 印链桌面客户端（钱包）
 *
 * @author ln
 */
public class WebViewBootstrap extends Application implements Runnable, ActionListener {

    private static final Logger log = LoggerFactory.getLogger(WebViewBootstrap.class);

    private static final String APP_ICON = "/image/tray.png";
    private static final String APP_TITLE = "NULS";

    private boolean hideTip;

    private TrayIcon trayIcon;
    private Stage stage;

    @Override
    public void run() {
        startWebView(null);
    }

    //    /**
//     * 程序入口
//     */
    public void startWebView(String[] args) {
        String os = System.getProperty("os.name").toUpperCase();
        if (!os.startsWith("WINDOWS") && !os.startsWith("MAC OS")) {
            return;
        }
        launch(args);
    }

    //    /**
//     * 启动方法
//     */
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

        openBrowse();

    }

    //    /**
//     * 停止
//     */
    @Override
    public void stop() throws Exception {
        System.exit(0);
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
            URL resource = this.getClass().getResource(APP_ICON);
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
        MenuItem itemShow = new MenuItem("Show Wallet");
        itemShow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openBrowse();
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

    //    /**
//     * 事件监听处理
//     */
    @Override
    public void actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();

        if ("db_click_tray".equals(command) && !stage.isIconified()) {
            //双击托盘，显示窗体
            //多次使用显示和隐藏设置false
            if (stage.isShowing()) {
                hide();
            } else {
                openBrowse();
            }
        }
    }

    //    /**
//     * 显示窗口
//     */
    public void openBrowse() {
        String ip = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_IP, RpcConstant.DEFAULT_IP);
        int port = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_PORT, RpcConstant.DEFAULT_PORT);
        String url = "http://" + ip + ":" + port;
        openURL(url);
    }

    public static void openURL(String url) {
        try {
            browse(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void browse(String url) throws Exception {
        // 获取操作系统的名字
        String osName = System.getProperty("os.name", "");
        if (osName.startsWith("Mac OS")) {
            // 苹果
            Class fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL",
                    new Class[]{String.class});
            openURL.invoke(null, new Object[]{url});
        } else if (osName.startsWith("Windows")) {
            // windows
            Runtime.getRuntime().exec(
                    "rundll32 url.dll,FileProtocolHandler " + url);
        } else {
            // Unix or Linux
            String[] browsers = {"firefox", "opera", "konqueror", "epiphany",
                    "mozilla", "netscape"};
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++) {
                // 执行代码，在brower有值后跳出，
                // 这里是如果进程创建成功了，==0是表示正常结束。
                if (Runtime.getRuntime()
                        .exec(new String[]{"which", browsers[count]})
                        .waitFor() == 0) {
                    browser = browsers[count];
                }
            }
            if (browser == null) {
                throw new Exception("Could not find web browser");
            } else {
                // 这个值在上面已经成功的得到了一个进程。
                Runtime.getRuntime().exec(new String[]{browser, url});
            }
        }
    }


    //    /**
//     * 隐藏窗口
//     */
    public void hide() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                stage.hide();
            }
        });
    }

    //    /**
//     * 程序退出
//     */
    public void exit() {
        SystemTray.getSystemTray().remove(trayIcon);
        Platform.exit();
    }
}