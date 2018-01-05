package io.nuls.core.utils.cfg;

import java.util.Properties;

/**
 * @author Niels
 * @date 2018/1/5
 */
public class PropertiesEntity {

    private final Properties prop;

    public PropertiesEntity(Properties prop) {
        this.prop = prop;
    }

    public <T> T getPropValue(String name, T defaultValue) {
        if (prop.getProperty(name) == null) {
            return defaultValue;
        }
        String value = prop.getProperty(name);
        return IniEntity.getValueByType(value, defaultValue);
    }
}
