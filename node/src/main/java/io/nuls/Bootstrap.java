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
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
            String profile = prop.getProperty(NulsConstant.SPRING_PROFILE);
            String language = prop.getProperty(NulsConstant.SYSTEM_LANGUAGE);
            String databaseType = prop.getProperty(NulsConstant.DATABASE_TYPE);
            I18nUtils.setLanguage(language);
            //load spring context
            boolean result = loadSpringContext(profile);
            if (!result) {
                break;
            }
            //init modules
            initDB(databaseType);

            initMQ();
            //init rpc server
            result = initRpcServer();
            if (!result) {
                break;
            }
            Log.info("");
        } while (false);
    }

    private static void initDB(String databaseType) {
        DBModule dbModule = NulsContext.getApplicationContext().getBean(DBModule.class);
        Map<String,String> map = new HashMap<>();
        map.put("databaseType", databaseType);
        dbModule.init(map);

        IBlockStore blockStore = (IBlockStore) NulsContext.getApplicationContext().getBean("blockStore");
        long count = blockStore.count();
        System.out.println("-------------count:" + count);
    }

    private static void initMQ() {
        MQModule module = NulsContext.getApplicationContext().getBean(MQModule.class);
        module.start();
        Log.info(module.getInfo());
    }

    /**
     *
     * @return 启动结果
     */
    private static boolean initRpcServer() {
        RpcServerModule module = NulsContext.getApplicationContext().getBean(RpcServerModule.class);
        module.start();
        Log.info(module.getInfo());
        return true;
    }

    /**
     * start spring
     */
    private static boolean loadSpringContext(String profile) {
        //加载spring环境
        Log.info("get application context");
        boolean result = false;
        try {
            ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
            ctx.getEnvironment().setActiveProfiles(profile);
            List<String> filePath = new ArrayList<>();
            filePath.add("classpath:/applicationContext.xml");
            ctx.setConfigLocations(filePath.toArray(new String[]{}));
            ctx.refresh();
            NulsContext.setApplicationContext(ctx);
            Log.info("System is started!");
            result = true;
        } catch (Exception e) {
            Log.error("", e);
        }
        //RpcServerService intf = applicationContext.getBean(RpcServerService.class);
        return result;
    }

}
