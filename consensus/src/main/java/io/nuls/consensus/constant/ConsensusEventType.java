package io.nuls.consensus.constant;

/**
 * @author Niels
 * @date 2017/12/1
 */
public interface ConsensusEventType {
    short JOIN = 1;
    short EXIT = 2;
    short BLOCK = 3;
    short BLOCK_HEADER = 4;
    short GET_BLOCK = 5;
}
