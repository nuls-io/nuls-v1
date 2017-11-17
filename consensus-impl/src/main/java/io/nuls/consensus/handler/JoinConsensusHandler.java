package io.nuls.consensus.handler;

import io.nuls.core.event.NulsEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.filter.NulsEventFilter;
import io.nuls.event.bus.event.filter.NulsEventFilterChain;
import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;
import io.nuls.event.bus.event.handler.intf.NulsEventHandler;

/**
 * Created by facjas on 2017/11/16.
 */
public class JoinConsensusHandler extends NetworkNulsEventHandler {

    @Override
    public void onEvent(NulsEvent event) throws NulsException {

    }
}
