package io.nuls;


import io.nuls.core.MicroKernelBootstrap;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.manager.ModuleManager;
import io.nuls.core.module.service.ModuleService;
import io.nuls.core.utils.log.Log;
import io.nuls.jettyserver.JettyServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * System start class
 *
 * @author Niels
 */
public class Bootstrap {
    private static final ModuleService moduleService = ModuleService.getInstance();

    public static void main(String[] args) {
        Thread.currentThread().setName("Nuls");
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
            MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
            mk.init();
            mk.start();
            initModules();
        } while (false);
        while (true) {
            try {
                //todo 后续启动一个系统监视线程
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            Log.info(ModuleManager.getInstance().getInfo());
            Log.info("--------------------------------------------");
        }
    }

    private static void webStart() {
        JettyServer.init();
    }

    private static void initModules() {
        Map<String, String> bootstrapClasses = null;
        try {
            bootstrapClasses = getModuleBootstrapClass();
        } catch (NulsException e) {
            Log.error(e);
        }
        if (null == bootstrapClasses || bootstrapClasses.isEmpty()) {
            return;
        }
        List<String> keyList = new ArrayList<>(bootstrapClasses.keySet());
        for (String key : keyList) {
            try {
                moduleService.startModule(key, bootstrapClasses.get(key));
            } catch (Exception e) {
                throw new NulsRuntimeException(e);
            }
        }
    }

    private static Map<String, String> getModuleBootstrapClass() throws NulsException {
        Map<String, String> map = new HashMap<>();
        List<String> moduleNameList = NulsContext.MODULES_CONFIG.getSectionList();
        if (null == moduleNameList || moduleNameList.isEmpty()) {
            return map;
        }
        for (String moduleName : moduleNameList) {
            String className = NulsContext.MODULES_CONFIG.getCfgValue(moduleName, NulsConstant.MODULE_BOOTSTRAP_KEY);
            map.put(moduleName, className);
        }
        return map;
    }


}
