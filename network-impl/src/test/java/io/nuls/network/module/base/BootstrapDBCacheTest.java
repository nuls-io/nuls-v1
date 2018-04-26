package io.nuls.network.module.base;

import io.nuls.consensus.poc.protocol.service.DownloadService;
import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.module.BaseModuleBootstrap;
import io.nuls.core.module.service.ModuleService;
import io.nuls.core.utils.log.Log;
import io.nuls.network.service.NetworkService;
import io.nuls.poc.service.intf.ConsensusService;
import io.nuls.protocol.context.NulsContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/4/23
 */
public class BootstrapDBCacheTest {

    public static void main(String[] args) {
        start(MicroKernelBootstrap1Test.class);
    }

    public static void start(Class<? extends BaseModuleBootstrap> clazz){
        Thread.currentThread().setName("Nuls");
        try {
            sysStart(clazz);
        } catch (Exception e) {
            Log.error(e);
            System.exit(1);
        }
    }
    private static void sysStart(Class<? extends BaseModuleBootstrap> clazz) throws Exception {
        do {
            Method method = clazz.getDeclaredMethod("getInstance");
            BaseModuleBootstrap mk =  (BaseModuleBootstrap)method.invoke(clazz);
            //MicroKernelBootstrapTest mk = MicroKernelBootstrapTest.getInstance();
            mk.init();
            mk.start();
            initModules();
            Thread.sleep(3000);
        } while (false);
        while (true) {
            try {
                //todo 后续启动一个系统监视线程
                Thread.sleep(20000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            if (null != NulsContext.getInstance().getBestBlock()) {
                Log.info("node: " + NulsContext.getServiceBean(NetworkService.class).getAvailableNodes().size()
                                + "), height:{}, threadCount:{}, consensusStatus: {}, downloadStatus: {}",
                        NulsContext.getInstance().getBestBlock().getHeader().getHeight(),
                        Thread.activeCount(), NulsContext.getServiceBean(ConsensusService.class).getConsensusStatus(),
                        NulsContext.getServiceBean(DownloadService.class).getStatus());
            }
        }
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
        ModuleService.getInstance().startModules(bootstrapClasses);
    }

    private static Map<String, String> getModuleBootstrapClass() throws NulsException {
        Map<String, String> map = new HashMap<>();
        List<String> moduleNameList = NulsConfig.MODULES_CONFIG.getSectionList();
        if (null == moduleNameList || moduleNameList.isEmpty()) {
            return map;
        }
        for (String moduleName : moduleNameList) {
            String className = NulsConfig.MODULES_CONFIG.getCfgValue(moduleName, NulsConstant.MODULE_BOOTSTRAP_KEY);
            //只启动 Module Id: 2, 3
            //if(moduleName.equals("db") || moduleName.equals("cache")) {
                map.put(moduleName, className);
            //}
        }
        return map;
    }

}
