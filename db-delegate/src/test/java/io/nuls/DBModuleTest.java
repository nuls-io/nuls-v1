package io.nuls;

import com.alibaba.druid.pool.DruidDataSource;
import io.nuls.db.DBModule;
import io.nuls.db.DBModuleImpl;
import io.nuls.db.entity.Block;
import io.nuls.db.impl.BlockStoreImpl;
import io.nuls.db.intf.IBlockStore;
import io.nuls.global.NulsContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


/**
 * Unit test for simple DBModule.
 */
public class DBModuleTest {

    private DBModule dbModule;
    /**
     * start spring
     */
    @Before
    public void init() {
//        dbModule = new DBModuleImpl();
//        Map<String,String> map = new HashMap<>();
//        map.put("dataBaseType", "h2");
//        dbModule.init(map);
    }

    @org.junit.Test
    public void testMybatis() {
        SqlSessionFactory sqlSessionFactory;
        try {

            sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis/mybatis-config.xml"),"h2");
            SqlSession session = sqlSessionFactory.openSession();
            DataSource ds = session.getConfiguration().getEnvironment().getDataSource();
            if(ds instanceof DruidDataSource){
                System.out.println("Yes");
            }else{
                System.out.println("No");
            }
            Long block = session.selectOne("io.nuls.db.dao.mybatis.BlockMapper.count");
            System.out.println("------------" + block);
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
