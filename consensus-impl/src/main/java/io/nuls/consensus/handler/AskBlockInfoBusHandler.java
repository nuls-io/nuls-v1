package io.nuls.consensus.handler;

import io.nuls.consensus.event.AskBlockInfoEvent;
import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.event.bus.bus.service.intf.EventBroadcaster;

/**
 * @author Niels
 * @date 2017/12/12
 */
public class AskBlockInfoBusHandler extends AbstractEventBusHandler<AskBlockInfoEvent> {

    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);
    @Override
    public void onEvent(AskBlockInfoEvent event, String fromId)   {
        BlockHeader header ;
        if(null==event.getEventBody()||event.getEventBody().getVal()==0){
            header = blockService.getLocalBestBlock().getHeader();
        }else{
            Block block = blockService.getBlockByHeight(event.getEventBody().getVal());
            if(null==block){
                header = new BlockHeader();
                header.setHeight(event.getEventBody().getVal());
            }else{
                header = block.getHeader();
            }
        }
        this.eventBroadcaster.sendToPeer(new BlockHeaderEvent(header),fromId);
    }
}
