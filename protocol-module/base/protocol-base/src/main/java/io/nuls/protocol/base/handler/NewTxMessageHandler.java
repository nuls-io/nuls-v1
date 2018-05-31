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
package io.nuls.protocol.base.handler;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.network.model.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.message.TransactionMessage;
import io.nuls.protocol.service.TransactionService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class NewTxMessageHandler extends AbstractMessageHandler<TransactionMessage> {

    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();

    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);
    private TransactionService transactionService = NulsContext.getServiceBean(TransactionService.class);

    public NewTxMessageHandler() {
    }

    @Override
    public void onMessage(TransactionMessage message, Node fromNode) {

        Transaction tx = message.getMsgBody();
        if (null == tx) {
            return;
        }
        if(null!=fromNode){
            Log.debug("receive new tx {} from {} , tx count {}", tx.getHash().getDigestHex(), fromNode.getId(), temporaryCacheManager.getTxCount());
        }

        if (tx.getType() == ProtocolConstant.TX_TYPE_COINBASE || tx.getType() == ConsensusConstant.TX_TYPE_YELLOW_PUNISH || tx.getType() == ConsensusConstant.TX_TYPE_RED_PUNISH) {
            return;
        }
        ValidateResult result = tx.verify();
        if (result.isFailed() && result.getErrorCode() != TransactionErrorCode.ORPHAN_TX) {
//            if (result.getLevel() == SeverityLevelEnum.FLAGRANT_FOUL) {
//                networkService.removeNode(fromNode.getId());
//            }
            return;
        }

        Transaction tempTx = temporaryCacheManager.getTx(tx.getHash());
        if (tempTx != null) {
            consensusService.newTx(tx);
            return;
        }
        try {
            temporaryCacheManager.cacheTx(tx);
            consensusService.newTx(tx);
            transactionService.forwardTx(message.getMsgBody(), fromNode);
        } catch (Exception e) {
            Log.error(e);
        }
    }

}
