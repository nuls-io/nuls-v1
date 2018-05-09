/*
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
 *
 */
package io.nuls.protocol.base.handler;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.network.entity.Node;
import io.nuls.protocol.base.download.cache.DownloadCacheHandler;
import io.nuls.protocol.message.TxGroupMessage;
import io.nuls.protocol.model.TxGroup;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class TxGroupHandler extends AbstractMessageHandler<TxGroupMessage> {


    @Override
    public void onMessage(TxGroupMessage message, Node fromNode) throws NulsException {
        TxGroup txGroup = message.getMsgBody();
        if (null == txGroup) {
            Log.warn("recieved a null txGroup form " + fromNode.getId());
            return;
        }
        DownloadCacheHandler.receiveTxGroup(txGroup);
    }
}
