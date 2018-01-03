package io.nuls.network.message.filter;


import io.nuls.core.mesasge.NulsMessage;

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


    void addFilter(NulsMessageFilter filter) {
        filters.add(filter);
    }

    void deleteFilter(NulsMessageFilter filter) {
        if (filters.contains(filter)) {
            filters.remove(filter);
        }
    }

    public boolean doFilter(NulsMessage message) {
        for (int i = 0; i < filters.size(); i++) {
            if (!filters.get(i).filter(message)) {
                return false;
            }
        }
        return true;
    }
}
