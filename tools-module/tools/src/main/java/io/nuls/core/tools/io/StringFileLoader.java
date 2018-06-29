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
package io.nuls.core.tools.io;

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Niels
 */
public class StringFileLoader {

    public static String read(String path) throws Exception {
        AssertUtil.canNotEmpty(path, "null parameter");
        String filePath = StringFileLoader.class.getClassLoader().getResource(path).getPath();
        return readRealPath(filePath, false);
    }

    public static String readRealPath(String realPath, boolean format) throws Exception {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(realPath));
        } catch (FileNotFoundException e) {
            Log.error(e);
            throw new Exception(e);
        }
        StringBuilder str = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {

                if (format) {
                    str.append(line);
                    str.append("\n");
                } else {
                    str.append(line.trim());
                }
            }
        } catch (IOException e) {
            Log.error(e);
            throw new Exception(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.error(e);
            }
        }
        return str.toString();
    }
}
