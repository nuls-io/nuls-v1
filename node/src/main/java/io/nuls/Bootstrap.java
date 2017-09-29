package io.nuls;

import io.nuls.global.constant.NulsConstant;
import io.nuls.global.NulsContext;
import io.nuls.mq.MQModule;
import io.nuls.rpcserver.intf.RpcServerModule;
import io.nuls.util.cfg.ConfigLoader;
import io.nuls.util.cfg.I18nUtils;
import io.nuls.util.log.Log;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
            I18nUtils.setLanguage(language);
            //load spring context
            boolean result = loadSpringContext(profile);
            if (!result) {
                break;
            }
            //init modules
            initDB();

            initMQ();
            //init rpc server
            result = initRpcServer();
            if (!result) {
                break;
            }
            Log.info("");
        } while (false);
    }

    private static void initDB() {
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
