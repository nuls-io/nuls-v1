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

package io.nuls.protocol.base.download.thread;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.message.GetBlocksByHeightMessage;

import java.util.List;

/**
 * @author Niels
 */
public class RequestThread implements Runnable {

    private List<Node> nodeList;
    private long startHeight;
    private long endHeight;
    private int randomIndex = 1;

    private MessageBusService service = NulsContext.getServiceBean(MessageBusService.class);

    private static final int count = 10;
    private boolean running = true;

    public RequestThread(List<Node> nodeList, long startHeight, long endHeight) {
        this.nodeList = nodeList;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
    }

    @Override
    public void run() {
        this.running = true;
        while (true) {
            try {
                if (startHeight > endHeight) {
                    this.init();
                    break;
                }
                if (this.nodeList == null || this.nodeList.isEmpty()) {
                    break;
                }
                if (startHeight - NulsContext.getInstance().getBestHeight() < 5000) {
                    downloadRound();
                    continue;
                } else {
                    for (int i = nodeList.size() - 1; i >= 0; i--) {
                        Node node = nodeList.get(i);
                        if (!node.isHandShake()) {
                            nodeList.remove(i);
                        }
                    }
                }
                Thread.sleep(10L);
            } catch (Exception e) {
                Log.error(e);
            }
        }
        this.running = false;
    }

    private void init() {
        this.startHeight = 0;
        this.endHeight = 0;
        this.randomIndex = 1;
    }

    private void downloadRound() {
        for (int i = nodeList.size() - 1; i >= 0; i--) {
            Node node = nodeList.get(i);
            if (!node.isHandShake()) {
                nodeList.remove(i);
                continue;
            }
            int size = count;
            if ((startHeight + size) > endHeight) {
                size = (int) (endHeight - startHeight + 1);
            }
            if (size <= 0) {
                break;
            }
//            Log.info("request:{}-{} ,from {}.", startHeight, size, node.getId());
            boolean result = request(node, startHeight, size);
            if (result) {
                startHeight += size;
            }
        }
    }

    private boolean request(Node node, long start, int size) {
        GetBlocksByHeightMessage message = new GetBlocksByHeightMessage(start, start + size - 1);
        Result result = service.sendToNode(message, node, true);
        return result.isSuccess();
    }

    public boolean retryDownload(long start, int size) {
        if (nodeList.isEmpty()) {
            return false;
        }
        Node node = nodeList.get(randomIndex++ % nodeList.size());
        request(node, start, size);
        return true;
    }

    public boolean isStoped() {
        return !this.running;
    }

    public long getStartHeight() {
        return startHeight;
    }
}
