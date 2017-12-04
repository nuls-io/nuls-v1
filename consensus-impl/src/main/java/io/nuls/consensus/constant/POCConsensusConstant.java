package io.nuls.consensus.constant;

/**
 *
 * @author Niels
 * @date 2017/11/8
 *
 */
public interface POCConsensusConstant {
    //todo version
    int POC_CONSENSUS_MODULE_VERSION = 1111;
    //Minimum version supported
    int MINIMUM_VERSION_SUPPORTED = 0;


    String CFG_CONSENSUS_SECTION = "Consensus";
    String CFG_CONSENSUS_MINING_AGENT = "mining.agent";


    short EVENT_TYPE_RED_PUNISH = 6;
    short EVENT_TYPE_YELLOW_PUNISH = 7;
}
