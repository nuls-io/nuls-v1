package io.nuls.consensus.handler;

import io.nuls.consensus.event.YellowPunishConsensusEvent;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class YellowPunishHandler extends AbstractEventBusHandler<YellowPunishConsensusEvent> {

    @Override
    public void onEvent(YellowPunishConsensusEvent event,String fromId)  {
        //todo

    }
}
