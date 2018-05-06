package io.nuls.message.bus.filter;

import io.nuls.protocol.message.base.BaseMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/5/6
 */
public class NulsMessageFilterChain {
    private List<NulsMessageFilter> list = new ArrayList<>();
    private ThreadLocal<Integer> index = new ThreadLocal<>();

    public boolean startDoFilter(BaseMessage message) {
        index.set(-1);
        doFilter(message);
        boolean result = index.get() == list.size();
        index.remove();
        return result;

    }

    public void doFilter(BaseMessage message) {
        index.set(1 + index.get());
        if (index.get() == list.size()) {
            return;
        }
        NulsMessageFilter filter = list.get(index.get());
        filter.doFilter(message, this);
    }

    public void addFilter(NulsMessageFilter<? extends BaseMessage> filter) {
        list.add(0, filter);
    }
}
