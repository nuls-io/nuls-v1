package io.nuls.event.bus.event.filter;

import io.nuls.core.event.NulsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niels on 2017/11/7.
 * nuls.io
 */
public class NulsEventFilterChain {

    private List<NulsEventFilter> list = new ArrayList<>();
    private ThreadLocal<Integer> index = new ThreadLocal<>();

    public boolean startDoFilter(NulsEvent event) {
        index.set(-1);
        doFilter(event);
        return index.get() == list.size();
    }

    public void doFilter(NulsEvent event) {
        index.set(1 + index.get());
        if (index.get() == list.size()) {
            return;
        }
        NulsEventFilter filter = list.get(index.get());
        filter.doFilter(event, this);
    }

    public void addFilter(NulsEventFilter<? extends NulsEvent> filter) {
        list.add(filter);
    }
}
