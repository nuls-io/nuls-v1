package io.nuls.message.bus.handler;

import io.nuls.message.bus.filter.NulsMessageFilter;
import io.nuls.message.bus.filter.NulsMessageFilterChain;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * 消息处理器的实现类(抽象的)
 * Message processor implementation class (abstract)
 * @author: Charlie
 * @date: 2018/5/6
 */
public abstract class AbstractMessageHandler<T extends BaseMessage> implements NulsMessageHandler<T> {

    private NulsMessageFilterChain filterChain = new NulsMessageFilterChain();

    @Override
    public void addFilter(NulsMessageFilter<T> filter) {
        filterChain.addFilter(filter);
    }

    @Override
    public NulsMessageFilterChain getFilterChian() {
        return filterChain;
    }
}
