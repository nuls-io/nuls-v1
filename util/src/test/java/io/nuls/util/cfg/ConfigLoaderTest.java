package io.nuls.util.cfg;
import org.ini4j.Profile;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by Niels on 2017/10/9.
 * nuls.io
 */
public class ConfigLoaderTest {
    @Test
    public void test() {
        try {
            ConfigLoader.loadIni("cfg.properties");
            assertTrue(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}