package io.nuls.contract.vm.program.impl;

import io.nuls.db.service.DBService;
import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.datasource.Source;

public class KeyValueSource implements Source<byte[], byte[]> {

    public static final String AREA = "contract";

    private DBService dbService;

    public KeyValueSource(DBService dbService) {
        this.dbService = dbService;
        String[] areas = dbService.listArea();
        if (!ArrayUtils.contains(areas, AREA)) {
            dbService.createArea(AREA);
        }
    }

    @Override
    public void put(byte[] key, byte[] val) {
        dbService.put(AREA, key, val);
    }

    @Override
    public byte[] get(byte[] key) {
        return dbService.get(AREA, key);
    }

    @Override
    public void delete(byte[] key) {
    }

    @Override
    public boolean flush() {
        return true;
    }

}
