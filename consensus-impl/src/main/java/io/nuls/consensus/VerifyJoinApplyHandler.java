package io.nuls.consensus;

import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.event.bus.event.handler.NulsEventHandler;

/**
 * Created by Niels on 2017/11/13.
 * nuls.io
 */
public class VerifyJoinApplyHandler extends NulsEventHandler<JoinConsensusEvent> {
    //todo
    @Override
    public void onEvent(JoinConsensusEvent event) {

        //check address
        //check status
        //check credit
        //check balance
//        if(ok){

//        }
    }
}
