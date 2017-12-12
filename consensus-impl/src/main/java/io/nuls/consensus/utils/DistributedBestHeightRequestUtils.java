package io.nuls.consensus.utils;

import io.nuls.consensus.event.AskBestBlockEvent;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.service.intf.EventService;

import java.util.*;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class DistributedBestHeightRequestUtils {
    private EventService eventService = NulsContext.getInstance().getService(EventService.class);
    private List<String> peerIdList;
    private Map<String, BlockHeader> headerMap = new HashMap<>();
    private Map<String, Set<String>> calcMap = new HashMap<>();
    private BestBlockInfo bestBlockInfo;

    public List<String> getPeerIdList() {
        return peerIdList;
    }

    public void request() {
        headerMap.clear();
        calcMap.clear();
        AskBestBlockEvent askBestBlockEvent = new AskBestBlockEvent();
        peerIdList = this.eventService.broadcast(askBestBlockEvent);
        if (peerIdList.isEmpty()) {
            Log.error("get best height from net faild!");
            throw new NulsRuntimeException(ErrorCode.NET_MESSAGE_ERROR, "get best height from net faild!");
        }
    }

    public long getHeight() {
        return getBestBlockInfo().getHeight();
    }

    public NulsDigestData getHash() {
        return getBestBlockInfo().getHash();
    }

    public void addBlockHeader(String peerId, BlockHeader header) {
        if(!headerMap.containsKey(peerId)){
            return;
        }
        headerMap.put(peerId, header);
        Set<String> peers = calcMap.get(header.getHash().getDigestHex());
        if (null == peers) {
            peers = new HashSet<>();
        }
        peers.add(peerId);
        calcMap.put(header.getHash().getDigestHex(), peers);
        calc();
    }

    private void calc() {
        if (null == peerIdList || peerIdList.isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "success list of peers is empty!");
        }

        int size = peerIdList.size();
        int halfSize = (size + 1) / 2;
        if (headerMap.size() < halfSize) {
            return;
        }
        BestBlockInfo result = null;
        for (String hash : calcMap.keySet()) {
            Set<String> peers = calcMap.get(hash);
            if (peers.size() > halfSize) {
                result = new BestBlockInfo();
                BlockHeader header = headerMap.get(result.getPeerIdList().get(0));
                result.setHash(header.getHash());
                result.setHeight(header.getHeight());
                result.setPeerIdList(peers);
                result.setFinished(true);
                break;
            }
        }
        if (null != result) {
            bestBlockInfo = result;
        } else if (size == calcMap.size()) {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            this.request();
        }

    }

    public BestBlockInfo getBestBlockInfo() {
        while (true) {
            if (null != bestBlockInfo && bestBlockInfo.isFinished()) {
                break;
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
        return bestBlockInfo;
    }
}
