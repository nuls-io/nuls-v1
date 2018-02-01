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
package io.nuls.test;

import io.nuls.core.module.manager.ServiceManager;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.*;
import io.nuls.db.dao.impl.mybatis.BlockDaoImpl;
import io.nuls.db.entity.*;
import io.nuls.db.module.impl.MybatisDBModuleBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhouwei on 2017/10/24.
 */
public class DBModuleTest {

    private static MybatisDBModuleBootstrap dbModule;

    private static BlockHeaderService blockDao;

    private static NodeDataService nodeDao;

    private static AccountDataService accountDao;

    private static TransactionDataService txService;

    private static TransactionLocalDataService txLocalService;

    private static UtxoInputDataService inputService;

    private static UtxoOutputDataService outputService;

    private static TxAccountRelationDataService relationDataService;

    @BeforeClass
    public static void init() {
        dbModule = new MybatisDBModuleBootstrap();
        dbModule.start();
        blockDao = ServiceManager.getInstance().getService(BlockHeaderService.class);
        nodeDao = ServiceManager.getInstance().getService(NodeDataService.class);
        accountDao = ServiceManager.getInstance().getService(AccountDataService.class);
        txService = ServiceManager.getInstance().getService(TransactionDataService.class);
        txLocalService = ServiceManager.getInstance().getService(TransactionLocalDataService.class);
        inputService = ServiceManager.getInstance().getService(UtxoInputDataService.class);
        outputService = ServiceManager.getInstance().getService(UtxoOutputDataService.class);
        relationDataService = ServiceManager.getInstance().getService(TxAccountRelationDataService.class);
    }

    @AfterClass
    public static void end() {
        dbModule.destroy();
    }

    @Test
    public void testCallback() {
        BlockHeaderPo blockHeaderPo = blockDao.get("aaa");
        Log.debug("=============" + blockHeaderPo.getHeight());
        blockHeaderPo.setHeight(blockHeaderPo.getHeight() + 2);

    }

    @Test
    public void testSaveBlock() {
        BlockHeaderPo blockHeaderPo = new BlockHeaderPo();
        blockHeaderPo.setCreateTime(111L);
        blockHeaderPo.setHash("aaa");
        blockHeaderPo.setHeight(111L);
        blockHeaderPo.setMerkleHash("aaab");
        blockHeaderPo.setPreHash("xxx");
        blockHeaderPo.setTxCount(10L);
//        blockHeaderPo.setBytes(new byte[10]);
        blockHeaderPo.setVersion(1);
        blockHeaderPo.setSign(new byte[2]);
//        blockPo.setScript("dsfasdf".getBytes());
        int result = blockDao.save(blockHeaderPo);
        Log.debug("result" + result);
    }

    @Test
    public void testSelect() {
        BlockHeaderPo blockHeaderPo = blockDao.get("bbb");
        Log.debug(blockHeaderPo.getCreateTime() + "");
    }


    public static void main(String[] args) {
        MybatisDBModuleBootstrap dbModule = new MybatisDBModuleBootstrap();
        dbModule.start();
        BlockHeaderService blockDao = ServiceManager.getInstance().getService(BlockDaoImpl.class);

        for (int i = 0; i < 10; i++) {
            new Thread() {
                public void run() {
                    try {
                        BlockHeaderPo blockHeaderPo = blockDao.get("aaa");
                        Log.debug(blockHeaderPo.getCreateTime() + "");
                    } catch (Exception e) {
                        Log.error(e);
                    }
                }
            }.start();
        }
    }

    @Test
    public void testCount() {
        Long count = blockDao.getCount();
        Log.info("count===" + count);
    }

    @Test
    public void testBB() {
        Log.debug(0xFFFFL + "");
    }

    @Test
    public void testNode() {
        Set<String> keys = new HashSet<>();
        keys.add("12");
        keys.add("1442");
        List<NodePo> list = nodeDao.getRandomNodePoList(10, keys);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).getIp());
        }
    }

    @Test
    public void testInsertNode() {
        List<NodePo> list = new ArrayList<>();
        for (int i = 40; i < 50; i++) {
            NodePo nodePo = new NodePo();
            nodePo.setIp("192.168.1." + i);
            nodePo.setPort(i + 1000);
            nodePo.setId(nodePo.getIp() + ":" + nodePo.getPort());
            nodePo.setLastTime(System.currentTimeMillis());
            nodePo.setLastFailTime(System.currentTimeMillis() - 4123 * 999);
            nodePo.setMagicNum(1234);
            nodePo.setVersion((short) 5097);
            nodePo.setFailCount(0);
            list.add(nodePo);
        }
        nodeDao.save(list);
    }

    @Test
    public void testDeleteNode() {
        NodePo po = new NodePo();
        po.setIp("192.168.1.166");
        po.setPort(1234);
        po.setMagicNum(123456789);
        po.setVersion((short) 1001);
        po.setFailCount(10);
        nodeDao.saveChange(po);

    }

    @Test
    public void testAccount() {
        AccountPo accountPo = new AccountPo();
        accountPo.setAddress("abcd");
        accountPo.setCreateTime(System.currentTimeMillis());
        accountPo.setVersion((short) 11);

        accountDao.save(accountPo);
    }

    @Test
    public void testInsertTransaction() {
        TransactionPo tx = new TransactionPo();
        tx.setHash("aa123");
        tx.setType(1);
        tx.setTxIndex(1);
        tx.setBlockHeight(1L);
        tx.setCreateTime(System.currentTimeMillis());
        tx.setFee(10L);
        tx.setRemark("test insert");
        tx.setTxData(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        tx.setSign(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});

        txService.save(tx);
    }

    @Test
    public void testInsertTransactionLocal() {
        TransactionLocalPo tx = new TransactionLocalPo();
        tx.setHash("aa123");
        tx.setType(1);
        tx.setTxIndex(1);
        tx.setBlockHeight(1L);
        tx.setCreateTime(System.currentTimeMillis());
        tx.setFee(10L);
        tx.setRemark("test insert");
        tx.setTxData(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        tx.setSign(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});

        txLocalService.save(tx);
    }

    @Test
    public void testGetTx() {
        String hash = "aa123";
//        TransactionPo tx = txService.get(hash);
//
//        List<TransactionPo> list = txService.getTxs("ABCDEFJG",1);
//        TransactionLocalPo po = txLocalService.get(hash);
        List<TransactionLocalPo> list = txLocalService.getTxs("ABCDEFJG",1);
    }

    @Test
    public void testInsertInput() {
        UtxoInputPo inputPo = new UtxoInputPo();
        inputPo.setTxHash("aa123");
        inputPo.setInIndex(5);
        inputPo.setFromIndex(0);
        inputPo.setSign(new byte[]{16, 2, 3, 4, 5, 6, 7, 8});
        inputService.save(inputPo);
    }

    @Test
    public void testInsertOutput() {
        UtxoOutputPo outputPo = new UtxoOutputPo();
        outputPo.setTxHash("aa123");
        outputPo.setOutIndex(0);
        outputPo.setStatus((byte) 3);
        outputPo.setValue(1000000L);
        outputPo.setLockTime(0L);
        outputPo.setAddress("ABCDEFJG");
        outputPo.setScript(new byte[]{1, 2, 3, 4, 3, 76, 7, 7, 3});

        outputService.save(outputPo);

    }

    @Test
    public void testInsertRelation() {
        TxAccountRelationPo relationPo = new TxAccountRelationPo();
        relationPo.setAddress("ABCDEFJG");
        relationPo.setTxHash("aa123");
        relationDataService.save(relationPo);
    }
}
