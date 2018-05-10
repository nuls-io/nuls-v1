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
package io.nuls.network.message.filter;


import io.nuls.protocol.message.base.BaseMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class MessageFilterChain {

    private static MessageFilterChain messageFilterChain = new MessageFilterChain();

    protected List<NulsMessageFilter> filters;

    private MessageFilterChain() {
        filters = new ArrayList<>();
    }

    public static MessageFilterChain getInstance() {
        return messageFilterChain;
    }


    public void addFilter(NulsMessageFilter filter) {
        filters.add(filter);
    }

    public void deleteFilter(NulsMessageFilter filter) {
        if (filters.contains(filter)) {
            filters.remove(filter);
        }
    }

    public boolean doFilter(BaseMessage message) {
        for (int i = 0; i < filters.size(); i++) {
            if (!filters.get(i).filter(message)) {
                return false;
            }
        }
        return true;
    }
}
