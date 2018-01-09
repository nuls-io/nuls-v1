package io.nuls.consensus.constant;

import io.nuls.core.chain.entity.Na;

/**
 * @author Niels
 */
public interface PocConsensusConstant {
    //version
    int POC_CONSENSUS_MODULE_VERSION = 0;
    int MINIMUM_VERSION_SUPPORTED = 0;


    String CFG_CONSENSUS_SECTION = "Consensus";
    String PROPERTY_PARTAKE_PACKING = "partake.packing";
    String PROPERTY_SEED_NODES = "seed.nodes";
    String SEED_NODES_DELIMITER = ",";

    String DEFAULT_CONSENSUS_LIST_FILE = "default-consensus-list.properties";
    String GENESIS_BLOCK_FILE = "genesis-block.json";

    short EVENT_TYPE_JOIN_CONSENSUS = 11;
    short EVENT_TYPE_EXIT_CONSENSUS = 12;
    short EVENT_TYPE_REGISTER_AGENT = 13;
    short EVENT_TYPE_RED_PUNISH = 10;
    short EVENT_TYPE_YELLOW_PUNISH = 13;

    /**
     * TODO THE PARAMETERS OF CONSENSUS,bellow
     */
    int CONFIRM_BLOCK_COUNT = 6;
    int MIN_CONSENSUS_AGENT_COUNT = 30;
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

    Na AGENT_DEPOSIT_LOWER_LIMIT = Na.parseNuls(20000);
    Na ENTRUSTER_DEPOSIT_LOWER_LIMIT = Na.parseNuls(2000);

    Na SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT = Na.parseNuls(200000);
    Na SUM_OF_DEPOSIT_OF_AGENT_UPPER_LIMIT = Na.parseNuls(500000);
    /**
     * Annual inflation
     */
    Na ANNUAL_INFLATION = Na.parseNuls(5000000);
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
