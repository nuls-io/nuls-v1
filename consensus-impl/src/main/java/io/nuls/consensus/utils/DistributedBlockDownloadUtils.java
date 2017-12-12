package io.nuls.consensus.utils;

import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.event.service.intf.EventService;
import io.nuls.mq.intf.QueueService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class DistributedBlockDownloadUtils {
    private static final DistributedBlockDownloadUtils INSTANCE = new DistributedBlockDownloadUtils();
    private String queueId = StringUtils.getNewUUID();
    private EventService eventService = NulsContext.getInstance().getService(EventService.class);
    private QueueService<String> queueService = NulsContext.getInstance().getService(QueueService.class);
    private Map<String, Long> peerHeightMap = new HashMap<>();
    private Map<Long, Block> blockMap = new HashMap<>();

    private boolean finished = true;
    private List<String> peerIdList;
    private long startHeight;
    private long endHeight;
    private String heightestHash;

    private DistributedBlockDownloadUtils() {
    }

    public static DistributedBlockDownloadUtils getInstance() {
        return INSTANCE;
    }

    private void init(List<String> peerIdList) {
        this.peerIdList = peerIdList;
        this.queueService.createQueue(queueId, (long) peerIdList.size(), false);
        for (String peerId : peerIdList) {
            this.queueService.offer(queueId, peerId);
        }
        peerHeightMap.clear();
        blockMap.clear();
    }

    public void request(List<String> peerIdList, long startHeight, long endHeight,String heightestHash) throws InterruptedException {
        this.init(peerIdList);
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.heightestHash = heightestHash;
        finished = false;
        for (long i = 0; i <= (endHeight - startHeight); i++) {
            sendRequest(i + startHeight, this.queueService.take(queueId));
        }
    }

    private void sendRequest(long height, String peerId) {
        GetBlockEvent event = new GetBlockEvent();
        event.setEventBody(new BasicTypeData<>(height));
        this.eventService.sendToPeer(event, peerId);
    }


    public boolean recieveBlock(String peerId, Block block) {

//        if (!this.peerIdList.contains(peerId) ||
//                block.getHeader().getHeight() !=) {
//            return false;
//        }
        peerHeightMap.put(peerId, block.getHeader().getHeight());
        blockMap.put(block.getHeader().getHeight(), block);
        this.queueService.offer(queueId, peerId);
        verify();
        return true;
    }

    private void verify() {
        //todo


//        if(){
//            finished();
//        }
    }

    private void finished() {
        this.finished = true;
        this.queueService.destroyQueue(queueId);
    }

    public boolean isFinished() {
        return finished;
    }
}
