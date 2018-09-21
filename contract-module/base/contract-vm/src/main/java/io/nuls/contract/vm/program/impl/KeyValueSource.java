package io.nuls.contract.vm.program.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.nuls.db.service.DBService;
import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.datasource.Source;
import org.ethereum.db.ByteArrayWrapper;

import java.util.concurrent.TimeUnit;

public class KeyValueSource implements Source<byte[], byte[]> {

    public static final String AREA = "contract";

    private DBService dbService;

    private final Cache<ByteArrayWrapper, byte[]> cache;

    public KeyValueSource(DBService dbService) {
        this.dbService = dbService;
        String[] areas = dbService.listArea();
        if (!ArrayUtils.contains(areas, AREA)) {
            dbService.createArea(AREA);
        }
        this.cache = CacheBuilder.newBuilder()
                .initialCapacity(10240)
                .maximumSize(102400)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void put(byte[] key, byte[] val) {
        cache.put(new ByteArrayWrapper(key), val);
        dbService.put(AREA, key, val);
    }

    @Override
    public byte[] get(byte[] key) {
        byte[] bytes = cache.getIfPresent(new ByteArrayWrapper(key));
        if (bytes == null) {
            bytes = dbService.get(AREA, key);
        }
        return bytes;
    }

    @Override
    public void delete(byte[] key) {
    }

    @Override
    public boolean flush() {
        return true;
    }

}
