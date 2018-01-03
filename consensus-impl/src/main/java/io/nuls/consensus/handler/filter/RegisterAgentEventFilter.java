package io.nuls.consensus.handler.filter;

import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class RegisterAgentEventFilter implements NulsEventFilter<RegisterAgentEvent> {
    @Override
    public void doFilter(RegisterAgentEvent event, NulsEventFilterChain chain) {
        //todo
        chain.doFilter(event);
    }
}
