/**
 * MIT License
 **
 * Copyright (c) 2017-2018 nuls.io
 **
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 **
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 **
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.message.impl;

import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.utils.str.VersionUtils;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.base.BaseEvent;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkCacheService;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.GetVersionEvent;
import io.nuls.network.message.entity.VersionEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.model.Block;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionEventHandler implements NetWorkEventHandler {

    private static final GetVersionEventHandler INSTANCE = new GetVersionEventHandler();

    private NetworkCacheService cacheService;

    private NetworkService networkService;

    private GetVersionEventHandler() {
        cacheService = NetworkCacheService.getInstance();
    }

    public static GetVersionEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseEvent networkEvent, Node node) {
        GetVersionEvent event = (GetVersionEvent) networkEvent;

//        String key = event.getHeader().getEventType() + "-" + node.getId();
//        if (cacheService.existEvent(key)) {
//            Log.info("----------GetVersionEventHandler  cacheService  existEvent--------");
//            getNetworkService().removeNode(node.getId());
//            return null;
//        }
//        cacheService.putEvent(key, event, true);

        Block block = NulsContext.getInstance().getBestBlock();
        while (block == null) {
            try {
                Thread.sleep(1000);
                block = NulsContext.getInstance().getBestBlock();
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }

        VersionEvent versionEvent = new VersionEvent(event.getSeverPort(), event.getBestBlockHeight(), event.getBestBlockHash());
        node.setVersionMessage(versionEvent);
        checkVersion(event.getNulsVersion());

        VersionEvent replyMessage = new VersionEvent(getNetworkService().getNetworkParam().getPort(),
                block.getHeader().getHeight(), block.getHeader().getHash().getDigestHex());

        return new NetworkEventResult(true, replyMessage);
    }

    private void checkVersion(String version) {
        if (VersionUtils.higherThan(version,NulsConfig.VERSION)) {
            NulsConfig.NEWEST_VERSION = version;
        }
    }

    private NetworkService getNetworkService() {
        if (networkService == null) {
            networkService = NulsContext.getServiceBean(NetworkService.class);
        }
        return networkService;
    }
}
