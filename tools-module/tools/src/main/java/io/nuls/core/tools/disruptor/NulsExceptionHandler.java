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

package io.nuls.core.tools.disruptor;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.LifecycleAware;
import io.nuls.core.tools.log.Log;

/**
 * @author: Niels Wang
 */
public class NulsExceptionHandler implements ExceptionHandler<Object> {
    /**
     * <p>Strategy for handling uncaught exceptions when processing an event.</p>
     * <p>
     * <p>If the strategy wishes to terminate further processing by the {@link BatchEventProcessor}
     * then it should throw a {@link RuntimeException}.</p>
     *
     * @param ex       the exception that propagated from the {@link EventHandler}.
     * @param sequence of the event which cause the exception.
     * @param event    being processed when the exception occurred.  This can be null.
     */
    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        Log.error(ex);

    }

    /**
     * Callback to notify of an exception during {@link LifecycleAware#onStart()}
     *
     * @param ex throw during the starting process.
     */
    @Override
    public void handleOnStartException(Throwable ex) {
        Log.error(ex);
    }

    /**
     * Callback to notify of an exception during {@link LifecycleAware#onShutdown()}
     *
     * @param ex throw during the shutdown process.
     */
    @Override
    public void handleOnShutdownException(Throwable ex) {
        Log.error(ex);
    }
}
