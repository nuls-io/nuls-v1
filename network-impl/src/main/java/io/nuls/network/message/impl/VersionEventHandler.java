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
package io.nuls.network.message.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.utils.date.TimeService;
import io.nuls.db.dao.NodeDataService;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeTransferTool;
import io.nuls.network.exception.NetworkMessageException;
import io.nuls.network.message.NetworkCacheService;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.VersionEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.NetworkService;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class VersionEventHandler implements NetWorkEventHandler {

    private static final VersionEventHandler INSTANCE = new VersionEventHandler();

    private NodeDataService nodeDao;

    private NetworkCacheService cacheService;

    private NetworkService networkService;

    private VersionEventHandler() {
        cacheService = NetworkCacheService.getInstance();
    }

    public static VersionEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseEvent networkEvent, Node node) {
        VersionEvent event = (VersionEvent) networkEvent;

//        String key = event.getHeader().getEventType() + "-" + node.getId();
//        if (cacheService.existEvent(key)) {
//            Log.info("----------VersionEventHandler  cacheService  existEvent--------");
//            getNetworkService().removeNode(node.getId());
//            return null;
//        }
//        cacheService.putEvent(key, event, true);

        if (event.getBestBlockHeight() < 0) {
            throw new NetworkMessageException(ErrorCode.NET_MESSAGE_ERROR);
        }
        node.setVersionMessage(event);
        checkVersion(event.getNulsVersion());

        if (!node.isHandShake()) {
            node.setStatus(Node.HANDSHAKE);

            node.setSeverPort(event.getSeverPort());
            node.setLastTime(TimeService.currentTimeMillis());
            getNodeDao().saveChange(NodeTransferTool.toPojo(node));
        }
        return null;
    }


    private void checkVersion(String version) {
        int newVersion = Integer.parseInt(version.replace(".", ""));
        int myVersion = Integer.parseInt(NulsContext.VERSION.replace(".", ""));
        if (newVersion > myVersion) {
            NulsContext.NEWEST_VERSION = version;
        }
    }


    private NodeDataService getNodeDao() {
        if (nodeDao == null) {
            nodeDao = NulsContext.getServiceBean(NodeDataService.class);
        }
        return nodeDao;
    }

    private NetworkService getNetworkService() {
        if (networkService == null) {
            networkService = NulsContext.getServiceBean(NetworkService.class);
        }
        return networkService;
    }

}
