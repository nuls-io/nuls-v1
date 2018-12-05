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

package io.nuls.protocol.utils;

import io.nuls.kernel.model.NulsDigestData;

/**
 * 用于接收交易去重
 *
 * @author: Niels Wang
 * @date: 2018/7/8
 */
public class SmallBlockDuplicateRemoval {

    private static HashSetDuplicateProcessor processorOfSmallBlock = new HashSetDuplicateProcessor(1000);
    private static HashSetDuplicateProcessor processorOfForward = new HashSetDuplicateProcessor(1000);

    public static boolean needDownloadSmallBlock(NulsDigestData hash) {
        return processorOfForward.check(hash);
    }

    public static boolean needProcess(NulsDigestData hash) {
        processorOfForward.insertAndCheck(hash);
        return processorOfSmallBlock.insertAndCheck(hash);
    }

    public static void removeForward(NulsDigestData hash) {
        processorOfForward.remove(hash);
    }
}
