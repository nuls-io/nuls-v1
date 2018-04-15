/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.event.handler;

import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.OrphanTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.download.DownloadCacheHandler;
import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.manager.BlockManager;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.network.service.NetworkService;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class BlockEventHandler extends AbstractEventHandler<BlockEvent> {

    private BlockManager blockCacheManager = BlockManager.getInstance();
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);

    @Override
    public void onEvent(BlockEvent event, String fromId) {
        Block block = event.getEventBody();
        if (null == block) {
            Log.warn("recieved a null blockEvent form " + fromId);
            return;
        }
        //BlockLog.debug("download("+fromId+") block height:" + block.getHeader().getHeight() + ", preHash:" + block.getHeader().getPreHash() + " , hash:" + block.getHeader().getHash() + ", address:" + block.getHeader().getPackingAddress());
//        if (BlockBatchDownloadUtils.getInstance().downloadedBlock(fromId, block)) {
//            return;
//        }

        //blockCacheManager.addBlock(block, true, fromId);
        for (Transaction tx : block.getTxs()) {
            Transaction cachedTx = ConfirmingTxCacheManager.getInstance().getTx(tx.getHash());
            if (null == cachedTx) {
                cachedTx = ReceivedTxCacheManager.getInstance().getTx(tx.getHash());
            }
            if (null == cachedTx) {
                cachedTx = OrphanTxCacheManager.getInstance().getTx(tx.getHash());
            }
            if (cachedTx != null && cachedTx.getStatus() != tx.getStatus()) {
                tx.setStatus(cachedTx.getStatus());
                if(!(tx instanceof AbstractCoinTransaction)){
                    continue;
                }
                AbstractCoinTransaction coinTx = (AbstractCoinTransaction) tx;
                AbstractCoinTransaction cachedCoinTx = (AbstractCoinTransaction) cachedTx;
                coinTx.setCoinData(cachedCoinTx.getCoinData());
//                Log.error("the transaction status is wrong!");
            }
        }
        DownloadCacheHandler.receiveBlock(block);

    }
}
