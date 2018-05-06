package io.nuls.message.bus.handler.intf;

import io.nuls.kernel.exception.NulsException;
import io.nuls.message.bus.filter.NulsMessageFilter;
import io.nuls.message.bus.filter.NulsMessageFilterChain;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * 消息处理器接口
 * @author: Charlie
 * @date: 2018/5/6
 */
public interface NulsMessageHandler<T extends BaseMessage> {

    /**
     * 添加一个过滤器
     * add a filter
     *
     * @param filter
     */
    void addFilter(NulsMessageFilter<T> filter);

    /**
     * 获得过滤器
     * Get a FilterChain
     * @return NulsMessageFilterChain
     */
    NulsMessageFilterChain getFilterChian();

    void onMessage(T message, String formId) throws NulsException;
}
