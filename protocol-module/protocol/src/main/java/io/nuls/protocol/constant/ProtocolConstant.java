package io.nuls.protocol.constant;

import io.nuls.kernel.constant.NulsConstant;

/**
 * 协议相关的常量和一些通用的常量定义在这里
 * The relevant constants of the protocol and some general constants are defined here.
 *
 * @author: Niels Wang
 * @date: 2018/4/17
 */
public interface ProtocolConstant extends NulsConstant {
    /**
     * 出块间隔时间（秒）
     * Block interval time.
     * unit:second
     */
    long BLOCK_TIME_INTERVAL_SECOND = 10;

    /**
     * 出块间隔时间（毫秒）
     * Block interval time.
     * unit:millis
     */
    long BLOCK_TIME_INTERVAL_MILLIS = BLOCK_TIME_INTERVAL_SECOND * 1000L;

    short MODULE_ID_PROTOCOL = 2;

    /**
     * 协议模块的所有消息类型定义
     * All message type definitions for the protocol module.
     * =======================================================================
     */
    /**
     * "数据找不到应"答消息的类型
     * The data cannot find the type of answer.
     */
    short NOT_FOUND_MESSAGE = 1;
    /**
     * 新交易发送及转发的消息类型
     * The type of message that the new transaction sends and forwards.
     */
    short NEW_TX_MESSAGE = 2;
    /**
     * 获取区块的消息的类型
     * Gets the type of message for the block.
     */
    short GET_BLOCK = 3;
    /**
     * 发送区块的消息的类型
     * The type of message to send the block.
     */
    short BLOCK = 4;
    /**
     * 获取区块头的消息的类型
     * Gets the type of message for the block-header.
     */
    short GET_BLOCK_HEADER = 5;
    /**
     * 发送区块头的消息的类型
     * The type of message to send the block-header.
     */
    short BLOCK_HEADER = 6;
    /**
     * 获取交易或交易列表的消息的类型
     * Gets the type of message for the transactions.
     */
    short GET_TX_GROUP = 7;
    /**
     * 发送区块头的消息的类型
     * The type of message to send the transactions.
     */
    short TX_GROUP = 8;
    /**
     * 新区块发送及转发的消息类型
     * The type of message that the new SmallBlock sends and forwards.
     */
    short NEW_BLOCK = 9;
    /**
     * 获取区块hash或hash列表的消息的类型
     * Gets the type of message for the Blocks hashes.
     */
    short GET_BLOCKS_HASH = 10;
    /**
     * 发送区块hash的消息的类型
     * The type of message to send the Blocks hashes.
     */
    short BLOCKS_HASH = 11;
}
