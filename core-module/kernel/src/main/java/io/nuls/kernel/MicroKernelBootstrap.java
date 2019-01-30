/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.i18n.I18nUtils;
import io.nuls.kernel.lite.core.ModularServiceMethodInterceptor;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.module.BaseModuleBootstrap;
import io.nuls.kernel.utils.TransactionManager;
import io.nuls.kernel.validate.ValidatorManager;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @author Niels
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
        String folder = null;
        try {
            NulsConfig.NULS_CONFIG = ConfigLoader.loadIni(NulsConstant.USER_CONFIG_FILE);

            String mode = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, "mode", "main");
            if ("main".equals(mode)) {
                NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConstant.MODULES_CONFIG_FILE);
            } else {
                NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(mode + "/" + NulsConstant.MODULES_CONFIG_FILE);
            }
        } catch (Exception e) {
            Log.error("Client start failed", e);
            throw new RuntimeException("Client start failed");
        }

        TimeService.getInstance().start();

        //set system language
        try {
            NulsConfig.DEFAULT_ENCODING = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_DEFAULT_ENCODING);
            String language = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_LANGUAGE);
            I18nUtils.setLanguage(language);
            String chainId = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_DEFAULT_CHAIN_ID, "8964");
            NulsContext.getInstance().setDefaultChainId(Short.parseShort(chainId));
        } catch (Exception e) {
            Log.error(e);
        }
        SpringLiteContext.init("io.nuls", new ModularServiceMethodInterceptor());
        try {
            NulsConfig.VERSION = getKernelVersion();
            TransactionManager.init();
            ValidatorManager.init();
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

        String version = getKernelVersion();
        if (StringUtils.isBlank(version)) {
            info.append("module-version:");
            info.append(version);
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

    private String getKernelVersion() {
        try {
            Class mavenInfo = Class.forName("io.nuls.module.version.KernelMavenInfo");
            Field field = mavenInfo.getDeclaredField("VERSION");
            return (String) field.get(mavenInfo);
        } catch (Exception e) {
            //do nothing
        }
        return "0.0.0";
    }
}
