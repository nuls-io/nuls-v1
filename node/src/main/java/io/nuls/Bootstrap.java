package io.nuls;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.i18n.I18nUtils;
import io.nuls.core.manager.ModuleManager;
import io.nuls.core.module.NulsModule;
import io.nuls.core.module.service.ModuleService;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.jettyserver.JettyServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * System start class
 */
public class Bootstrap {

    public static void main(String[] args) {
        try {
            sysStart();
//            webStart();
        } catch (Exception e) {
            Log.error(e);
            System.exit(1);
        }
    }

    private static void sysStart() {
        do {
            //load nuls.ini
            try {
                ConfigLoader.loadIni(NulsConstant.USER_CONFIG_FILE);
            } catch (IOException e) {
                Log.error("Client start faild", e);
                throw new NulsRuntimeException(ErrorCode.FAILED,"Client start faild");
            }
            //set system language
            try {
                String language = ConfigLoader.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_LANGUAGE);
                I18nUtils.setLanguage(language);
            } catch (NulsException e) {
                Log.error(e);
            }
            try {
                Properties bootstrapClasses = ConfigLoader.loadProperties(NulsConstant.SYSTEM_CONFIG_FILE);
                initModules(bootstrapClasses);
            } catch (IOException e) {
                Log.error(e);
                throw new NulsRuntimeException(ErrorCode.FAILED,"Client start faild");
            }
            Log.info("");
        } while (false);
        Log.debug("--------------------------------------------");
        Log.debug(ModuleManager.getInstance().getInfo());
        Log.debug("--------------------------------------------");
    }

    private static void webStart() {
        JettyServer.init();
    }

    private static void initModules(Properties bootstrapClasses) {
        List<String> keyList = new ArrayList<>(bootstrapClasses.stringPropertyNames());
        Collections.sort(keyList);
        for (String key : keyList) {
            try {
                short moduleId = Short.parseShort(key);
                NulsModule module = regModule(moduleId, bootstrapClasses.getProperty(key));
                module.start();
            } catch (Exception e) {
                throw new NulsRuntimeException(e);
            }
        }
    }

    private static NulsModule regModule(short id, String moduleClass) {
        if (null == moduleClass) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "module class is null:" + id);
        }
        try {
            NulsModule module = ModuleService.getInstance().loadModule(moduleClass);
            module.setModuleId(id);
            EventManager.refreshEvents();
            return module;
        } catch (ClassNotFoundException e) {
            Log.error(e);
        } catch (IllegalAccessException e) {
            Log.error(e);
        } catch (InstantiationException e) {
            Log.error(e);
        }
        return null;
    }

}
