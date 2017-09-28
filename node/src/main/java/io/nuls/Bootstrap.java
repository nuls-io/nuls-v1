package io.nuls;

import io.nuls.constant.CfgConstant;
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
    public static ClassPathXmlApplicationContext applicationContext;

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
            String profile = prop.getProperty(CfgConstant.SpringProfile);
            String dbType = prop.getProperty(CfgConstant.MybatisDbType);
            //load spring context
            boolean result = loadSpringContext(profile, dbType);
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
        MQModule module = applicationContext.getBean(MQModule.class);
        module.start();
        Log.info(module.getInfo());
    }

    /**
     *
     * @return 启动结果
     */
    private static boolean initRpcServer() {
        RpcServerModule module = applicationContext.getBean(RpcServerModule.class);
        module.start();
        Log.info(module.getInfo());
        return true;
    }

    /**
     * start spring
     */
    private static boolean loadSpringContext(String profile, String dbType) {
        //加载spring环境
        Log.info("get application context");
        boolean result = false;
        try {
            ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
            ctx.getEnvironment().setActiveProfiles(profile);
            List<String> filePath = new ArrayList<>();
            filePath.add("classpath:/applicationContext.xml");
            //todo 这里追加数据库配置文件
//            filePath.add("classpath:/database-"+dbType+".xml");
            ctx.setConfigLocations(filePath.toArray(new String[]{}));
            ctx.refresh();
            applicationContext = ctx;
            Log.info("System is started!");
            result = true;
        } catch (Exception e) {
            Log.error("", e);
        }
        //        RpcServerService intf = applicationContext.getBean(RpcServerService.class);
        return result;
    }

}
