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

package io.nuls.client;

import io.nuls.client.rpc.RpcServerManager;
import io.nuls.client.rpc.constant.RpcConstant;
import io.nuls.client.rpc.resources.util.FileUtil;
import io.nuls.client.storage.LanguageService;
import io.nuls.client.storage.impl.LanguageServiceImpl;
import io.nuls.client.version.VersionManager;
import io.nuls.client.web.view.WebViewBootstrap;
import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.config.ConsensusConfig;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.core.tools.date.DateUtil;
import io.nuls.core.tools.log.Log;
import io.nuls.db.service.DBService;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.i18n.I18nUtils;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.module.service.ModuleService;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.model.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.service.BlockService;

import java.io.File;
import java.util.*;

/**
 * @author: Niels Wang
 */
public class Bootstrap {
    public static void main(String[] args) {
        Thread.currentThread().setName("Nuls");
        try {
            sysStart();
        } catch (Exception e) {
            Log.error(e);
            System.exit(-1);
        }
    }

    private static void copyWebFiles() {
        String path = Bootstrap.class.getClassLoader().getResource("").getPath() + "/temp/" + NulsConfig.VERSION + "/conf/client-web/";
        File source = new File(path);
        if (!source.exists()) {
            Log.info("source not exists:" + path);
            return;
        }
        Log.info("do the files copy!");
        File target = new File(Bootstrap.class.getClassLoader().getResource("").getPath() + "/conf/client-web/");
        FileUtil.deleteFolder(target);
        FileUtil.copyFolder(source, target);
    }

    private static void sysStart() throws Exception {
        do {
            MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
            mk.init();
            mk.start();
            VersionManager.start();
            initModules();
            String ip = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_IP, RpcConstant.DEFAULT_IP);
            int port = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_PORT, RpcConstant.DEFAULT_PORT);
            copyWebFiles();
            RpcServerManager.getInstance().startServer(ip, port);

            LanguageService languageService = NulsContext.getServiceBean(LanguageService.class);
            String languageDB = (String) languageService.getLanguage().getData();
            String language = null == languageDB ? I18nUtils.getLanguage() : languageDB;
            I18nUtils.setLanguage(language);
            if (null == languageDB) {
                languageService.saveLanguage(language);
            }
        } while (false);
        TaskManager.asynExecuteRunnable(new WebViewBootstrap());


        while (true) {
            try {
                //todo 后续启动一个系统监视线程
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            if (null != NulsContext.getInstance().getBestBlock()) {
                Log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-  netTime : " + (DateUtil.convertDate(new Date(TimeService.currentTimeMillis()))));
                Block bestBlock = NulsContext.getInstance().getBestBlock();
                Collection<Node> nodes = NulsContext.getServiceBean(NetworkService.class).getAvailableNodes();

                Log.info("bestHeight:" + bestBlock.getHeader().getHeight() + " , txCount : " + bestBlock.getHeader().getTxCount() + " , tx memory pool count : " + TxMemoryPool.getInstance().size() + " , hash : " + bestBlock.getHeader().getHash() + ",nodeCount:" + nodes.size());
                for (Node node : nodes) {
                    Log.info(node.getBestBlockHeight() + ", " + node.getId() + ", " + node.getBestBlockHash());
                }
            }
        }
    }

    private static void initModules() {
        Map<String, String> bootstrapClasses = null;
        try {
            bootstrapClasses = getModuleBootstrapClass();
        } catch (Exception e) {
            Log.error(e);
        }
        if (null == bootstrapClasses || bootstrapClasses.isEmpty()) {
            return;
        }
        ModuleService.getInstance().startModules(bootstrapClasses);
    }

    private static Map<String, String> getModuleBootstrapClass() throws Exception {
        Map<String, String> map = new HashMap<>();
        List<String> moduleNameList = NulsConfig.MODULES_CONFIG.getSectionList();
        if (null == moduleNameList || moduleNameList.isEmpty()) {
            return map;
        }
        for (String moduleName : moduleNameList) {

            String className = null;
            try {
                className = NulsConfig.MODULES_CONFIG.getCfgValue(moduleName, NulsConstant.MODULE_BOOTSTRAP_KEY);
            } catch (Exception e) {
                continue;
            }
            map.put(moduleName, className);
        }
        return map;
    }
}
