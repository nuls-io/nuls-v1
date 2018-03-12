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

package io.nuls.notify.handler;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.notify.controller.NulsEventDelegate;

/**
 * @author daviyang35
 * @date 2018/1/19
 */
public class NulsEventHandler extends AbstractEventHandler<BaseEvent> {
    public NulsEventDelegate getEventDelegate() {
        return eventDelegate;
    }

    public void setEventDelegate(NulsEventDelegate eventDelegate) {
        this.eventDelegate = eventDelegate;
    }

    private NulsEventDelegate eventDelegate;

    @Override
    public void onEvent(BaseEvent event, String fromId) {
        if(null==event||event.getNotice()==null){
            return;
        }
        try {
            Log.debug(JSONUtils.obj2json(event.getNotice()));
//            event.getHeader()
//                    event.getNotice()
//                            event.getClass().getSimpleName()
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
