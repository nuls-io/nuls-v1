package io.nuls.test;

import io.nuls.core.manager.ModuleManager;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.impl.mybatis.BlockDaoImpl;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.module.impl.MybatisDBModuleImpl;
import org.junit.*;

/**
 * Created by zhouwei on 2017/10/24.
 */
public class DBModuleTest {

    private static MybatisDBModuleImpl dbModule;

    private static BlockDao blockDao;

    @BeforeClass
    public static void init() {
        dbModule = new MybatisDBModuleImpl();
        dbModule.start();
        blockDao = ModuleManager.getInstance().getService(BlockDao.class);
    }

    @AfterClass
    public static void end() {
        dbModule.destroy();
    }

    @Test
    public void testCallback(){
        BlockPo blockPo = blockDao.getByKey("aaa");
        Log.debug("============="+blockPo.getHeight());
        blockPo.setHeight(blockPo.getHeight()+2);

    }

    @Test
    public void testSaveBlock() {
        BlockPo blockPo = new BlockPo();
        blockPo.setCreateTime(111L);
        blockPo.setHash("aaa");
        blockPo.setHeight(111L);
//        blockPo.setScript("dsfasdf".getBytes());
        int result = blockDao.save(blockPo);
        Log.debug("result" + result);

    }

    @Test
    public void testSelect() {
        BlockPo blockPo = blockDao.getByKey("bbb");
        Log.debug(blockPo.getCreateTime()+"");
    }


    public static void main(String[] args) {
        MybatisDBModuleImpl dbModule = new MybatisDBModuleImpl();
        dbModule.start();
        BlockDao blockDao = ModuleManager.getInstance().getService(BlockDaoImpl.class);

        for(int i = 0 ; i < 10 ;i++) {
            new Thread(){
                public void run(){
                    try {
                        BlockPo blockPo = blockDao.getByKey("aaa");
                        Log.debug(blockPo.getCreateTime()+"");
                    }catch (Exception e) {
                        Log.error(e);
                    }
                }
            }.start();
        }
    }

    @Test
    public void testCount() {
        Long count = blockDao.getCount();
        Log.debug("count" + count);
    }

    @Test
    public void testBB() {
        Log.debug(0xFFFFL+"");
    }
}
