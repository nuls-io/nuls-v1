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
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.event.notice.EntrustConsensusNotice;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.db.entity.DepositPo;
import io.nuls.event.bus.service.intf.EventBroadcaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class JoinConsensusTxService implements TransactionService<PocJoinConsensusTransaction> {
    private ConsensusCacheManager manager = ConsensusCacheManager.getInstance();
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);
    private AgentDataService accountDataService = NulsContext.getServiceBean(AgentDataService.class);

    @Override
    public void onRollback(PocJoinConsensusTransaction tx) throws NulsException {
        this.manager.delDeposit(tx.getTxData().getHexHash());
        depositDataService.delete(tx.getTxData().getHexHash());
    }

    @Override
    public void onCommit(PocJoinConsensusTransaction tx) throws NulsException {
        manager.changeDepositStatus(tx.getTxData().getHexHash(), ConsensusStatusEnum.WAITING);
        Consensus<Deposit> cd = tx.getTxData();
        cd.getExtend().setTxHash(tx.getHash().getDigestHex());
        cd.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
        DepositPo po = ConsensusTool.depositToPojo(cd,tx.getHash().getDigestHex());
        po.setBlockHeight(tx.getBlockHeight());
        po.setTime(tx.getTime());
        depositDataService.save(po);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("agentHash", cd.getExtend().getAgentHash());
        List<DepositPo> poList = depositDataService.getList(paramsMap);
        long sum = 0L;
        for (DepositPo depositPo : poList) {
            sum += depositPo.getDeposit();
        }
        if (sum >= PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT.getValue()) {
            manager.changeAgentStatusByHash(tx.getTxData().getExtend().getAgentHash(),ConsensusStatusEnum.IN);
            manager.changeDepositStatusByAgentHash(tx.getTxData().getExtend().getAgentHash(),ConsensusStatusEnum.IN);
            AgentPo daPo = this.accountDataService.get(cd.getExtend().getAgentHash());
            if (null == daPo) {
                throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "the agent cannot find,agent hash:" + cd.getExtend().getAgentHash());
            }
            daPo.setStatus(ConsensusStatusEnum.IN.getCode());
            this.accountDataService.updateSelective(daPo);
        }
        EntrustConsensusNotice notice = new EntrustConsensusNotice();
        notice.setEventBody(tx);
        NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
    }

    @Override
    public void onApproval(PocJoinConsensusTransaction tx) throws NulsException {
        Consensus<Deposit> cd = tx.getTxData();
        cd.getExtend().setStatus(ConsensusStatusEnum.NOT_IN.getCode());
        cd.getExtend().setTxHash(tx.getHash().getDigestHex());
        manager.cacheDeposit(cd);

    }
}
