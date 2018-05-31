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
import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.core.tools.date.DateUtil;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.module.service.ModuleService;
import io.nuls.network.model.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.service.BlockService;

import java.util.*;

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
            String ip = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_IP, RpcConstant.DEFAULT_IP);
            int port = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_PORT, RpcConstant.DEFAULT_PORT);
            RpcServerManager.getInstance().startServer(ip, port);
        } while (false);

        // test code begin TODO
//        Log.info("开始验证····");
//        testHash();
//        Log.info("验证hash成功····");
//        testHeight();
//        Log.info("验证结束！！！");
        // test code end TODO

        while (true) {
            try {
                //todo 后续启动一个系统监视线程
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            if (null != NulsContext.getInstance().getBestBlock()) {
                Log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-  netTime : " + (DateUtil.convertDate(new Date(TimeService.currentTimeMillis()))));
                Block bestBlock = NulsContext.getInstance().getBestBlock();
                Log.info("bestHeight:" + bestBlock.getHeader().getHeight() + " , txCount : " + bestBlock.getHeader().getTxCount() + " , tx memory pool count : " + TxMemoryPool.getInstance().getAll().size() + " , hash : " + bestBlock.getHeader().getHash());
//                Log.info("node: " + NulsContext.getServiceBean(NetworkService.class).getAvailableNodes().size() + "), height:{}, threadCount:{}, consensusStatus: {}, downloadStatus: {}", NulsContext.getInstance().getBestBlock().getHeader().getHeight(), Thread.activeCount(), NulsContext.getServiceBean(ConsensusService.class).getConsensusStatus(), NulsContext.getServiceBean(DownloadService.class).getStatus());
                Collection<Node> nodes = NulsContext.getServiceBean(NetworkService.class).getAvailableNodes();
                for (Node node : nodes) {
                    Log.info(node.getBestBlockHeight() + ", " + node.getId() + ", " + node.getBestBlockHash());
                }
            }
        }
    }

    /*=====  test code begin TODO ====*/
    private static void testHeight() throws Exception {
        BlockService blockService = NulsContext.getServiceBean(BlockService.class);
        Block bestBlock = blockService.getBestBlock().getData();
        if(bestBlock == null) {
            return;
        }
        NulsDigestData preHash = null;
        long lastheight = 0L;
        while(true) {
            lastheight = bestBlock.getHeader().getHeight();
            verifyBlcok(bestBlock, preHash);
            boolean isFirstBlock = false;
            if(bestBlock.getHeader().getHeight() == 0L) {
                isFirstBlock = true;
            }
            if(lastheight == 0L) {
                isFirstBlock = true;
            }
            preHash = bestBlock.getHeader().getPreHash();
            bestBlock = blockService.getBlock(lastheight - 1L).getData();
            if(bestBlock == null) {
                if(!isFirstBlock) {
                    throw new Exception("出错了");
                }
                break;
            }
            if(lastheight - bestBlock.getHeader().getHeight() != 1L) {
                throw new Exception("高度不正确");
            }
        }
    }

    private static void testHash() throws Exception {
        BlockService blockService = NulsContext.getServiceBean(BlockService.class);
        Block bestBlock = blockService.getBestBlock().getData();
        if(bestBlock == null) {
            return;
        }
        NulsDigestData preHash = null;
        while(true) {
            verifyBlcok(bestBlock, preHash);
            boolean isFirstBlock = false;
            if(bestBlock.getHeader().getHeight() == 0L) {
                isFirstBlock = true;
            }
            preHash = bestBlock.getHeader().getPreHash();
            bestBlock = blockService.getBlock(preHash).getData();
            if(bestBlock == null) {
                if(!isFirstBlock) {
                    throw new Exception("出错了");
                }
                break;
            }
        }
    }

    private static void verifyBlcok(Block block, NulsDigestData hash) throws Exception {
        block.getHeader().verifyWithException();
        if(block.getTxs().size() != block.getHeader().getTxCount()) {
            throw new Exception("交易数量不正确, height: " + block.getHeader().getHeight());
        }
        if(hash != null) {
            block.getHeader().setHash(null);
            if(!hash.equals(block.getHeader().getHash())) {
                throw new Exception("hash不正确, height: " + block.getHeader().getHeight());
            }
        }
    }
    /*=====  test code end TODO ====*/


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
