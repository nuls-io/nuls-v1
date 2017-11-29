package io.nuls.core.module.thread;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;

import java.util.concurrent.ThreadFactory;

/**
 * @author Niels
 * @date 2017/11/27
 */
public class ModuleThreadFactory implements ThreadFactory {

    private static final String POOL_NAME = "Process";
    @Override
    public ModuleProcess newThread(Runnable r) {
        if (null == r) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "runnable cannot be null!");
        }
        if (!(r instanceof ModuleRunner)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "unkown runnable!");
        }
        ModuleProcess process = new ModuleProcess((ModuleRunner) r);
        process.setModuleId((short) 0);
        process.setName(POOL_NAME+"-"+((ModuleRunner) r).getModuleKey());
        process.setPoolName(POOL_NAME);
        process.setDaemon(false);
        return process;
    }
}
