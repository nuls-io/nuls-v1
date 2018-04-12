/**
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
 */
package io.nuls.core.module;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.module.manager.ServiceManager;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/9/26
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
    public abstract void init();

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
     *
     * @return
     */
    public abstract String getInfo();

    /**
     * get the status of the module
     *
     * @return
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
            return NulsContext.MODULES_CONFIG.getCfgValue(section, property);
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }

    protected final void registerService(Class serviceClass) {
        ServiceManager.getInstance().regService(this.moduleId, serviceClass);
    }

    protected final void registerTransaction(int txType, Class<? extends Transaction> txClass, Class<? extends TransactionService> txServiceClass) {
        this.registerService(txServiceClass);
        TransactionManager.putTx(txType, txClass, txServiceClass);
    }

    public Class<? extends BaseModuleBootstrap> getModuleClass() {
        return this.getClass();
    }

    public short getModuleId() {
        return moduleId;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
