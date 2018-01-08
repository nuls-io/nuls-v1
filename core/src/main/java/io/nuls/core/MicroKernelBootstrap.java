package io.nuls.core;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.i18n.I18nUtils;
import io.nuls.core.module.BaseModuleBootstrap;
import io.nuls.core.tx.serivce.CommonTransactionService;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.manager.QueueManager;

import java.io.IOException;

/**
 * @author Niels
 * @date 2018/1/5
 */
public class MicroKernelBootstrap extends BaseModuleBootstrap {
    private static final MicroKernelBootstrap INSTANCE = new MicroKernelBootstrap();

    private MicroKernelBootstrap() {
        super(NulsConstant.MODULE_ID_MICROKERNEL);
    }

    public static MicroKernelBootstrap getInstance() {
        return INSTANCE;
    }

    @Override
    public void init() {
        try {
            NulsContext.NULS_CONFIG = ConfigLoader.loadIni(NulsConstant.USER_CONFIG_FILE);
            NulsContext.MODULES_CONFIG = ConfigLoader.loadIni(NulsConstant.MODULES_CONFIG_FILE);
        } catch (IOException e) {
            Log.error("Client start faild", e);
            throw new NulsRuntimeException(ErrorCode.FAILED, "Client start faild");
        }
        //set system language
        try {
            NulsContext.DEFAULT_ENCODING = NulsContext.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_DEFAULT_ENCODING);
            String language = NulsContext.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_LANGUAGE);
            I18nUtils.setLanguage(language);
        } catch (NulsException e) {
            Log.error(e);
        }
    }

    @Override
    public void start() {
        QueueManager.start();
        this.registerService(CommonTransactionService.class, new CommonTransactionService());
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
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public int getVersion() {
        // todo auto-generated method stub(niels)
        return 0;
    }
}
