package io.nuls.core.i18n;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.str.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
public class I18nUtils {

    private static final Map<String, Properties> AllMapping = new HashMap<>();
    private static Properties nowMapping;
    //default language is English
    private static String key = "en";
    //default properties file folder
    private static final String folder = "languages";

    static {
        //load all language properties
        try {
            URL furl = I18nUtils.class.getClassLoader().getResource(folder);
            File folderFile = new File(furl.getPath());
            for (File file : folderFile.listFiles()) {
                InputStream is = new FileInputStream(file);
                Properties prop = new Properties();
                prop.load(new InputStreamReader(is, NulsConstant.DEFAULT_ENCODING));
                String key = file.getName().replace(".properties", "");
                AllMapping.put(key, prop);
            }
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
    }

    public static void setLanguage(String _key) throws NulsException {
        if (StringUtils.isBlank(_key)) {
            throw new NulsException(ErrorCode.LANGUAGE_CANNOT_SET_NULL);
        }
        key = _key;
        nowMapping = AllMapping.get(_key);
    }

    public static String get(int id) {
        if (nowMapping == null) {
            nowMapping = AllMapping.get(key);
        }
        return nowMapping.getProperty(id + "");
    }
}
