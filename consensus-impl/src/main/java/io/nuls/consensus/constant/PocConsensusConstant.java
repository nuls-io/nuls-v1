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
     * Minimum safe quantity of consensus agents
     */
    int SAFELY_CONSENSUS_COUNT = 40;
    /**
     * Set temporarily as a fixed value,unit:nuls
     */
    int BLOCK_COUNT_OF_YEAR = 3153600;
    /**
     * value = 5000000/3154600
     */
    double BLOCK_REWARD = 1.5855;
    Na TRANSACTION_FEE = Na.CENT;
    /**
     * unit:second
     */
    int BLOCK_TIME_INTERVAL = 10;

    /**
     * default:2M
     */
    long MAX_BLOCK_SIZE = 2 << 21;

    Na AGENT_DEPOSIT_LOWER_LIMIT = Na.parseNa(20000);
    Na ENTRUSTER_DEPOSIT_LOWER_LIMIT = Na.parseNa(2000);

    Na SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT = Na.parseNa(200000);
    Na SUM_OF_DEPOSIT_OF_AGENT_UPPER_LIMIT = Na.parseNa(500000);
    /**
     * unit: %
     */
    double AGENT_FORCED_EXITED_RATE = 70;
    /**
     * commission rate,UNIT:%
     */
    double DEFAULT_COMMISSION_RATE = 20;
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
