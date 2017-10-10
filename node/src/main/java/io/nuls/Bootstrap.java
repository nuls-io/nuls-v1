package io.nuls;

import io.nuls.db.DBModule;
import io.nuls.db.intf.IBlockStore;
import io.nuls.exception.NulsException;
import io.nuls.global.constant.NulsConstant;
import io.nuls.global.NulsContext;
import io.nuls.mq.MQModule;
import io.nuls.rpcserver.intf.RpcServerModule;
import io.nuls.task.ModuleManager;
import io.nuls.task.NulsModule;
import io.nuls.util.cfg.ConfigLoader;
import io.nuls.util.cfg.I18nUtils;
import io.nuls.util.log.Log;
import io.nuls.util.str.StringUtils;
import sun.security.krb5.Config;

import java.io.IOException;
import java.util.*;

/**
 * System start class
 */
public class Bootstrap {

    public static void main(String[] args) {
        do {
            //load nuls.ini
            try {
                ConfigLoader.loadIni(NulsConstant.CONFIG_FILE);
            } catch (IOException e) {
                Log.error("Client start faild", e);
                break;
            }
            //set system language
            try {
                String language = ConfigLoader.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_LANGUAGE);
                I18nUtils.setLanguage(language);
            } catch (NulsException e) {
                Log.error(e);
            }
            //init modules
//            initDB();
            initMQ();
            //init rpc server
            boolean result = initRpcServer();
            if (!result) {
                break;
            }
            Log.info("");
        } while (false);
    }

    private static boolean initDB() {
        return startModule(NulsConstant.CFG_BOOTSTRAP_DB_MODULE);
    }

    private static boolean initMQ() {
        return startModule(NulsConstant.CFG_BOOTSTRAP_QUEUE_MODULE);
    }

    /**
     * @return 启动结果
     */
    private static boolean initRpcServer() {
       return startModule( NulsConstant.CFG_BOOTSTRAP_RPC_SERVER_MODULE);
    }


    private static boolean startModule(String key) {
        String moduleClass = null;
        try {
            moduleClass = ConfigLoader.getCfgValue(NulsConstant.CFG_BOOTSTRAP_SECTION,key);
        } catch (NulsException e) {
            Log.error(e);
        }
        boolean result = false;
        do {
            if (StringUtils.isBlank(moduleClass)) {
                Log.warn("module cannot start:"+key);
                break;
            }
            Class clazz = null;
            try {
                clazz = Class.forName(moduleClass);
            } catch (ClassNotFoundException e) {
                Log.error(e);
                break;
            }
            NulsModule module = null;
            try {
                module = (NulsModule) clazz.newInstance();
            } catch (InstantiationException e) {
                Log.error(e);
                break;
            } catch (IllegalAccessException e) {
                Log.error(e);
                break;
            }
            module.start();
            ModuleManager.regModule(module.getModuleName(), module);
            Log.info(module.getInfo());
            result = true;
        } while (false);
        return result;
    }

}
