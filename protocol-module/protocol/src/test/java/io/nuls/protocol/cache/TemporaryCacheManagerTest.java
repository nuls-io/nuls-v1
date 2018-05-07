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

package io.nuls.protocol.cache;

import io.nuls.cache.manager.EhCacheManager;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDataType;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.model.SmallBlock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 临时缓存管理器测试类，测试临时缓存的存、取、删、清理、销毁功能
 * <p>
 * Temporary cache manager test class, test temporary cache storage, fetch, delete, clean, destroy function.
 *
 * @author: Niels Wang
 * @date: 2018/5/7
 */
public class TemporaryCacheManagerTest {

    private TemporaryCacheManager manager;

    /**
     * 获取TemporaryCacheManager对象，TemporaryCacheManager是用单例模式实现的，只需要调用getInstance方法就能获取虚拟机内唯一的实例
     * <p>
     * To obtain the TemporaryCacheManager object, TemporaryCacheManager is implemented in singleton mode,
     * and the only instance in the virtual machine can be obtained by calling the getInstance method.
     */
    @Before
    public void init() {
        manager = TemporaryCacheManager.getInstance();
    }

    /**
     * 测试小区快的缓存处理流程：
     * 1. 先测试放入缓存，不出现异常视为成功
     * 2. 测试获取刚放入的小区快，获取的结果不能为空且和刚仿佛的是相同内容
     * 3. 测试删除之前放入的小区快，删除后再获取应该得到null
     * 4. 重新把小区快放回缓存中，调用清理方法，清理后缓存中应该没有任何小区块或者交易
     * test the cache processing process of the smallblock:
     * 1. The first test is put into the cache, and no exceptions are deemed to be successful.
     * 2. Test to get the newly inserted smallblock fast, the results can not be empty and the same content as if it is just as if.
     * 3. Before the test is deleted, the cells should be put into the smallblock quickly. After deleting, it should be null.
     * 4. Reattach the smallblock to the cache, call the cleaning method, and there should be no blocks or transactions in the cache.
     */
    @Test
    public void cacheSmallBlock() {
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setDataType(NulsDataType.TRANSACTION);
        BlockHeader header = new BlockHeader();
        NulsDigestData hash = NulsDigestData.calcDigestData("abcdefg".getBytes());
        header.setHash(hash);
        smallBlock.setHeader(header);
        manager.cacheSmallBlock(smallBlock);
        assertTrue(true);

        this.getSmallBlock(hash, smallBlock);

        this.removeSmallBlock(hash);

        manager.cacheSmallBlock(smallBlock);

        this.clear();
    }

    /**
     * 测试获取已存在缓存中的小区块
     * <p>
     * The test gets the small block in the cache.
     *
     * @param hash       已存入的小区快的摘要对象，A quick summary of the community that has been deposited.
     * @param smallBlock 已存入缓存的小区快，The cached community is fast.
     */
    private void getSmallBlock(NulsDigestData hash, SmallBlock smallBlock) {
        SmallBlock sb = manager.getSmallBlock(NulsDigestData.calcDigestData("abcdefg".getBytes()));
        assertEquals(sb.getDataType(), smallBlock.getDataType());
    }

    /**
     * 测试交易的缓存处理流程：
     * 1. 先测试放入缓存，不出现异常视为成功
     * 2. 测试获取刚放入的交易，获取的结果不能为空且和刚仿佛的是相同内容
     * 3. 测试删除之前放入的交易，删除后再获取应该得到null
     * 4. 重新把交易放回缓存中，调用清理方法，清理后缓存中应该没有任何小区块或者交易
     * the cache processing flow of test transaction:
     * 1. The first test is put into the cache, and no exceptions are deemed to be successful.
     * 2. Test to obtain the newly placed transaction, the results obtained cannot be empty and the same content as the new one.
     * 3. Delete the transaction that was put in before the test is deleted, and then get null after deleting.
     * 4. Return the transaction to the cache, call the cleaning method, and there should be no blocks or transactions in the cache.
     */
    @Test
    public void cacheTx() {
        Transaction tx = new CacheTestTx();
        tx.setTime(1234567654L);
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        manager.cacheTx(tx);
        assertTrue(true);

        getTx(tx.getHash(), tx);
    }

    /**
     * 测试获取已存在缓存中的交易
     * <p>
     * The test gets the transaction in the cache.
     *
     * @param hash 已存入的交易的摘要对象，A quick summary of the transaction that has been deposited.
     * @param tx   已存入缓存的交易，The cached Transaction is fast.
     */
    private void getTx(NulsDigestData hash, Transaction tx) {
        Transaction txGoted = manager.getTx(hash);
        assertNotNull(tx);
        assertEquals(tx.getTime(), txGoted.getTime());
    }


    /**
     * 根据区块摘要对象从缓存中移除对应的小区块，判断移除是否成功
     * Remove the corresponding block from the cache according to the block summary object, and determine whether the removal is successful.
     */
    public void removeSmallBlock(NulsDigestData hash) {
        SmallBlock smallBlock = manager.getSmallBlock(hash);
        assertNotNull(smallBlock);

        manager.removeSmallBlock(hash);
        smallBlock = manager.getSmallBlock(hash);
        assertNull(smallBlock);
    }


    /**
     * 清理缓存，验证结果
     * clear the cache and verify the results
     */
    public void clear() {
        manager.clear();
        assertEquals(manager.getSmallBlockCount(), 0);
        assertEquals(manager.getTxCount(), 0);
    }

    /**
     * 销毁缓存，验证结果
     * destroy the cache and verify the results
     */
    @Test
    public void destroy() {
        manager.destroy();
        assertNull(EhCacheManager.getInstance().getCache("temp-small-block-cache"));
        assertNull(EhCacheManager.getInstance().getCache("temp-tx-cache"));
    }
}