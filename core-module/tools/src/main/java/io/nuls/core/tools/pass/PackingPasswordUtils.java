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

package io.nuls.core.tools.pass;

import io.nuls.core.tools.crypto.AESEncrypt;
import io.nuls.core.tools.crypto.EncryptedData;
import io.nuls.core.tools.crypto.Sha256Hash;
import io.nuls.core.tools.log.Log;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.*;
import java.net.URL;

/**
 * Temporary process
 *
 * @author Niels
 * @date 2018/3/31
 */
public class PackingPasswordUtils {

    private static String password;

    public static String read() {
        if (password != null) {
            return password;
        }
        URL url = PackingPasswordUtils.class.getClassLoader().getResource("");
        File file = new File(url.getPath() + "/nuls.info");
        if (!file.exists()) {
            return null;
        }
        try {
            InputStream input = new FileInputStream(file);
            byte[] bytes = new byte[input.available()];
            input.read(bytes);
            input.close();
            password = new String(AESEncrypt.decrypt(bytes, "nulsnulsnuls", "UTF-8"), "UTF-8");
            return password;
        } catch (Exception e) {
            return null;
        }
    }

    public static void write(String password) {
        try {
            PackingPasswordUtils.password = password;
            EncryptedData passBytes = AESEncrypt.encrypt(password.getBytes("UTF-8"), new byte[16], new KeyParameter(Sha256Hash.hash("nulsnulsnuls".getBytes("UTF-8"))));
            URL url = PackingPasswordUtils.class.getClassLoader().getResource("");
            File file = new File(url.getPath() + "/nuls.info");
            OutputStream output = new FileOutputStream(file);
            output.write(passBytes.getEncryptedBytes());
            output.flush();
            output.close();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public static void main(String[] args) {
        write("nuls123123");
        String pwd = read();
        System.out.println(pwd);
    }
}
