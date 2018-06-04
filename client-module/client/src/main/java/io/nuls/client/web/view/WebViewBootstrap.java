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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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


        VBox root = new VBox();
        root.setId("root");
        root.getChildren().addAll(  new Browser());

//        Region root = new Browser();
//        new TestDragListener(stage).enableDrag(root);
        scene = new Scene(root, 800, 560, Color.web("#666970"));
        stage.setScene(scene);


        stage.show();
    }


    public static void startWebView() {
        //todo launch(new String[]{});
    }
}
