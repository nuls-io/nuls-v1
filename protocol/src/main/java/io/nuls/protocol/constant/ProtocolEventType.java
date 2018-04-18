package io.nuls.protocol.constant;

/**
 * @author: Niels Wang
 * @date: 2018/4/17
 */
public interface ProtocolEventType {

    short NOT_FOUND_EVENT = 1;
    short GET_BLOCK = 3;
    short BLOCK = 4;
    short GET_BLOCK_HEADER = 5;
    short BLOCK_HEADER = 6;
    short GET_TX_GROUP = 7;
    short TX_GROUP = 8;
    short NEW_BLOCK = 9;
    short NEW_TX_EVENT = 2;
    short EVENT_TYPE_GET_BLOCKS_HASH = 10;
    short EVENT_TYPE_BLOCKS_HASH = 11;
}
