package io.nuls.notify.handler;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.handler.AbstractEventHandler;

/**
 * @author daviyang35
 * @date 2018/1/19
 */
public class EventsHandler extends AbstractEventHandler<BaseEvent> {
    @Override
    public void onEvent(BaseEvent event, String fromId) {
        Log.info(event.toString());
    }
}
