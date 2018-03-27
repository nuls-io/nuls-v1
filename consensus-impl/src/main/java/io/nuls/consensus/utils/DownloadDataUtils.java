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
package io.nuls.consensus.utils;

import io.nuls.consensus.cache.manager.block.TemporaryCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.entity.GetSmallBlockParam;
import io.nuls.consensus.entity.GetTxGroupParam;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.event.GetSmallBlockRequest;
import io.nuls.consensus.event.GetTxGroupRequest;
import io.nuls.consensus.event.notice.AssembledBlockNotice;
import io.nuls.consensus.manager.BlockManager;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.consensus.thread.DataDownloadThread;
import io.nuls.core.chain.entity.*;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.entity.NodePo;
import io.nuls.event.bus.service.intf.EventBroadcaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2018/1/12
 */
public class DownloadDataUtils {

    private static final DownloadDataUtils INSTANCE = new DownloadDataUtils();

    private DownloadDataUtils() {
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_CONSENSUS, "data-download-consensu", new DataDownloadThread());
    }

    public static DownloadDataUtils getInstance() {
        return INSTANCE;
    }

    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();
    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();
    private BlockManager blockManager = BlockManager.getInstance();


    private final Map<String, Long> smbRequest = new HashMap<>();
    private final Map<String, Long> tgRequest = new HashMap<>();
    private final Map<String, Integer> smbRequestCount = new HashMap<>();
    private final Map<String, Integer> tgRequestCount = new HashMap<>();

    public void requestSmallBlock(NulsDigestData blockHash, String nodeId) {
        GetSmallBlockRequest request = new GetSmallBlockRequest();
        GetSmallBlockParam param = new GetSmallBlockParam();
        param.setBlockHash(blockHash);
        request.setEventBody(param);
        smbRequest.put(blockHash.getDigestHex(), System.currentTimeMillis());
        Integer value = smbRequestCount.get(blockHash.getDigestHex());
        if (value == null) {
            value = 0;
        }
        smbRequestCount.put(blockHash.getDigestHex(), 1 + value);
        if (StringUtils.isBlank(nodeId)) {
            eventBroadcaster.broadcastAndCache(request, false);
        } else {
            eventBroadcaster.sendToNode(request, nodeId);
        }
    }

    public void requestTxGroup(NulsDigestData blockHash, String nodeId) {
        GetTxGroupRequest request = new GetTxGroupRequest();
        GetTxGroupParam data = new GetTxGroupParam();
        data.setBlockHash(blockHash);
        List<NulsDigestData> txHashList = new ArrayList<>();

        SmallBlock smb = temporaryCacheManager.getSmallBlock(blockHash.getDigestHex());
        for (NulsDigestData txHash : smb.getTxHashList()) {
            boolean exist = txCacheManager.txExist(txHash);
            if (!exist) {
                txHashList.add(txHash);
            }
        }
        if (txHashList.isEmpty()) {
            BlockHeader header = temporaryCacheManager.getBlockHeader(smb.getBlockHash().getDigestHex());
            if (null == header) {
                return;
            }
            Block block = new Block();
            block.setHeader(header);
            List<Transaction> txs = new ArrayList<>();
            for (NulsDigestData txHash : smb.getTxHashList()) {
                Transaction tx = txCacheManager.getTx(txHash);
                if (null == tx) {
                    throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
                }
                txs.add(tx);
            }
            block.setTxs(txs);
            ValidateResult<RedPunishData> vResult = block.verify();
            if (null == vResult || vResult.isFailed()) {
                if (vResult.getLevel() == SeverityLevelEnum.FLAGRANT_FOUL) {
                    RedPunishData redPunishData = vResult.getObject();
                    ConsensusMeetingRunner.putPunishData(redPunishData);
                }
                return;
            }
            blockManager.addBlock(block, false,nodeId);
            AssembledBlockNotice notice = new AssembledBlockNotice();
            notice.setEventBody(header);
            eventBroadcaster.publishToLocal(notice);
            return;
        }
        data.setTxHashList(txHashList);
        request.setEventBody(data);
        tgRequest.put(blockHash.getDigestHex(), System.currentTimeMillis());
        Integer value = tgRequestCount.get(blockHash.getDigestHex());
        if (null == value) {
            value = 0;
        }
        tgRequestCount.put(blockHash.getDigestHex(), 1 + value);
        if (StringUtils.isBlank(nodeId)) {
            eventBroadcaster.broadcastAndCache(request, false);
        } else {
            eventBroadcaster.sendToNode(request, nodeId);
        }
    }

    public void removeSmallBlock(String blockHash) {
        smbRequest.remove(blockHash);
        smbRequestCount.remove(blockHash);
    }

    public void removeTxGroup(String blockHash) {
        tgRequest.remove(blockHash);
        tgRequestCount.remove(blockHash);
    }

    public void remove(String blockHash) {
        smbRequest.remove(blockHash);
        tgRequest.remove(blockHash);
        smbRequestCount.remove(blockHash);
        tgRequestCount.remove(blockHash);
    }

    public void reRequest() {
        this.reRequestSmallBlock();
        this.reReqesetTxGroup();
    }

    private void reReqesetTxGroup() {
        for (String hash : this.tgRequest.keySet()) {
            Long time = tgRequest.get(hash);
            if (null != time && (System.currentTimeMillis() - time) >= 1000L) {
                this.requestTxGroup(NulsDigestData.fromDigestHex(hash), null);
            }
            if (tgRequestCount.get(hash) >= 10) {
                this.removeTxGroup(hash);
            }
        }

    }

    private void reRequestSmallBlock() {
        for (String hash : this.tgRequest.keySet()) {
            Long time = tgRequest.get(hash);
            if (null != time && (System.currentTimeMillis() - time) >= 1000L) {
                this.requestSmallBlock(NulsDigestData.fromDigestHex(hash), null);
            }
            if (smbRequestCount.get(hash) >= 10) {
                this.removeTxGroup(hash);
            }
        }
    }
}
