/**
 * MIT License
 **
 * Copyright (c) 2017-2018 nuls.io
 **
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 **
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 **
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.constant;

import io.nuls.core.chain.entity.Na;

/**
 * @author Niels
 */
public interface PocConsensusConstant {

    int ALIVE_MIN_NODE_COUNT = 2;

    String CFG_CONSENSUS_SECTION = "consensus";
    String PROPERTY_PARTAKE_PACKING = "partake.packing";
    String PROPERTY_SEED_NODES = "seed.nodes";
    String SEED_NODES_DELIMITER = ",";

    String GENESIS_BLOCK_FILE = "block/genesis-block.json";

    short EVENT_TYPE_GET_BLOCKS_HASH = 20;
    short EVENT_TYPE_BLOCKS_HASH = 21;

    short NOTICE_PACKED_BLOCK = 22;
    short NOTICE_REGISTER_AGENT = 23;
    short NOTICE_ASSEMBLED_BLOCK = 24;
    short NOTICE_JOIN_CONSENSUS = 25;
    short NOTICE_EXIT_CONSENSUS = 26;
    short NOTICE_CANCEL_CONSENSUS = 27;
    /**
     *   THE PARAMETERS OF CONSENSUS,bellow
     */
    int CONFIRM_BLOCK_COUNT = 6;
    int MIN_CONSENSUS_AGENT_COUNT = 30;
    /**
     * Set temporarily as a fixed value,unit:nuls
     */
    int BLOCK_COUNT_OF_YEAR = 3153600;
    int BLOCK_COUNT_OF_DAY = 8640;
    /**
     * value = 5000000/3154600
     */
    Na BLOCK_REWARD = Na.valueOf(158548960);
    /**
     * unit:second
     */
    int BLOCK_TIME_INTERVAL_SECOND = 10;

    /**
     * default:2M
     */
    long MAX_BLOCK_SIZE = 2 << 21;

    Na AGENT_DEPOSIT_LOWER_LIMIT = Na.parseNuls(20000);
    Na ENTRUSTER_DEPOSIT_LOWER_LIMIT = Na.parseNuls(2000);
    /**
     * Maximum acceptable number of delegate
     */
    int MAX_ACCEPT_NUM_OF_DEPOSIT = 1000;
    int MAX_AGENT_COUNT_OF_ADRRESS = 1;

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
    double MIN_COMMISSION_RATE = 5;
    /**
     * unit:day
     */
    long RED_PUNISH_DEPOSIT_LOCKED_TIME = 90;
    long YELLOW_PUNISH_DEPOSIT_LOCKED_TIME = 3;
    long STOP_AGENT_DEPOSIT_LOCKED_TIME = 3;

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
