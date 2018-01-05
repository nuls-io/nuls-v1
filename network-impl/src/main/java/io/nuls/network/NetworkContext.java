package io.nuls.network;

import io.nuls.core.utils.cfg.PropertiesEntity;

import java.util.Properties;

/**
 * @author Niels
 * @date 2018/1/5
 */
public class NetworkContext {

    public static PropertiesEntity getNetworkConfig() {
        return NETWORK_CONFIG;
    }

    public static void setNetworkConfig(Properties properties) {
        NETWORK_CONFIG = new PropertiesEntity(properties);
    }

    private static PropertiesEntity NETWORK_CONFIG;
}
