/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
    //todo
    long BLOCK_TIME_INTERVAL_SECOND = 5;

    /**
     * 出块间隔时间（毫秒）
     * Block interval time.
     * unit:millis
     */
    long BLOCK_TIME_INTERVAL_MILLIS = BLOCK_TIME_INTERVAL_SECOND * 1000L;

    /**
     * 协议模块节点id
     * module id of the protocol module
     */
    short MODULE_ID_PROTOCOL = 3;

    /**
     * 系统运行的最小连接节点数量
     * The number of minimum connection nodes that the system runs.
     */
    //todo
    int ALIVE_MIN_NODE_COUNT = 1;

    /**
     * 最大区块大小（不包含区块头）
     * Maximum block size (excluding block headers)
     */
    long MAX_BLOCK_SIZE = 2 * 1024 * 1024L;

    /**
     * 协议模块的所有消息类型定义
     * All message type definitions for the protocol module.
     * =======================================================================
     */
    /**
     * "数据找不到应"答消息的类型
     * The data cannot find the type of answer.
     */
    short MESSAGE_TYPE_NOT_FOUND = 1;
    /**
     * 新交易发送及转发的消息类型
     * The type of message that the new transaction sends and forwards.
     */
    short MESSAGE_TYPE_NEW_TX = 2;
    /**
     * 获取区块的消息的类型
     * Gets the type of message for the block.
     */
    short MESSAGE_TYPE_GET_BLOCK = 3;
    /**
     * 发送区块的消息的类型
     * The type of message to send the block.
     */
    short MESSAGE_TYPE_BLOCK = 4;
    /**
     * 根据区块hash获取区块列表的消息类型
     * The type of message to get the blocks by hash.
     */
    short MESSAGE_TYPE_GET_BLOCKS_BY_HASH = 5;

    /**
     * 根据区块高度获取区块列表的消息类型
     * The type of message to get the blocks by height.
     */
    short MESSAGE_TYPE_GET_BLOCKS_BY_HEIGHT = 6;
    /**
     * 获取区块头的消息的类型
     * Gets the type of message for the block-header.
     */
    short MESSAGE_TYPE_GET_BLOCK_HEADER = 7;
    /**
     * 发送区块头的消息的类型
     * The type of message to send the block-header.
     */
    short MESSAGE_TYPE_BLOCK_HEADER = 8;
    /**
     * 获取交易或交易列表的消息的类型
     * Gets the type of message for the transactions.
     */
    short MESSAGE_TYPE_GET_TX_GROUP = 9;
    /**
     * 发送交易或交易列表的消息的类型
     * The type of message to send the transactions.
     */
    short MESSAGE_TYPE_TX_GROUP = 10;
    /**
     * 新区块发送及转发的消息类型
     * The type of message that the new SmallBlock sends and forwards.
     */
    short MESSAGE_TYPE_NEW_BLOCK = 11;
    /**
     * 获取区块hash或hash列表的消息的类型
     * Gets the type of message for the Blocks hashes.
     */
    short MESSAGE_TYPE_GET_BLOCKS_HASH = 12;
    /**
     * 发送区块hash的消息的类型
     * The type of message to send the Blocks hashes.
     */
    short MESSAGE_TYPE_BLOCKS_HASH = 13;
    /**
     * 发送字符串给对等节点的消息的类型
     * The type of message that is sent to a peer.
     */
    short MESSAGE_TYPE_STRING = 14;
    /**
     * 任务完成的消息类型
     * task complete message
     */
    short MESSAGE_TYPE_COMPLETE = 15;
    /**
     * 请求回复消息类型，用于需要立刻知道目标节点是否接收到本次请求的情况
     * Request reply message type, used to immediately know if the target node received this request
     */
    short REQUEST_REACT = 16;
}
