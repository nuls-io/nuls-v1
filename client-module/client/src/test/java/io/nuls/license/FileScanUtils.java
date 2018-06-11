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

package io.nuls.license;

import io.nuls.core.tools.io.StringFileLoader;
import io.nuls.core.tools.log.Log;

import java.io.File;
import java.io.FileWriter;

/**
 * @author: Niels Wang
 * @date: 2018/6/5
 */
public class FileScanUtils {

    public static void main(String[] args) {
        String dirPath = "C:\\workspace\\nuls_v2";
        File root = new File(dirPath);
        scanFiles(root);
    }

    private static void scanFiles(File root) {
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanFiles(file);
            } else if (file.getName().endsWith(".java")) {
                executeJavaFile(file);
            }
        }
    }

    private static void executeJavaFile(File javaFile) {
        try {
            String content = StringFileLoader.readRealPath(javaFile.getPath(), true);
            if (content.startsWith("package io.nuls")) {
                String newContent = getMitLicense() + "\n" + content;
                FileWriter writer;
                writer = new FileWriter(javaFile.getPath());
                writer.write(newContent);
                writer.flush();
                writer.close();

            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private static String getMitLicense() {
        StringBuilder str = new StringBuilder();
        str.append("/*");
        str.append("\n");
        str.append(" * MIT License");
        str.append("\n");
        str.append(" *");
        str.append("\n");
        str.append(" * Copyright (c) 2017-2018 nuls.io");
        str.append("\n");
        str.append(" *");
        str.append("\n");
        str.append(" * Permission is hereby granted, free of charge, to any person obtaining a copy");
        str.append("\n");
        str.append(" * of this software and associated documentation files (the \"Software\"), to deal");
        str.append("\n");
        str.append(" * in the Software without restriction, including without limitation the rights");
        str.append("\n");
        str.append(" * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell");
        str.append("\n");
        str.append(" * copies of the Software, and to permit persons to whom the Software is");
        str.append("\n");
        str.append(" * furnished to do so, subject to the following conditions:");
        str.append("\n");
        str.append(" *");
        str.append("\n");
        str.append(" * The above copyright notice and this permission notice shall be included in all");
        str.append("\n");
        str.append(" * copies or substantial portions of the Software.");
        str.append("\n");
        str.append(" *");
        str.append("\n");
        str.append(" * THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR");
        str.append("\n");
        str.append(" * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,");
        str.append("\n");
        str.append(" * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE");
        str.append("\n");
        str.append(" * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER");
        str.append("\n");
        str.append(" * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,");
        str.append("\n");
        str.append(" * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE");
        str.append("\n");
        str.append(" * SOFTWARE.");
        str.append("\n");
        str.append(" *");
        str.append("\n");
        str.append(" */");
        return str.toString();
    }
}
