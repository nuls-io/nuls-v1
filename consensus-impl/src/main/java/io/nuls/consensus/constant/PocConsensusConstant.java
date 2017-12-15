package io.nuls.consensus.constant;

import io.nuls.core.chain.entity.Na;

/**
 * @author Niels
 * @date 2017/11/8
 */
public interface PocConsensusConstant {
    //version
    int POC_CONSENSUS_MODULE_VERSION = 0;
    int MINIMUM_VERSION_SUPPORTED = 0;


    String CFG_CONSENSUS_SECTION = "Consensus";
    String PROPERTY_DELEGATE_PEER = "delegate-peer";

    short EVENT_TYPE_RED_PUNISH = 6;
    short EVENT_TYPE_YELLOW_PUNISH = 7;
    short EVENT_TYPE_REGISTER_AGENT = 8;
    short EVENT_TYPE_ASK_BLOCK = 9;


    int TX_TYPE_REGISTER_AGENT = 10;
    int TX_TYPE_RED_PUNISH = 11;
    int TX_TYPE_YELLOW_PUNISH = 12;

    /**
     * TODO THE PARAMETERS OF CONSENSUS,bellow
     */
    int CONFIRM_BLOCK_COUNT = 6;
    /**
     * Set temporarily as a fixed value,unit:nuls
     */
    int BLOCK_COUNT_OF_YEAR = 3154600;
    /**
     * value = 5000000/3154600
     */
    double BLOCK_REWARD = 1.585;
    Na TRANSACTION_FEE = Na.CENT;
    /**
     * unit:second
     */
    int BLOCK_TIME_INTERVAL = 10;

    /**
     * default:2M
     */
    long MAX_BLOCK_SIZE = 2 << 21;


    double AGENT_DEPOSIT_LOWER_LIMIT = 20000;
    double ENTRUSTER_DEPOSIT_LOWER_LIMIT = 1000;

    double SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT = 50000;
    double SUM_OF_DEPOSIT_OF_AGENT_UPPER_LIMIT = 200000;
    /**
     * unit: %
     */
    double AGENT_FORCED_EXITED_RATE = 70;
    /**
     * commission rate,UNIT:%
     */
    double MAX_COMMISSION_RATE = 20;
    double MIN_COMMISSION_RATE = 0;
    /**
     * unit:day
     */
    long RED_PUNISH_DEPOSIT_LOCKED_TIME = 90;
    long YELLOW_PUNISH_DEPOSIT_LOCKED_TIME = 3;

    /**
     * credit parameters
     */
    /**
     * unit:round of consensus
     */
    int RANGE_OF_CAPACITY_COEFFICIENT = 100;
    /**
     * Penalty coefficient,greater than 4.
     */
    int CREDIT_MAGIC_NUM = 4;
}
