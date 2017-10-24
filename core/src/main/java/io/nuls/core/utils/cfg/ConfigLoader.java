package io.nuls.core.utils.cfg;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.str.StringUtils;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public abstract class ConfigLoader {

    private static Ini config = null;

//    public static Properties loadProperties(String fileName) throws IOException {
//        InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(fileName);
//        Properties prop = new Properties();
//        prop.load(is);
//        return prop;
//    }

    public static void loadIni(String fileName) throws IOException {
        Config cfg = new Config();
        URL url = ConfigLoader.class.getClassLoader().getResource(fileName);
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        ini.load(url);
        config = ini;
    }

    public static String getCfgValue(String section, String key) throws NulsException {
        Profile.Section ps = config.get(section);
        if (null == ps) {
            throw new NulsException(ErrorCode.CONFIGURATION_ITEM_DOES_NOT_EXIST);
        }
        String value = ps.get(key);
        if (StringUtils.isBlank(value)) {
            throw new NulsException(ErrorCode.CONFIGURATION_ITEM_DOES_NOT_EXIST);
        }
        return value;
    }

    public static Profile.Section getSection(String section) throws NulsException {
        Profile.Section ps = config.get(section);
        if (null == ps) {
            throw new NulsException(ErrorCode.CONFIGURATION_ITEM_DOES_NOT_EXIST);
        }
        return ps;
    }
}
