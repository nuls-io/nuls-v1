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
package io.nuls.kernel.i18n;

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 国际化工具，可以根据系统设置的语言把信息编码转换为可读的不同语言的字符串
 * Internationalized tools can convert information encoding into
 * a readable string of different languages, depending on the language set by the system.
 *
 * @author Niels
 */
public class I18nUtils {
    /**
     * 语言包池、内含所有配置的语言包数据
     * The language pool contains all the configured language package data.
     */
    private static final Map<String, Properties> ALL_MAPPING = new HashMap<>();

    /**
     * 系统当前已选择语言包
     * The system currently selects the language package.
     */
    private static Properties nowMapping = new Properties();

    /**
     * 默认语言设置为英文
     * default language is English
     */
    private static String key = "en";

    /**
     * 默认语言包存放文件夹名称
     * default properties file folder
     */
    private static final String FOLDER = "languages";

    /**
     * 第一次调用前，加载所有语言包
     * Load all the language packages before the first call.
     */
    static {
        //load all language properties
        try {
            URL furl = I18nUtils.class.getClassLoader().getResource(FOLDER);
            if (null != furl) {
                File folderFile = new File(furl.getPath());
                for (File file : folderFile.listFiles()) {
                    InputStream is = new FileInputStream(file);
                    Properties prop = new Properties();
                    prop.load(new InputStreamReader(is, NulsConfig.DEFAULT_ENCODING));
                    String key = file.getName().replace(".properties", "");
                    ALL_MAPPING.put(key, prop);
                }
            }
        } catch (IOException e) {
            Log.error(e);
        }
    }

//    /**
//     * 设置系统语言，切换语言包
//     * Set up the system language and switch the language package.
//     *
//     * @param lang 语言标识/Language identification
//     */
    public static void setLanguage(String lang) throws NulsException {
        if (StringUtils.isBlank(lang)) {
            throw new NulsException(KernelErrorCode.LANGUAGE_CANNOT_SET_NULL);
        }
        key = lang;
        nowMapping = ALL_MAPPING.get(lang);
    }

    /**
     * 根据信息编码获取一条翻译后的消息体
     * Obtain a translated message body based on the information encoding.
     *
     * @param id 信息编码
     * @return String 翻译后的字符串/The translated string.
     */
    public static String get(String id) {
        if (nowMapping == null) {
            nowMapping = ALL_MAPPING.get(key);
        }
        return nowMapping.getProperty(id + "");
    }

    /**
     * 判断是否已加载某个语言包
     * Determines whether a language package has been loaded.
     *
     * @param lang 语言标识/Language identification
     * @return 判断结果/Determine the results
     */
    public static boolean hasLanguage(String lang) {
        return ALL_MAPPING.containsKey(lang);
    }

    /**
     * 获取当前系统已设置的语言标识
     * Gets the language id that the current system has set.
     *
     * @return String 语言标识/Language identification
     */
    public static String getLanguage() {
        return key;
    }
}
