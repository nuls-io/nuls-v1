/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to retrieve property values from the ethereumj.conf files
 * <p>
 * The properties are taken from different sources and merged in the following order
 * (the config option from the next source overrides option from previous):
 * - resource ethereumj.conf : normally used as a reference config with default values
 * and shouldn't be changed
 * - system property : each config entry might be altered via -D VM option
 * - [user dir]/config/ethereumj.conf
 * - config specified with the -Dethereumj.conf.file=[file.conf] VM option
 * - CLI options
 *
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
public class SystemProperties {

    private static SystemProperties CONFIG;
    private static boolean useOnlySpringConfig = false;

    /**
     * Returns the static config instance. If the config is passed
     * as a Spring bean by the application this instance shouldn't
     * be used
     * This method is mainly used for testing purposes
     * (Autowired fields are initialized with this static instance
     * but when running within Spring context they replaced with the
     * bean config instance)
     */
    public static SystemProperties getDefault() {
        return useOnlySpringConfig ? null : getSpringDefault();
    }

    static SystemProperties getSpringDefault() {
        if (CONFIG == null) {
            CONFIG = new SystemProperties();
        }
        return CONFIG;
    }

    /**
     * Marks config accessor methods which need to be called (for value validation)
     * upon config creation or modification
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ValidateMe {
    }

    private Config config;

    public SystemProperties() {
        Map<String, Object> values = new HashMap<>();
        values.put("cache.flush.writeCacheSize", "");
        values.put("cache.flush.blocks", "");
        values.put("cache.flush.shortSyncFlush", "");
        values.put("cache.stateCacheSize", "");
        config = ConfigFactory.parseMap(values);
    }

    public Config getConfig() {
        return config;
    }

    @ValidateMe
    public int databasePruneDepth() {
        return 192;
    }

    public int getDatabaseMaxOpenFiles() {
        return 1;
    }

    @ValidateMe
    public String getKeyValueDataSource() {
        return "";
    }

    @ValidateMe
    public String getCryptoProviderName() {
        return "SC";
    }

    @ValidateMe
    public String getHash256AlgName() {
        return "ETH-KECCAK-256";
    }

    @ValidateMe
    public String getHash512AlgName() {
        return "ETH-KECCAK-512";
    }

}
