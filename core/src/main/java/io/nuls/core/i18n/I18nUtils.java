package io.nuls.core.i18n;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
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
    private static Properties nowMapping;
    //default language is English
    private static String key = "en";
    //default properties file folder
    private static final String FOLDER = "languages";

    static {
        //load all language properties
        try {
            URL furl = I18nUtils.class.getClassLoader().getResource(FOLDER);
            File folderFile = new File(furl.getPath());
            for (File file : folderFile.listFiles()) {
                InputStream is = new FileInputStream(file);
                Properties prop = new Properties();
                prop.load(new InputStreamReader(is, NulsContext.DEFAULT_ENCODING));
                String key = file.getName().replace(".properties", "");
                ALL_MAPPING.put(key, prop);
            }
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
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
