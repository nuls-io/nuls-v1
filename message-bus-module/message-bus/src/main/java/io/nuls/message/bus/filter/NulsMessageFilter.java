package io.nuls.message.bus.filter;

import io.nuls.protocol.message.base.BaseMessage;

/**
 * Nuls事件过滤器
 * The Nuls event filter.
 *
 * @author: Charlie
 * @date: 2018/5/6
 */
public interface NulsMessageFilter<T extends BaseMessage> {

    void doFilter(T data, NulsMessageFilterChain chain);
}
