/**
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
 */
package io.nuls.db.dao;

import io.nuls.core.dto.Page;
import io.nuls.db.entity.BlockHeaderPo;

import java.util.List;


/**
 * @author zhouwei
 * @date 2017/9/29
 */
public interface BlockHeaderService extends BaseDataService<String, BlockHeaderPo> {

    /**
     * get blockheanderpo from block height
     *
     * @param height
     * @return
     */
    BlockHeaderPo getHeader(long height);

    /**
     * get blockheanderpo from block hash
     *
     * @param hash
     * @return
     */
    BlockHeaderPo getHeader(String hash);

    /**
     * return highest height
     *
     * @return
     */
    long getBestHeight();

    /**
     * blockheanderpo of highest height
     *
     * @return
     */
    BlockHeaderPo getBestBlockHeader();

    /**
     * get blockheanderpo list from block height
     *
     * @param startHeight
     * @param endHeight
     * @return
     */
    List<BlockHeaderPo> getHeaderList(long startHeight, long endHeight);


    /**
     * Piecewise get the hashes of blocks;
     *
     * @param startHeight
     * @param endHeight
     * @param split
     * @return
     */
    List<BlockHeaderPo> getHashList(long startHeight, long endHeight, long split);

    Page<BlockHeaderPo> getBlockListByAddress(String nodeAddress, int type, int pageNumber, int pageSize);

    Page<BlockHeaderPo> getBlockHeaderList(int pageNumber, int pageSize);

    /**
     * calc count of roundIndex between start and end;
     *
     * @param address
     * @param roundStart
     * @param roundEnd
     * @return
     */
    long getCount(String address, long roundStart, long roundEnd);

    /**
     * use for POC consensus
     *
     * @param address
     * @param endRoundIndex
     * @return
     */
    List<Long> getListOfRoundIndexOfYellowPunish(String address, long startRoundIndex, long endRoundIndex);
}
