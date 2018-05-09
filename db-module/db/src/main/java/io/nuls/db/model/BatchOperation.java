package io.nuls.db.model;

public interface BatchOperation {

    BatchOperation put(byte[] key, byte[] value);

    BatchOperation delete(byte[] key);

    BatchOperation executeBatch();
}
