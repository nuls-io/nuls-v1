package io.nuls.core.utils.cfg;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.str.StringUtils;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Niels
 * @date 2018/1/5
 */
public class IniEntity {

    private final Ini ini;

    public IniEntity(Ini ini) {
        this.ini = ini;
    }


    public String getCfgValue(String section, String key) throws NulsException {
        Profile.Section ps = ini.get(section);
        if (null == ps) {
            throw new NulsException(ErrorCode.CONFIGURATION_ITEM_DOES_NOT_EXIST);
        }
        String value = ps.get(key);
        if (StringUtils.isBlank(value)) {
            throw new NulsException(ErrorCode.CONFIGURATION_ITEM_DOES_NOT_EXIST);
        }
        return value;
    }

    public <T> T getCfgValue(String section, String key, T defaultValue) {
        Profile.Section ps = ini.get(section);
        if (null == ps) {
            return defaultValue;
        }
        String value = ps.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return getValueByType(value, defaultValue);
    }

    protected static <T> T getValueByType(String value, T defaultValue) {
        if (defaultValue instanceof Integer) {
            return (T) ((Integer) Integer.parseInt(value));
        } else if (defaultValue instanceof Long) {
            return (T) ((Long) Long.parseLong(value));
        } else if (defaultValue instanceof Float) {
            return (T) ((Float) Float.parseFloat(value));
        } else if (defaultValue instanceof Double) {
            return (T) ((Double) Double.parseDouble(value));
        } else if (defaultValue instanceof Boolean) {
            return (T) ((Boolean) Boolean.parseBoolean(value));
        }
        return (T) value;
    }

    public Profile.Section getSection(String section) throws NulsException {
        Profile.Section ps = ini.get(section);
        if (null == ps) {
            throw new NulsException(ErrorCode.CONFIGURATION_ITEM_DOES_NOT_EXIST);
        }
        return ps;
    }

    public List<String> getSectionList() {
        Set<Map.Entry<String, Profile.Section>> entrySet = ini.entrySet();
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Profile.Section> entry : entrySet) {
            list.add(entry.getKey());
        }
        return list;
    }
}
