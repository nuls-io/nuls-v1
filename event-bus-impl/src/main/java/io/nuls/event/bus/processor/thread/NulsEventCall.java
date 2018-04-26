/**
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
 */
package io.nuls.event.bus.processor.thread;

import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.handler.intf.NulsEventHandler;
import io.nuls.event.bus.processor.manager.ProcessData;

/**
 * @author Niels
 * @date 2017/11/6
 */
public class NulsEventCall<T extends io.nuls.protocol.event.base.BaseEvent> implements Runnable {
    private final ProcessData<T> data;
    private final NulsEventHandler<T> handler;

    public NulsEventCall(ProcessData<T> data, NulsEventHandler<T> handler) {
        this.data = data;
        this.handler = handler;
    }

    @Override
    public void run() {
        if (null == data || null == handler) {
            return;
        }
        try {
            //filter&handler is the same level
            boolean ok = handler.getFilterChain().startDoFilter(data.getData());
            if (ok) {
                long start = System.currentTimeMillis();
                handler.onEvent(data.getData(), data.getNodeId());
                Log.debug(data.getData().getClass()+",use:"+(System.currentTimeMillis()-start));
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return;
    }
}
