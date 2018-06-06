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

package io.nuls.client.web.view.controller;

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author: Niels Wang
 * @date: 2018/6/6
 */
public class WebController {

    private final Stage stage;

    public WebController(Stage stage) {
        this.stage = stage;
    }

    public boolean copy(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent cc = new ClipboardContent();
        cc.putString(text.trim());
        clipboard.setContent(cc);
        return true;
    }

    public String download(String info) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Private Key");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(stage);
        if (null == file) {
            return null;
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(info);
            writer.flush();
        } catch (Exception e) {
            Log.error(e);
            return null;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                Log.error(e);
                return null;
            }
        }
        return file.getPath();
    }

    public String httpGet(String url) {
        System.out.println(url);
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            return sb.toString();
        } catch (Exception e) {
            Log.error("Exception occur when send http get request!", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                Log.error(e2);
            }
        }
        return null;
    }
}
