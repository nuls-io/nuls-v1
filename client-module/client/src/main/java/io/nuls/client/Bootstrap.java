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

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.module.service.ModuleService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Niels Wang
 * @date: 2018/5/7
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


    private static void sysStart() throws Exception {
        do {
            MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
            mk.init();
            mk.start();
            initModules();
            Thread.sleep(3000);
        } while (false);
        while (true) {
            try {
                //todo 后续启动一个系统监视线程
                Thread.sleep(20000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            if (null != NulsContext.getInstance().getBestBlock()) {
//                Log.info("node: " + NulsContext.getServiceBean(NetworkService.class).getAvailableNodes().size() + "), height:{}, threadCount:{}, consensusStatus: {}, downloadStatus: {}", NulsContext.getInstance().getBestBlock().getHeader().getHeight(), Thread.activeCount(), NulsContext.getServiceBean(ConsensusService.class).getConsensusStatus(), NulsContext.getServiceBean(DownloadService.class).getStatus());
//                Collection<Node> nodes = NulsContext.getServiceBean(NetworkService.class).getAvailableNodes();
//                for (Node node : nodes) {
//                    Log.info(node.getVersionMessage().getBestBlockHeight() + ", " + node.getId() + ", " + node.getVersionMessage().getBestBlockHash());
//                }
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
