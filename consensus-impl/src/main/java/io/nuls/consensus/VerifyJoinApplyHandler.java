package io.nuls.consensus;

import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.event.bus.event.handler.NetworkNulsEventHandler;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class VerifyJoinApplyHandler extends NetworkNulsEventHandler<JoinConsensusEvent> {
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
