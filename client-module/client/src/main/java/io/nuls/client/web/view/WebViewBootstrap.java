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

import io.nuls.core.tools.log.Log;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author: Niels Wang
 * @date: 2018/6/4
 */
public class WebViewBootstrap extends Application {
    private Scene scene;

    @Override
    public void start(Stage stage) {

//        stage.initStyle(StageStyle.TRANSPARENT);


//        VBox top = new VBox();
//        top.setId("top");
//        top.setPrefSize(800, 50);
//        new TestDragListener(stage).enableDrag(top);

        String url = "http://192.168.1.103:8001/docs";

        VBox root = new VBox();
        root.setId("root");
        root.getChildren().addAll(new Browser(url));

//        Region root = new Browser();
//        new TestDragListener(stage).enableDrag(root);
        scene = new Scene(root, 800, 560, Color.web("#666970"));
        stage.setScene(scene);

        stage.getIcons().add(new Image(
                WebViewBootstrap.class.getResourceAsStream("/image/icon.ico")));

        //添加系统托盘图标.
        SystemTray tray = SystemTray.getSystemTray();
        BufferedImage image = null;
        try {
            image = ImageIO.read(WebViewBootstrap.class
                    .getResourceAsStream("/image/icon.ico"));
        } catch (IOException e) {
            Log.error(e);
        }
        TrayIcon trayIcon = new TrayIcon(image, "自动备份工具");
        trayIcon.setToolTip("自动备份工具");
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            Log.error(e);
        }

        stage.show();
    }


    public static void startWebView() {
        launch(new String[]{});
    }
}
