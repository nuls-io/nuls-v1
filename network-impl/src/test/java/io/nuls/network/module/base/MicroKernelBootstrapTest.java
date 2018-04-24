package io.nuls.network.module.base;

import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.i18n.I18nUtils;
import io.nuls.core.module.BaseModuleBootstrap;
import io.nuls.core.module.manager.VersionManager;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.cfg.IniEntity;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.manager.QueueManager;
import io.nuls.core.utils.spring.lite.core.ModularServiceMethodInterceptor;
import io.nuls.core.utils.spring.lite.core.SpringLiteContext;

import java.io.IOException;

/**
 * 核心启动,配置写在代码中
 * @author: Charlie
 * @date: 2018/4/24
 */
public class MicroKernelBootstrapTest extends BaseModuleBootstrap {

    private static final MicroKernelBootstrapTest INSTANCE = new MicroKernelBootstrapTest();

    private MicroKernelBootstrapTest() {
        super(NulsConstant.MODULE_ID_MICROKERNEL);
    }

    public static MicroKernelBootstrapTest getInstance() {
        return INSTANCE;
    }
    @Override
    public void init() {
        try {
            NulsConfig.NULS_CONFIG = ConfigLoader.loadIni(NulsConstant.USER_CONFIG_FILE);
            NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConstant.MODULES_CONFIG_FILE);
        } catch (IOException e) {
            Log.error("Client start failed", e);
            throw new NulsRuntimeException(ErrorCode.FAILED, "Client start failed");
        }
        //set system language
        try {
            NulsConfig.DEFAULT_ENCODING = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_DEFAULT_ENCODING);
            String language = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_LANGUAGE);
            I18nUtils.setLanguage(language);
        } catch (NulsException e) {
            Log.error(e);
        }
        SpringLiteContext.init("io.nuls", new ModularServiceMethodInterceptor());
        try {
            VersionManager.start();
        } catch (NulsException e) {
            Log.error(e);
        }


    }

    @Override
    public void start() {
        QueueManager.start();
    }

    @Override
    public void shutdown() {
        QueueManager.shutdown();
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getInfo() {
        return null;
    }
}
