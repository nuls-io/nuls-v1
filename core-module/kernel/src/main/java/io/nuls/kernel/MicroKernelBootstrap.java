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
package io.nuls.kernel;

import io.nuls.core.tools.cfg.ConfigLoader;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.i18n.I18nUtils;
import io.nuls.kernel.lite.core.ModularServiceMethodInterceptor;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.module.BaseModuleBootstrap;
import io.nuls.kernel.module.manager.VersionManager;
import io.nuls.kernel.validate.ValidatorManager;

import java.io.IOException;
import java.lang.reflect.Field;

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

    /**
     * 微内核模块的初始化方法，进行的操作有：加载配置文件信息到内存中，设置默认编码方式和语言，启动spring-lite容器，启动版本管理器和验证器管理器
     * <p>
     * Micro kernel module initialization method, for the operation are: load profile information into memory,
     * set the default encoding and language, start the spring - lite container, and start the version manager and validator manager
     */
    @Override
    public void init() {
        try {
            NulsConfig.NULS_CONFIG = ConfigLoader.loadIni(NulsConstant.USER_CONFIG_FILE);
            NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConstant.MODULES_CONFIG_FILE);
        } catch (IOException e) {
            Log.error("Client start failed", e);
            throw new RuntimeException("Client start failed");
        }
        //set system language
        try {
            NulsConfig.DEFAULT_ENCODING = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_DEFAULT_ENCODING);
            String language = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_LANGUAGE);
            I18nUtils.setLanguage(language);
        } catch (Exception e) {
            Log.error(e);
        }
        SpringLiteContext.init("io.nuls", new ModularServiceMethodInterceptor());
        try {
            ValidatorManager.init();
            VersionManager.start();
        } catch (Exception e) {
            Log.error(e);
        }

    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getInfo() {
        StringBuilder info = new StringBuilder();
        info.append("kernel module:\n");

        try {
            Class mavenInfo = Class.forName("io.nuls.module.version.KernelMavenInfo");
            Field field = mavenInfo.getDeclaredField("VERSION");
            info.append("module-version:");
            info.append(field.get(mavenInfo));
        } catch (Exception e) {
            //do nothing
        }

        info.append("\nDEFAULT_ENCODING:");
        info.append(NulsConfig.DEFAULT_ENCODING);
        info.append("\nLanguage：");
        info.append(I18nUtils.getLanguage());
        info.append("\nNuls-Config:");
        info.append(NulsConfig.NULS_CONFIG);
        info.append("\nModule-Config:");
        info.append(NulsConfig.MODULES_CONFIG);
        return info.toString();
    }

}
