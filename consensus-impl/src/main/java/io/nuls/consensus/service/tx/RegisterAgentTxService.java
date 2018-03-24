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
package io.nuls.consensus.service.tx;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.event.notice.RegisterAgentNotice;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.event.bus.service.intf.EventBroadcaster;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class RegisterAgentTxService implements TransactionService<RegisterAgentTransaction> {
    private ConsensusCacheManager manager = ConsensusCacheManager.getInstance();
    private AgentDataService delegateAccountService = NulsContext.getServiceBean(AgentDataService.class);
    private DepositDataService delegateService = NulsContext.getServiceBean(DepositDataService.class);

    @Override
    public void onRollback(RegisterAgentTransaction tx) throws NulsException {
        this.manager.delAgent(tx.getTxData().getHexHash());
        manager.delDepositByAgentHash(tx.getTxData().getHexHash());
        this.delegateAccountService.delete(tx.getTxData().getHexHash());
        this.delegateService.deleteByAgentHash(tx.getTxData().getHexHash());
    }

    @Override
    public void onCommit(RegisterAgentTransaction tx) throws NulsException {
        manager.changeAgentStatusByHash(tx.getTxData().getHexHash(), ConsensusStatusEnum.WAITING);
        Consensus<Agent> ca = tx.getTxData();
        ca.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
        AgentPo po = ConsensusTool.agentToPojo(ca);
        delegateAccountService.save(po);
        RegisterAgentNotice notice = new RegisterAgentNotice();
        notice.setEventBody(tx);
        NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
    }


    @Override
    public void onApproval(RegisterAgentTransaction tx) throws NulsException {
        Consensus<Agent> ca = tx.getTxData();
        ca.getExtend().setStatus(ConsensusStatusEnum.NOT_IN.getCode());
        manager.cacheAgent(ca);

    }
}
