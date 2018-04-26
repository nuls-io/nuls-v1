package io.nuls.db.module.impl; 

import io.nuls.core.MicroKernelBootstrap;
import io.nuls.core.module.manager.ModuleManager;
import io.nuls.core.module.manager.ServiceManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.spring.lite.core.ModularServiceMethodInterceptor;
import io.nuls.core.utils.spring.lite.core.SpringLiteContext;
import io.nuls.db.dao.impl.mybatis.BlockDaoImpl;
import io.nuls.protocol.context.NulsContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** 
 * MybatisDbModuleBootstrap Tester. 
 * 
 * @author Charlie
 * @since 04/22/2018
 */ 
public class MybatisDbModuleBootstrapTest {
   private MybatisDbModuleBootstrap db ;

    /**
     * 只启动核心模块
     * @throws Exception
     */
    private static void sysStart() throws Exception {
        MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
        mk.init();
        mk.start();
    }

    @Before
    public void before() throws Exception {
        SpringLiteContext.init("io.nuls", new ModularServiceMethodInterceptor());
        //sysStart();
        db = new MybatisDbModuleBootstrap();
    } 

    @After
    public void after() throws Exception { 
    
    } 
    

    /** 
     * 
     * Method: init() 
     * 
     */ 
    @Test
    public void testInit() throws Exception {
        //db.init();
    }


    /**
     *
     * Method: initSqlSessionFactory()
     *
     */
    @Test
    public void testInitSqlSessionFactory() throws Exception {
        try {
            Method method = this.db.getClass().getDeclaredMethod("initSqlSessionFactory");
            method.setAccessible(true);
            method.invoke(db);
            Assert.assertNotNull(db.getSqlSessionFactory());
            System.out.println("over");
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
    }

    /**
     *
     * Method: initService()
     *
     */
    @Test
    public void testInitService() throws Exception {

        try {
            Method method = db.getClass().getDeclaredMethod("initService");
            method.setAccessible(true);
            method.invoke(db);
            ServiceManager.getInstance().getModule(BlockDaoImpl.class);
            System.out.println("over");
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * 
     * Method: start() 
     * 
     */ 
    @Test
    public void testStart() throws Exception {
        /*
        try {
           Method method = MybatisDbModuleBootstrap.getClass().getMethod("start");
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        
    }
        
    /** 
     * 
     * Method: shutdown() 
     * 
     */ 
    @Test
    public void testShutdown() throws Exception { 
        /*
        try {
           Method method = MybatisDbModuleBootstrap.getClass().getMethod("shutdown");
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        
    }
        
    /** 
     * 
     * Method: destroy() 
     * 
     */ 
    @Test
    public void testDestroy() throws Exception { 
        /*
        try {
           Method method = MybatisDbModuleBootstrap.getClass().getMethod("destroy");
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        
    }
        
    /** 
     * 
     * Method: getInfo() 
     * 
     */ 
    @Test
    public void testGetInfo() throws Exception { 
        /*
        try {
           Method method = MybatisDbModuleBootstrap.getClass().getMethod("getInfo");
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        
    }
        
    /** 
     * 
     * Method: getSqlSessionFactory() 
     * 
     */ 
    @Test
    public void testGetSqlSessionFactory() throws Exception { 
        /*
        try {
           Method method = MybatisDbModuleBootstrap.getClass().getMethod("getSqlSessionFactory");
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        
    }
        
    /** 
     * 
     * Method: setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) 
     * 
     */ 
    @Test
    public void testSetSqlSessionFactory() throws Exception { 
        /*
        try {
           Method method = MybatisDbModuleBootstrap.getClass().getMethod("setSqlSessionFactory", SqlSessionFactory.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        
    }
        
        
    /** 
     * 
     * Method: initLevelDBStorage() 
     * 
     */ 
    @Test
    public void testInitLevelDBStorage() throws Exception { 
        /*
        try {
           Method method = MybatisDbModuleBootstrap.getClass().getMethod("initLevelDBStorage");
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        
    }
        
}
