package io.nuls.contract.vm.program.impl;

import io.nuls.core.tools.crypto.Hex;
import io.nuls.db.service.DBService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.datasource.Source;

import java.io.File;
import java.io.IOException;

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
//        File file = new File("/tmp/" + Hex.encode(key));
//        try {
//            FileUtils.writeByteArrayToFile(file, val);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        dbService.put(AREA, key, val);
    }

    @Override
    public byte[] get(byte[] key) {
//        File file = new File("/tmp/" + Hex.encode(key));
//        try {
//            return FileUtils.readFileToByteArray(file);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return dbService.get(AREA, key);
    }

    @Override
    public void delete(byte[] key) {
//        File file = new File("/tmp/" + Hex.encode(key));
//        FileUtils.deleteQuietly(file);
        //dbService.delete(AREA, key);
    }

    @Override
    public boolean flush() {
        return true;
    }

}
