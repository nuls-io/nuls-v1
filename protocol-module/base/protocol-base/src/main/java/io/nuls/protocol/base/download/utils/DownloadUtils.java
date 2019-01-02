/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
 *
 */

package io.nuls.protocol.base.download.utils;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.base.cache.ProtocolCacheHandler;
import io.nuls.protocol.message.*;
import io.nuls.protocol.model.BlockHashResponse;
import io.nuls.protocol.model.CompleteParam;
import io.nuls.protocol.model.GetTxGroupParam;
import io.nuls.protocol.model.TxGroup;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author ln
 */
public class DownloadUtils {

    private static MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    public static Block getBlockByHash(NulsDigestData hash, Node node) {
        if (hash == null || node == null) {
            return null;
        }
        GetBlockMessage message = new GetBlockMessage(hash);
        Future<Block> future = ProtocolCacheHandler.addGetBlockByHashRequest(hash);
        Future<NulsDigestData> reactFuture = ProtocolCacheHandler.addRequest(hash);
        Result result = messageBusService.sendToNode(message, node, false);
//        Log.error("start request:"+new Date().toLocaleString()+" ::: "+hash);
        if (!result.isSuccess()) {
            ProtocolCacheHandler.removeBlockByHashFuture(hash);
            ProtocolCacheHandler.removeRequest(hash);
            return null;
        }
        try {
            reactFuture.get(1L, TimeUnit.SECONDS);
            Block block = future.get(30L, TimeUnit.SECONDS);
            return block;
        } catch (Exception e) {
            Log.error(node.getId(), e);
            return null;
        } finally {
            ProtocolCacheHandler.removeBlockByHashFuture(hash);
            ProtocolCacheHandler.removeRequest(hash);
        }
    }
}
