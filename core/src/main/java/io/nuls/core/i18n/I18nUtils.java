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
package io.nuls.core.i18n;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Niels
 * @date 2017/9/27
 *
 */
public class I18nUtils {

    private static final Map<String, Properties> ALL_MAPPING = new HashMap<>();
    private static Properties nowMapping = new Properties();
    /**
     * default language is English
     */

    private static String key = "en";
    /**
     * default properties file folder
     */
    private static final String FOLDER = "languages";

    static {
        //load all language properties
        try {
            URL furl = I18nUtils.class.getClassLoader().getResource(FOLDER);
            if (null != furl) {
                File folderFile = new File(furl.getPath());
                for (File file : folderFile.listFiles()) {
                    InputStream is = new FileInputStream(file);
                    Properties prop = new Properties();
                    prop.load(new InputStreamReader(is, NulsContext.DEFAULT_ENCODING));
                    String key = file.getName().replace(".properties", "");
                    ALL_MAPPING.put(key, prop);
                }
            }
        } catch (IOException e) {
            Log.error(e);
        }
    }

    public static void setLanguage(String lang) throws NulsException {
        if (StringUtils.isBlank(lang)) {
            throw new NulsException(ErrorCode.LANGUAGE_CANNOT_SET_NULL);
        }
        key = lang;
        nowMapping = ALL_MAPPING.get(lang);
    }

    public static String get(int id) {
        if (nowMapping == null) {
            nowMapping = ALL_MAPPING.get(key);
        }
        return nowMapping.getProperty(id + "");
    }
}
