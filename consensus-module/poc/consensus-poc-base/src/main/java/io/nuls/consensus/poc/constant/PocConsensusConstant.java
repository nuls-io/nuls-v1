/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.constant;

import io.nuls.kernel.model.Na;

/**
 * @author ln
 * @date 2018/4/22
 */
public interface PocConsensusConstant {

    /**
     * Coinbase rewards the number of locked blocks
     * coinbase奖励的锁定块数
     */
    int COINBASE_UNLOCK_HEIGHT = 1000;

    /**
     * value = 5000000/3154600
     */
    Na BLOCK_REWARD = Na.valueOf(158548960);

    /**
     * Maximum height difference handled by furcation blocks
     * 当分叉链高度超过主链多少高度时，触发切换主链的操作
     */
    int CHANGE_CHAIN_BLOCK_DIFF_COUNT = 3;

    /**
     * Maximum height difference handled by furcation blocks , Blocks that exceed this difference will be discarded directly
     * 分叉块处理的最大高度差值，超过这个差值的区块，会直接丢弃掉
     */
    int MAX_ISOLATED_BLOCK_COUNT = 1000;

    /**
     * How long does the current network time exceed the number of blocks that are discarded directly, in milliseconds
     * 超过当前网络时间多久的区块，直接丢弃掉，单位毫秒
     */
    long DISCARD_FUTURE_BLOCKS_TIME = 60 * 1000L;

    /**
     * Load the block header of the last specified number of rounds during initialization
     * 初始化时加载最近指定轮数的区块头
     */
    int INIT_HEADERS_OF_ROUND_COUNT = 200;

    /**
     * When the system starts up, load the newly specified number of blocks into memory
     * 系统启动时，加载最新指定数量的区块到内存里面
     */
    int INIT_BLOCKS_COUNT = 10;

    /**
     * Consensus memory expiration data cleaning interval, in milliseconds
     * 共识内存过期数据清理间隔时间，单位毫秒
     */
    long CLEAR_INTERVAL_TIME = 60000L;

    /**
     * Regularly clear the round before the specified number of rounds of the main chain
     * 定期清理主链指定轮数之前的轮次信息
     */
    int CLEAR_MASTER_CHAIN_ROUND_COUNT = 5;

    /**
     * The maximum continuous number of yellow punish log.
     */
    int MAXINUM_CONTINUOUS_YELLOW_NUMBER = 100;

    /**
     * reset system time interval , unit minutes
     */
    int RESET_SYSTEM_TIME_INTERVAL = 5;
    long CONSENSUS_LOCK_TIME = -1;
    long STOP_AGENT_LOCK_TIME = 3 * 24 * 3600000L;
}
