package io.nuls.network.message.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.utils.date.TimeService;
import io.nuls.db.dao.NodeDataService;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeTransfer;
import io.nuls.network.exception.NetworkMessageException;
import io.nuls.network.message.NetworkCacheService;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.VersionEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class VersionEventHandler implements NetWorkEventHandler {

    private static final VersionEventHandler INSTANCE = new VersionEventHandler();

    private NodeDataService nodeDao;

    private NetworkCacheService cacheService;

    private VersionEventHandler() {
        cacheService = NetworkCacheService.getInstance();
    }

    public static VersionEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseNetworkEvent networkEvent, Node node) {
        VersionEvent event = (VersionEvent) networkEvent;

        String key = event.getHeader().getEventType() + "-" + node.getIp() + "-" + node.getPort();
        if (cacheService.existEvent(key)) {
            node.destroy();
            return null;
        }
        cacheService.putEvent(key, event, false);

        if (event.getBestBlockHeight() < 0) {
            throw new NetworkMessageException(ErrorCode.NET_MESSAGE_ERROR);
        }
        node.setVersionMessage(event);
        node.setStatus(Node.HANDSHAKE);
        node.setLastTime(TimeService.currentTimeMillis());

        getNodeDao().saveChange(NodeTransfer.toPojo(node));
        return null;
    }

    private NodeDataService getNodeDao() {
        if (nodeDao == null) {
            nodeDao = NulsContext.getInstance().getService(NodeDataService.class);
        }
        return nodeDao;
    }
}
