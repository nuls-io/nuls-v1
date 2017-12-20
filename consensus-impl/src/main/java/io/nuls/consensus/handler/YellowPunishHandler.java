package io.nuls.consensus.handler;

import io.nuls.consensus.event.RedPunishConsensusEvent;
import io.nuls.consensus.event.YellowPunishConsensusEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class YellowPunishHandler extends AbstractNetworkNulsEventHandler<YellowPunishConsensusEvent> {

    @Override
    public void onEvent(YellowPunishConsensusEvent event,String fromId)  {
        //todo

    }
}
