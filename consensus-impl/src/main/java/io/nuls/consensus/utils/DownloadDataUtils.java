package io.nuls.consensus.utils;

import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.entity.TxHashData;
import io.nuls.consensus.event.GetSmallBlockRequest;
import io.nuls.consensus.event.GetTxGroupRequest;
import io.nuls.consensus.thread.DataDownloadThread;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.SmallBlock;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.str.StringUtils;
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

    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);
    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();
    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();

    private final Map<String, Long> smbRequest = new HashMap<>();
    private final Map<String, Long> tgRequest = new HashMap<>();
    private final Map<String, Integer> smbRequestCount = new HashMap<>();
    private final Map<String, Integer> tgRequestCount = new HashMap<>();

    public void requestSmallBlock(NulsDigestData blockHash, String nodeId) {
        GetSmallBlockRequest request = new GetSmallBlockRequest();
        request.setEventBody(blockHash);
        if (StringUtils.isBlank(nodeId)) {
            eventBroadcaster.broadcastAndCache(request, false);
        } else {
            eventBroadcaster.sendToNode(request, nodeId);
        }
        smbRequest.put(blockHash.getDigestHex(), System.currentTimeMillis());
        if (null == smbRequestCount.get(blockHash.getDigestHex())) {
            smbRequestCount.put(blockHash.getDigestHex(), 1);
        } else {
            smbRequestCount.put(blockHash.getDigestHex(), 1 + smbRequestCount.get(blockHash.getDigestHex()));
        }
    }

    public void requestTxGroup(NulsDigestData blockHash, String nodeId) {
        GetTxGroupRequest request = new GetTxGroupRequest();
        TxHashData data = new TxHashData();
        data.setBlockHash(blockHash);
        List<NulsDigestData> txHashList = new ArrayList<>();
        SmallBlock smb = blockCacheManager.getSmallBlock(blockHash.getDigestHex());
        for (NulsDigestData txHash : smb.getTxHashList()) {
            boolean exist = txCacheManager.txExist(txHash);
            if (!exist) {
                txHashList.add(txHash);
            }
        }
        data.setTxHashList(txHashList);
        request.setEventBody(data);
        if (StringUtils.isBlank(nodeId)) {
            eventBroadcaster.broadcastAndCache(request, false);
        } else {
            eventBroadcaster.sendToNode(request, nodeId);
        }
        tgRequest.put(blockHash.getDigestHex(), System.currentTimeMillis());
        if (null == tgRequestCount.get(blockHash.getDigestHex())) {
            tgRequestCount.put(blockHash.getDigestHex(), 1);
        } else {
            tgRequestCount.put(blockHash.getDigestHex(), 1 + tgRequestCount.get(blockHash.getDigestHex()));
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
            if (null != time && (System.currentTimeMillis() - time)  >= 1000L) {
                this.requestSmallBlock(NulsDigestData.fromDigestHex(hash), null);
            }if (smbRequestCount.get(hash) >= 10) {
                this.removeTxGroup(hash);
            }
        }
    }
}
