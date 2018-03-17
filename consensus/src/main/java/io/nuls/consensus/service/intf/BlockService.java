/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.service.intf;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.db.entity.BlockHeaderPo;

import java.io.IOException;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/10
 */
public interface BlockService {

    Block getGengsisBlock();

    long getLocalHeight();

    long getLocalSavedHeight();

    Block getLocalBestBlock();

    BlockHeader getBlockHeader(long height);

    BlockHeader getBlockHeader(String hash);

    Block getBlock(String hash);

    Block getBlock(long height);

    List<Block> getBlockList(long startHeight, long endHeight);

    void saveBlock(Block block) throws IOException;

    void rollbackBlock(long height) throws NulsException;

    List<BlockHeader> getBlockHeaderList(long start, long end, long split);

    Page<BlockHeaderPo> getBlockHeaderList(String nodeAddress, int type, int pageNumber, int pageSize);

    Page<BlockHeaderPo> getBlockHeaderList(int pageNumber, int pageSize);

    BlockHeader getBlockHeader(NulsDigestData hash);
}
