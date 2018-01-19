/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.utils;

import io.nuls.consensus.entity.genesis.GenesisBlock;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;

import java.io.*;

/**
 * @author Niels
 * @date 2017/12/26
 */
public class StringFileLoader {

    public static String read(String path) throws NulsException {
        AssertUtil.canNotEmpty(path, ErrorCode.NULL_PARAMETER.getMsg());
        String filePath = StringFileLoader.class.getClassLoader().getResource(path).getPath();
        return readRealPath(filePath, false);
    }

    public static String readRealPath(String realPath, boolean format) throws NulsException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(realPath));
        } catch (FileNotFoundException e) {
            Log.error(e);
            throw new NulsException(e);
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
            throw new NulsException(e);
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
