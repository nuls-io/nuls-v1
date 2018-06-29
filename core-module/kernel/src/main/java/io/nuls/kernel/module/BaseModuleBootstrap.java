/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.kernel.module;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.ModuleStatusEnum;
import io.nuls.kernel.module.manager.ModuleManager;
import io.nuls.kernel.module.manager.ServiceManager;

/**
 * @author Niels
 */
public abstract class BaseModuleBootstrap {

    private final short moduleId;

    private String moduleName;

    private ModuleStatusEnum status;

    public BaseModuleBootstrap(short moduleId) {
        this.moduleId = moduleId;
        this.status = ModuleStatusEnum.UNINITIALIZED;
    }

    /**
     *
     */
    public abstract void init() throws Exception;

    /**
     * start the module
     */
    public abstract void start();

    /**
     * stop the module
     */
    public abstract void shutdown();

    /**
     * destroy the module
     */
    public abstract void destroy();

    /**
     * get all info of the module
     */
    public abstract String getInfo();

    /**
     * get the status of the module
     */
    public final ModuleStatusEnum getStatus() {
        return this.status;
    }

    public final String getModuleName() {
        return moduleName;
    }

    public void setStatus(ModuleStatusEnum status) {
        Log.info("Status change(" + this.moduleName + "):" + this.status + "-->" + status);
        this.status = status;
    }

    protected final String getModuleCfgProperty(String section, String property) {
        try {
            return NulsConfig.MODULES_CONFIG.getCfgValue(section, property);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

//    protected final void registerTransaction(int txType, Class<? extends Transaction> txClass, Class<? extends TransactionService> txServiceClass) {
//        this.registerService(txServiceClass);
//        TransactionManager.putTx(txType, txClass, txServiceClass);
//    }

    public Class<? extends BaseModuleBootstrap> getModuleClass() {
        return this.getClass();
    }

    public short getModuleId() {
        return moduleId;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    protected void waitForDependencyInited(short... moduleIds) {
        ModuleManager mm = ModuleManager.getInstance();
        String result = checkItInited(mm, moduleIds);
        while (result != null) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            result = checkItInited(mm, moduleIds);
        }
    }

    private String checkItInited(ModuleManager mm, short[] moduleIds) {
        for (short id : moduleIds) {
            BaseModuleBootstrap module = mm.getModule(id);
            if (null == module) {
                return id + "";
            }
            if (null == module || module.getStatus() == ModuleStatusEnum.UNINITIALIZED || module.getStatus() == ModuleStatusEnum.INITIALIZING || module.getStatus() == ModuleStatusEnum.EXCEPTION) {
                return module.getModuleName();
            }
        }
        return null;
    }


    protected void waitForDependencyRunning(short... moduleIds) {
        ModuleManager mm = ModuleManager.getInstance();
        String result = checkItRunning(mm, moduleIds);
        while (result != null) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            result = checkItRunning(mm, moduleIds);
        }
    }

    private String checkItRunning(ModuleManager mm, short[] moduleIds) {
        for (short id : moduleIds) {
            BaseModuleBootstrap module = mm.getModule(id);
            if (null == module) {
                return "null";
            }
            if (module.getStatus() != ModuleStatusEnum.RUNNING) {
                return module.getModuleName();
            }
        }
        return null;
    }
}
