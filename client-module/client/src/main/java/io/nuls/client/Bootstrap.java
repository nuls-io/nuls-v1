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

package io.nuls.client;

import io.nuls.kernel.args.NULSParams;
import io.nuls.client.rpc.RpcServerManager;
import io.nuls.client.rpc.constant.RpcConstant;
import io.nuls.client.rpc.resources.thread.ShutdownHook;
import io.nuls.client.rpc.resources.util.FileUtil;
import io.nuls.client.storage.LanguageService;
import io.nuls.client.version.WalletVersionManager;
import io.nuls.client.web.view.WebViewBootstrap;
import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.provider.BlockQueueProvider;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.i18n.I18nUtils;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.module.manager.ModuleManager;
import io.nuls.kernel.module.service.ModuleService;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.base.download.thread.CollectThread;
import io.nuls.protocol.service.DownloadService;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author: Niels Wang
 */
public class Bootstrap {

    public static void main(String[] args) {
        Thread.currentThread().setName("Nuls");
        try {
            NULSParams.BOOTSTRAP.init(args);
            System.setProperty("protostuff.runtime.allow_null_array_element", "true");
            System.setProperty("file.encoding", UTF_8.name());
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, UTF_8);
            sysStart();
        } catch (Exception e) {
            Log.error(e);
            System.exit(-1);
        }
    }

    private static void copyWebFiles() throws UnsupportedEncodingException {
        String path = Bootstrap.class.getClassLoader().getResource("").getPath() + "/temp/" + NulsConfig.VERSION + "/conf/client-web/";
        path = URLDecoder.decode(path, "UTF-8");
        File source = new File(path);
        if (!source.exists()) {
            Log.info("source not exists:" + path);
            return;
        }
        Log.info("do the files copy!");
        File target = new File(URLDecoder.decode(Bootstrap.class.getClassLoader().getResource("").getPath(), "UTF-8") + "/conf/client-web/");
        FileUtil.deleteFolder(target);
        FileUtil.copyFolder(source, target);
    }

    private static void sysStart() throws Exception {
        do {
            MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
            mk.init();
            mk.start();
            WalletVersionManager.start();
            initModules();
            String ip = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_IP, RpcConstant.DEFAULT_IP);
            int port = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_PORT, RpcConstant.DEFAULT_PORT);
            copyWebFiles();
            if (NULSParams.BOOTSTRAP.getRpcIp() != null) {
                ip = NULSParams.BOOTSTRAP.getRpcIp();
            }
            if (NULSParams.BOOTSTRAP.getRpcPort() != null) {
                port = NULSParams.BOOTSTRAP.getRpcPort();
            }
            RpcServerManager.getInstance().startServer(ip, port);

            LanguageService languageService = NulsContext.getServiceBean(LanguageService.class);
            String languageDB = (String) languageService.getLanguage().getData();
            String language = null == languageDB ? I18nUtils.getLanguage() : languageDB;
            I18nUtils.setLanguage(language);
            if (null == languageDB) {
                languageService.saveLanguage(language);
            }
        } while (false);

        // if isDaemon flag is true, don't launch the WebView
        boolean isDaemon = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_DAEMON, false);
        if (!isDaemon) {
            TaskManager.asynExecuteRunnable(new WebViewBootstrap());
        }

        int i = 0;
        Map<NulsDigestData, List<Node>> map = new HashMap<>();
        NulsContext context = NulsContext.getInstance();
        DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);
        while (true) {
            if (context.getStop() > 0) {
                if (context.getStop() == 2) {
                    Runtime.getRuntime().addShutdownHook(new ShutdownHook());
                }
                System.exit(0);
            }
            if (NulsContext.mastUpGrade) {
                //如果强制升级标志开启，停止网络连接
                ModuleManager.getInstance().stopModule(NetworkConstant.NETWORK_MODULE_ID);
                Log.error(">>>>>> The new protocol version has taken effect, the network connection has been disconnected，please upgrade immediately **********");
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            if (i > 10) {
                i = 0;
                if (!downloadService.isDownloadSuccess().isSuccess() && CollectThread.getInstance().getStartHeight() > 0) {
                    Log.info("collect-start:{},request-start:{},BlockQueueSize:{}", CollectThread.getInstance().getStartHeight(), CollectThread.getInstance().getRequestStartHeight(), BlockQueueProvider.getInstance().size());
                }
                Block bestBlock = NulsContext.getInstance().getBestBlock();
                Collection<Node> nodes = NulsContext.getServiceBean(NetworkService.class).getAvailableNodes();
                Log.info("bestHeight:" + bestBlock.getHeader().getHeight() + " , txCount : " + bestBlock.getHeader().getTxCount() + " , tx memory pool count : " + TxMemoryPool.getInstance().size() + " - " + TxMemoryPool.getInstance().getOrphanPoolSize() + " , hash : " + bestBlock.getHeader().getHash() + ",nodeCount:" + nodes.size());
                map.clear();
                for (Node node : nodes) {
                    List<Node> ips = map.get(node.getBestBlockHash());
                    if (null == ips) {
                        ips = new ArrayList<>();
                        map.put(node.getBestBlockHash(), ips);
                    }
                    ips.add(node);
                }
                for (NulsDigestData key : map.keySet()) {
                    if (key == null) continue;
                    List<Node> nodeList = map.get(key);
                    long height = nodeList.get(0).getBestBlockHeight();
                    StringBuilder ids = new StringBuilder();
                    for (Node node : nodeList) {
                        ids.append("," + node.getId());
                    }
                    Log.info("height:" + height + ",count:" + nodeList.size() + ", hash:" + key.getDigestHex() + ids);
                }
            } else {
                i++;
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
