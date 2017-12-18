package io.nuls.account.event.handler;

import io.nuls.account.entity.event.AliasEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasEventHandler extends AbstractNetworkNulsEventHandler<AliasEvent> {

    private static AliasEventHandler handler = new AliasEventHandler();

    private AliasEventHandler(){

    }

    public static AliasEventHandler getInstance(){
        return handler;
    }

    @Override
    public void onEvent(AliasEvent event, String fromId) throws NulsException {
        //todo


    }
}