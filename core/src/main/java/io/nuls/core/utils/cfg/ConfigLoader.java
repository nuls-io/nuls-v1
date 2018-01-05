package io.nuls.core.utils.cfg;

import org.ini4j.Config;
import org.ini4j.Ini;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author Niels
 * @date 2017/9/26
 */
public class ConfigLoader {

    public static Properties loadProperties(String fileName) throws IOException {
        InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(fileName);
        Properties prop = new Properties();
        prop.load(is);
        return prop;
    }

    public static IniEntity loadIni(String fileName) throws IOException {
        Config cfg = new Config();
        URL url = ConfigLoader.class.getClassLoader().getResource(fileName);
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        ini.load(url);
        return new IniEntity(ini);
    }

}
