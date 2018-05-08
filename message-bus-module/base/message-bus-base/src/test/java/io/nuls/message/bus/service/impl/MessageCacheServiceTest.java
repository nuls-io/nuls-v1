package io.nuls.message.bus.service.impl;

import io.nuls.cache.CacheMap;
import io.nuls.protocol.message.BlockMessage;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class MessageCacheServiceTest {

    private MessageCacheService messageCacheService = MessageCacheService.getInstance();

    private BlockMessage blockMessage =  null;

    @Before
    public void before() {
        blockMessage = new BlockMessage();
    }

    /**
     * 缓存发送的消息
     * Caching messages sent
     */
    @Test
    public void cacheSendedMessage() {
        messageCacheService.cacheSendedMessage(blockMessage);
        assertNotNull(messageCacheService.getSendMessage(blockMessage.getHash().getDigestHex()));
    }

    /**
     * 将缓存的消息移除
     * Removes the cached message.
     *
     */
    @Test
    public void removeSendedMessage() {
        if (null == messageCacheService.getSendMessage(blockMessage.getHash().getDigestHex())){
            messageCacheService.cacheSendedMessage(blockMessage);
        }
        assertNotNull(messageCacheService.getSendMessage(blockMessage.getHash().getDigestHex()));
        messageCacheService.removeSendedMessage(blockMessage);
        assertNull(messageCacheService.getSendMessage(blockMessage.getHash().getDigestHex()));
    }

    /**
     * 缓存接收的消息的散列
     * Caching the Hex hash of received messages
     */
    @Test
    public void cacheRecievedMessageHash() throws Exception {
        messageCacheService.cacheRecievedMessageHash(blockMessage.getHash().getDigestHex());
        assertNotNull(messageCacheService);

        Field cacheMapRecievedField = messageCacheService.getClass().getDeclaredField("cacheMapRecieved");
        cacheMapRecievedField.setAccessible(true);
        CacheMap<String, Integer>  cacheMap = (CacheMap<String, Integer>)cacheMapRecievedField.get(messageCacheService);
        assertTrue(cacheMap.get(blockMessage.getHash().getDigestHex()) == 1);
    }

    /**
     * 是否缓存有对应的消息
     * Check if the cache has a corresponding message.
     */
    @Test
    public void kownTheMessage() {
        if (null == messageCacheService.getSendMessage(blockMessage.getHash().getDigestHex())){
            messageCacheService.cacheSendedMessage(blockMessage);
            assertTrue(messageCacheService.kownTheMessage(blockMessage.getHash().getDigestHex()));
        }else{
            assertTrue(messageCacheService.kownTheMessage(blockMessage.getHash().getDigestHex()));
        }
        messageCacheService.cacheRecievedMessageHash(blockMessage.getHash().getDigestHex());
        assertTrue(messageCacheService.kownTheMessage(blockMessage.getHash().getDigestHex()));
    }

    /**
     * 验证获取消息
     * 1.先验证初始获取消息为空时,则发送一个消息
     * 2.验证获取对应的消息不为空
     * Verify get the sent message in the cache according to the hex hash.
     */
    @Test
    public void getSendMessage() {
        if (null == messageCacheService.getSendMessage(blockMessage.getHash().getDigestHex())){
            messageCacheService.cacheSendedMessage(blockMessage);
        }
        assertNotNull(messageCacheService.getSendMessage(blockMessage.getHash().getDigestHex()));
    }

    /**
     * 验证销毁缓存的方法
     * 需要分别验证[cacheMapRecieved集合]和[cacheMapSended集合]
     * 在销毁前缓存数据不为0(如果为0则先添加缓存数据), 销毁后缓存数为0
     *
     * Test case destroying the cache
     * Verify cacheMapRecieved collection and cacheMapSended collection
     * cache data before destroy is not 0(add cached data for 0 first),
     * and the cache number is 0 after destroy.
     * @throws Exception
     */
    @Test
    public void destroy() throws Exception {
        //缓存消息,验证cacheMapRecieved集合是否有值
        messageCacheService.cacheRecievedMessageHash(blockMessage.getHash().getDigestHex());
        Field cacheMapRecievedField = messageCacheService.getClass().getDeclaredField("cacheMapRecieved");
        cacheMapRecievedField.setAccessible(true);
        CacheMap<String, Integer>  cacheMap = (CacheMap<String, Integer>)cacheMapRecievedField.get(messageCacheService);
        assertTrue(cacheMap.size() > 0);

        //缓存消息,验证cacheMapSended集合是否有值
        messageCacheService.cacheSendedMessage(blockMessage);
        Field cacheMapSendedField = messageCacheService.getClass().getDeclaredField("cacheMapSended");
        cacheMapSendedField.setAccessible(true);
        CacheMap<String, Integer>  cacheMapSended = (CacheMap<String, Integer>)cacheMapSendedField.get(messageCacheService);
        assertTrue(cacheMapSended.size() > 0);

        //销毁
        messageCacheService.destroy();

        //销毁后, 验证cacheMapRecieved集合是否被销毁
        cacheMap = (CacheMap<String, Integer>)cacheMapRecievedField.get(messageCacheService);
        assertTrue(cacheMap.size() == 0);

        cacheMapSended = (CacheMap<String, Integer>)cacheMapRecievedField.get(messageCacheService);
        assertTrue(cacheMapSended.size() == 0);
    }
}