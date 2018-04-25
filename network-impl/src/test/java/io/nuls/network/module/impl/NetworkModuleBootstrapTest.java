package io.nuls.network.module.impl; 

import io.nuls.core.module.BaseModuleBootstrap;
import io.nuls.network.module.base.BootstrapDBCacheTest;
import io.nuls.network.module.base.MicroKernelBootstrap1Test;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** 
 * NetworkModuleBootstrap Tester. 
 * 
 * @author Charlie
 * @since 04/24/2018
 */ 
public class NetworkModuleBootstrapTest { 

    private NetworkModuleBootstrap networkModuleBootstrap;
    @Before
    public void before() throws Exception {
        BootstrapDBCacheTest.start(MicroKernelBootstrap1Test.class);
        Class clazz = Class.forName("io.nuls.network.module.impl.NetworkModuleBootstrap");
        BaseModuleBootstrap module = (BaseModuleBootstrap) clazz.newInstance();
        module.setModuleName("network");
        networkModuleBootstrap = (NetworkModuleBootstrap)module;
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
        try {
           Method method = networkModuleBootstrap.getClass().getMethod("init");
           method.setAccessible(true);
           method.invoke(networkModuleBootstrap);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
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
           Method method = NetworkModuleBootstrap.getClass().getMethod("start");
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
           Method method = NetworkModuleBootstrap.getClass().getMethod("shutdown");
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
           Method method = NetworkModuleBootstrap.getClass().getMethod("destroy");
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
           Method method = NetworkModuleBootstrap.getClass().getMethod("getInfo");
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
     * Method: registerEvent() 
     * 
     */ 
    @Test
    public void testRegisterEvent() throws Exception { 
        /*
        try {
           Method method = NetworkModuleBootstrap.getClass().getMethod("registerEvent");
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        
    }
        
}
