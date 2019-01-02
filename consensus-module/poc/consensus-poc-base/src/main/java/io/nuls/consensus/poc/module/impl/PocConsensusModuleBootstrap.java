/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.consensus.poc.module.impl;

import io.nuls.consensus.module.AbstractConsensusModule;
import io.nuls.consensus.poc.block.validator.BifurcationUtil;
import io.nuls.consensus.poc.config.ConsensusConfig;
import io.nuls.consensus.poc.constant.ConsensusStatus;
import io.nuls.consensus.poc.context.ConsensusStatusContext;
import io.nuls.consensus.poc.model.Evidence;
import io.nuls.consensus.poc.scheduler.ConsensusScheduler;
import io.nuls.consensus.poc.storage.po.EvidencePo;
import io.nuls.consensus.poc.storage.service.BifurcationEvidenceStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.ModuleStatusEnum;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.thread.BaseThread;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.protocol.constant.ProtocolConstant;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 */
public class PocConsensusModuleBootstrap extends AbstractConsensusModule {

    @Override
    public void init() throws Exception {
        ConsensusStatusContext.setConsensusStatus(ConsensusStatus.INITING);
        ConsensusConfig.initConfiguration();
        BifurcationEvidenceStorageService bes = NulsContext.getServiceBean(BifurcationEvidenceStorageService.class);

        Map<String, List<EvidencePo>> map = bes.getBifurcationEvidence();
        if (null != map) {
            BifurcationUtil.getInstance().setBifurcationEvidenceMap(Evidence.bifurcationEvidencePoMapToMap(map));
        }
    }

    @Override
    public void start() {
        this.waitForDependencyRunning(ProtocolConstant.MODULE_ID_PROTOCOL);

        ConsensusScheduler.getInstance().start();
        this.registerHandlers();
        Log.info("the POC consensus module is started!");
    }


    private void registerHandlers() {

    }

    @Override
    public void shutdown() {
        ConsensusScheduler.getInstance().stop();
        TaskManager.shutdownByModuleId(this.getModuleId());
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getInfo() {
        if (this.getStatus() == ModuleStatusEnum.UNINITIALIZED || this.getStatus() == ModuleStatusEnum.INITIALIZING) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        str.append("module:[consensus]:\n");
        str.append("thread count:");
        List<BaseThread> threadList = TaskManager.getThreadList(this.getModuleId());
        if (null == threadList) {
            str.append(0);
        } else {
            str.append(threadList.size());
            for (BaseThread thread : threadList) {
                str.append("\n");
                str.append(thread.getName());
                str.append("{");
                str.append(thread.getPoolName());
                str.append("}");
            }
        }
        return str.toString();
    }


}
