package io.nuls.util.cfg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public class ConfigLoader {

    public static Properties loadProperties(String fileName) throws IOException {
        InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(fileName);
        Properties prop = new Properties();
        prop.load(is);
        return prop;
    }
}
