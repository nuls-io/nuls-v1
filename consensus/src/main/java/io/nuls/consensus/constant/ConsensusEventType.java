package io.nuls.consensus.constant;

/**
 * @author Niels
 * @date 2017/12/1
 */
public interface ConsensusEventType {
    short GET_SMALL_BLOCK = 1;
    short SMALL_BLOCK = 2;
    short GET_BLOCK = 3;
    short BLOCK = 4;
    short GET_BLOCK_HEADER = 5;
    short BLOCK_HEADER = 6;
    short GET_TX_GROUP = 7;
    short TX_GROUP = 8;
}
