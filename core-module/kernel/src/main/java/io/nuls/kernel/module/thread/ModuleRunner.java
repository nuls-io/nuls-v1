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
package io.nuls.kernel.module.thread;

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.ModuleStatusEnum;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.module.BaseModuleBootstrap;
import io.nuls.kernel.module.manager.ModuleManager;

/**
 * @author Niels
 */
public class ModuleRunner implements Runnable {

    private final String moduleKey;
    private final String moduleClass;
    private BaseModuleBootstrap module;

    public ModuleRunner(String key, String moduleClass) {
        this.moduleKey = key;
        this.moduleClass = moduleClass;
    }

    @Override
    public void run() {
        try {
            module = this.loadModule();
            module.setStatus(ModuleStatusEnum.INITIALIZING);
            module.init();
            module.setStatus(ModuleStatusEnum.INITIALIZED);
            module.setStatus(ModuleStatusEnum.STARTING);
            module.start();
            module.setStatus(ModuleStatusEnum.RUNNING);
        } catch (ClassNotFoundException e) {
//            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
            throw new RuntimeException(e);
        } catch (NulsRuntimeException e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
            throw e;
        } catch (Exception e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
            Log.error(e);
            System.exit(-1);
        }
    }

    private BaseModuleBootstrap loadModule() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        BaseModuleBootstrap module = null;
        do {
            if (StringUtils.isBlank(moduleClass)) {
                Log.warn("module cannot start:" + moduleClass);
                break;
            }
            Class clazz = Class.forName(moduleClass);
            module = (BaseModuleBootstrap) clazz.newInstance();
            module.setModuleName(this.moduleKey);
            Log.debug("load module:" + module.getInfo());
        } while (false);
        ModuleManager.getInstance().regModule(module);

        return module;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public BaseModuleBootstrap getModule() {
        return module;
    }
}
