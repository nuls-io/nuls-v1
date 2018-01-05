package io.nuls.network.message.impl;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.GetVersionEvent;
import io.nuls.network.message.entity.VersionEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionEventHandler implements NetWorkEventHandler {

    private static final GetVersionEventHandler INSTANCE = new GetVersionEventHandler();

    private GetVersionEventHandler() {

    }

    public static GetVersionEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseNetworkEvent networkEvent, Node node) {
        GetVersionEvent event = (GetVersionEvent) networkEvent;
        Block block = NulsContext.getInstance().getBestBlock();
        VersionEvent replyMessage = new VersionEvent(block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());
        node.setPort(event.getEventBody().getVal());
        return new NetworkEventResult(true, replyMessage);
    }
}
