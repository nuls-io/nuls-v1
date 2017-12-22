package io.nuls.test;

import io.nuls.core.chain.entity.Result;
import io.nuls.core.module.manager.ServiceManager;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.AccountDao;
import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.PeerDao;
import io.nuls.db.dao.impl.mybatis.BlockDaoImpl;
import io.nuls.db.entity.AccountPo;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.PeerPo;
import io.nuls.db.module.impl.MybatisDBModuleImpl;
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

    private static MybatisDBModuleImpl dbModule;

    private static BlockDao blockDao;

    private static PeerDao peerDao;

    private static AccountDao accountDao;

    @BeforeClass
    public static void init() {
        dbModule = new MybatisDBModuleImpl();
        dbModule.start();
        blockDao = ServiceManager.getInstance().getService(BlockDao.class);
        peerDao = ServiceManager.getInstance().getService(PeerDao.class);
        accountDao = ServiceManager.getInstance().getService(AccountDao.class);
    }

    @AfterClass
    public static void end() {
        dbModule.destroy();
    }

    @Test
    public void testCallback() {
        BlockPo blockPo = blockDao.getByKey("aaa");
        Log.debug("=============" + blockPo.getHeight());
        blockPo.setHeight(blockPo.getHeight() + 2);

    }

    @Test
    public void testSaveBlock() {
        BlockPo blockPo = new BlockPo();
        blockPo.setCreateTime(111L);
        blockPo.setHash("aaa");
        blockPo.setHeight(111L);
        blockPo.setMerkleHash("aaab");
        blockPo.setPreHash("xxx");
        blockPo.setTxcount(10L);
        blockPo.setBytes(new byte[10]);
        blockPo.setVarsion(1);
        blockPo.setSign(new byte[2]);
//        blockPo.setScript("dsfasdf".getBytes());
        int result = blockDao.save(blockPo);
        Log.debug("result" + result);
    }

    @Test
    public void testSelect() {
        BlockPo blockPo = blockDao.getByKey("bbb");
        Log.debug(blockPo.getCreateTime() + "");
    }


    public static void main(String[] args) {
        MybatisDBModuleImpl dbModule = new MybatisDBModuleImpl();
        dbModule.start();
        BlockDao blockDao = ServiceManager.getInstance().getService(BlockDaoImpl.class);

        for (int i = 0; i < 10; i++) {
            new Thread() {
                public void run() {
                    try {
                        BlockPo blockPo = blockDao.getByKey("aaa");
                        Log.debug(blockPo.getCreateTime() + "");
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
    public void testPeer() {
        Set<String> keys = new HashSet<>();
        keys.add("12");
        keys.add("1442");
        List<PeerPo> list = peerDao.getRandomPeerPoList(10, keys);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).getIp());
        }
    }

    @Test
    public void testInsertPeer() {
        List<PeerPo> list = new ArrayList<>();
        for (int i = 40; i < 50; i++) {
            PeerPo peerPo = new PeerPo();
            peerPo.setIp("192.168.1." + i);
            peerPo.setPort(i + 1000);
            peerPo.setId(peerPo.getIp() + ":" + peerPo.getPort());
            peerPo.setLastTime(System.currentTimeMillis());
            peerPo.setLastFailTime(System.currentTimeMillis() - 4123 * 999);
            peerPo.setMagicNum(1234);
            peerPo.setVersion((short) 5097);
            peerPo.setFailCount(0);
            list.add(peerPo);
        }
        peerDao.saveBatch(list);
    }

    @Test
    public void testDeletePeer() {
        PeerPo po = new PeerPo();
        po.setIp("192.168.1.166");
        po.setPort(1234);
        po.setMagicNum(123456789);
        po.setVersion((short) 1001);
        po.setFailCount(10);
        peerDao.saveChange(po);

    }

    @Test
    public void testAccount() {
        AccountPo accountPo = new AccountPo();
        accountPo.setId("abcd");
        accountPo.setAddress("abcd");
        accountPo.setCreateTime(System.currentTimeMillis());
        accountPo.setVersion((short) 11);

        accountDao.save(accountPo);
    }

    @Test
    public void testAlias() {
        String alias = "zoro";
        String id = "abcd";
    }
}
