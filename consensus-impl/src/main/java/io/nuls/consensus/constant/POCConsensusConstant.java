package io.nuls.consensus.constant;

/**
 * @author Niels
 * @date 2017/11/8
 */
public interface POCConsensusConstant {
    //todo version
    int POC_CONSENSUS_MODULE_VERSION = 1111;
    int MINIMUM_VERSION_SUPPORTED = 0;


    String CFG_CONSENSUS_SECTION = "Consensus";
    String PROPERTY_DELEGATE_PEER = "delegate-peer";


    short EVENT_TYPE_RED_PUNISH = 6;
    short EVENT_TYPE_YELLOW_PUNISH = 7;
    short EVENT_TYPE_REGISTER_AGENT = 8;


    int TX_TYPE_REGISTER_AGENT = 10;
    int TX_TYPE_RED_PUNISH = 11;
    int TX_TYPE_YELLOW_PUNISH = 12;

}
