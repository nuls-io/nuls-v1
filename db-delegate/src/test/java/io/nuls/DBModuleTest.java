package io.nuls;

import com.alibaba.druid.pool.DruidDataSource;
import io.nuls.db.DBModule;
import io.nuls.db.DBModuleImpl;
import io.nuls.db.dao.mybatis.BlockMapper;
import io.nuls.db.dao.mybatis.base.CommonMapper;
import io.nuls.db.dao.mybatis.util.Condition;
import io.nuls.db.dao.mybatis.util.SearchOperator;
import io.nuls.db.dao.mybatis.util.Searchable;
import io.nuls.db.entity.Block;
import io.nuls.db.impl.BlockStoreImpl;
import io.nuls.db.intf.IBlockStore;
import io.nuls.global.NulsContext;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Unit test for simple DBModule.
 */
public class DBModuleTest {

    private DBModule dbModule;

    private SqlSessionFactory sqlSessionFactory;
    /**
     * start spring
     */
    @Before
    public void init() {

    }

    @org.junit.Test
    public void testMybatis() {
        try {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis/mybatis-config.xml"),"druid");
            SqlSession session = sqlSessionFactory.openSession();
            DataSource ds = session.getConfiguration().getEnvironment().getDataSource();
            if(ds instanceof DruidDataSource){
                System.out.println("Yes");
            }else{
                System.out.println("No");
            }

            CommonMapper commonMapper = session.getMapper(CommonMapper.class);

            BlockMapper blockMapper = session.getMapper(BlockMapper.class);
//            Long block = session.selectOne("io.nuls.db.dao.mybatis.BlockMapper.count");
            Searchable searchable = new Searchable();
            searchable.addCondition(new Condition("hash", SearchOperator.eq, "shshh"));
            List<Block> blockList = blockMapper.selectList(searchable);
            for(Block block : blockList) {
                System.out.println(block.getHash());
            }
            long num = blockMapper.count(new Searchable());
            System.out.println("------------" + num);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    @org.junit.Test
    public void testDB() {

        IBlockStore blockStore = new BlockStoreImpl();

        long count = blockStore.count();
        System.out.println(count);
//        long start = System.currentTimeMillis();
//
//        for (long i = 0; i < 100000; i++) {
//            Block b = new Block();
//            b.setHash("blockkey" + i);
//            b.setHeight(i);
//            b.setCreatetime(System.currentTimeMillis());
//            try{
//                blockStore.save(b);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//
//
//        long end = System.currentTimeMillis();
//
//        System.out.println("-----------------use:" + (start - end));


//        try {
//            for (int j = 0; j < 20; j++) {
//                new Thread(
//                        new Runnable() {
//                            @Override
//                            public void run() {
//
//                                long start = System.currentTimeMillis();
//
//                                for (long i = 0; i < 100000; i++) {
//                                    Block b = new Block();
//                                    b.setHash("blockkey" + i);
//                                    b.setHeight(i);
//                                    b.setCreatetime(System.currentTimeMillis());
//                                    try{
//                                        blockStore.save(b);
//                                    }catch(Exception e){
//                                        e.printStackTrace();
//                                    }
//                                }
//
//
//                                long end = System.currentTimeMillis();
//
//                                System.out.println("-----------------use:" + (start - end));
//                            }
//                        }
//                ).start();
//            }
//            Thread.sleep(30000l);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
