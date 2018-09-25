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
package io.nuls.consensus.poc.storage.po;

import io.nuls.kernel.model.BlockHeader;

/**
 * @author: Charlie
 * @date: 2018/9/4
 */
public class EvidencePo {

    private long roundIndex;
    private BlockHeader blockHeader1;
    private BlockHeader blockHeader2;

    public EvidencePo(){

    }

    public EvidencePo(long roundIndex, BlockHeader blockHeader1, BlockHeader blockHeader2){
        this.roundIndex = roundIndex;
        this.blockHeader1 = blockHeader1;
        this.blockHeader2 = blockHeader2;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public BlockHeader getBlockHeader1() {
        return blockHeader1;
    }

    public void setBlockHeader1(BlockHeader blockHeader1) {
        this.blockHeader1 = blockHeader1;
    }

    public BlockHeader getBlockHeader2() {
        return blockHeader2;
    }

    public void setBlockHeader2(BlockHeader blockHeader2) {
        this.blockHeader2 = blockHeader2;
    }
}
