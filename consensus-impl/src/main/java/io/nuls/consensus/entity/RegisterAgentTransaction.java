package io.nuls.consensus.entity;

import io.nuls.consensus.constant.POCConsensusConstant;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RegisterAgentTransaction extends AbstractConsensusTransaction<Agent> {

    public RegisterAgentTransaction( ) {
        super(POCConsensusConstant.TX_TYPE_REGISTER_AGENT);
    }


}
