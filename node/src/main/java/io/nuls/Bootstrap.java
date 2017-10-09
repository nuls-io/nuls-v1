package io.nuls;

import io.nuls.db.DBModule;
import io.nuls.db.intf.IBlockStore;
import io.nuls.global.constant.NulsConstant;
import io.nuls.global.NulsContext;
import io.nuls.mq.MQModule;
import io.nuls.rpcserver.intf.RpcServerModule;
import io.nuls.util.cfg.ConfigLoader;
import io.nuls.util.cfg.I18nUtils;
import io.nuls.util.log.Log;

import java.io.IOException;
import java.util.*;

/**
 * System start class
 */
public class Bootstrap {

    public static void main(String[] args) {
        do {
            //load cfg.properties
            Properties prop;
            try {
                prop = ConfigLoader.loadProperties("cfg.properties");
            } catch (IOException e) {
                Log.error("Client start faild", e);
                break;
            }
            String language = prop.getProperty(NulsConstant.SYSTEM_LANGUAGE);
            I18nUtils.setLanguage(language);
            //init modules
            initDB();

            initMQ();
            //init rpc server
           boolean  result = initRpcServer();
            if (!result) {
                break;
            }
            Log.info("");
        } while (false);
    }

    private static void initDB() {
//        DBModule dbModule = NulsContext.getApplicationContext().getBean(DBModule.class);
//        dbModule.init(null);
//
//        IBlockStore blockStore = (IBlockStore) NulsContext.getApplicationContext().getBean("blockStore");
//        long count = blockStore.count();
//        System.out.println("-------------count:" + count);
    }

    private static void initMQ() {
//        MQModule module = NulsContext.getApplicationContext().getBean(MQModule.class);
//        module.start();
//        Log.info(module.getInfo());
    }

    /**
     *
     * @return 启动结果
     */
    private static boolean initRpcServer() {
//        RpcServerModule module = NulsContext.getApplicationContext().getBean(RpcServerModule.class);
//        module.start();
//        Log.info(module.getInfo());
        return true;
    }

}
