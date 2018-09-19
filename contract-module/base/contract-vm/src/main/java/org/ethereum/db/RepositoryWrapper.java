/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.db;

import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.vm.DataWord;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Repository delegating all calls to the last Repository
 * <p>
 * Created by Anton Nashatyrev on 22.12.2016.
 */
public class RepositoryWrapper implements Repository {

    private Repository repository;

    public RepositoryWrapper(Repository repository) {
        this.repository = repository;
    }

    public Repository getRepository() {
        return repository;
    }

    @Override
    public AccountState createAccount(byte[] addr, byte[] creater) {
        return getRepository().createAccount(addr, creater);
    }

    @Override
    public boolean isExist(byte[] addr) {
        return getRepository().isExist(addr);
    }

    @Override
    public AccountState getAccountState(byte[] addr) {
        return getRepository().getAccountState(addr);
    }

    @Override
    public void delete(byte[] addr) {
        getRepository().delete(addr);
    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {
        return getRepository().increaseNonce(addr);
    }

    @Override
    public BigInteger setNonce(byte[] addr, BigInteger nonce) {
        return getRepository().setNonce(addr, nonce);
    }

    @Override
    public BigInteger getNonce(byte[] addr) {
        return getRepository().getNonce(addr);
    }

    @Override
    public ContractDetails getContractDetails(byte[] addr) {
        return getRepository().getContractDetails(addr);
    }

    @Override
    public boolean hasContractDetails(byte[] addr) {
        return getRepository().hasContractDetails(addr);
    }

    @Override
    public void saveCode(byte[] addr, byte[] code) {
        getRepository().saveCode(addr, code);
    }

    @Override
    public byte[] getCode(byte[] addr) {
        return getRepository().getCode(addr);
    }

    @Override
    public byte[] getCodeHash(byte[] addr) {
        return getRepository().getCodeHash(addr);
    }

    @Override
    public void addStorageRow(byte[] addr, DataWord key, DataWord value) {
        getRepository().addStorageRow(addr, key, value);
    }

    @Override
    public DataWord getStorageValue(byte[] addr, DataWord key) {
        return getRepository().getStorageValue(addr, key);
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        return getRepository().getBalance(addr);
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        return getRepository().addBalance(addr, value);
    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        return getRepository().getAccountsKeys();
    }

    @Override
    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
        getRepository().dumpState(block, gasUsed, txNumber, txHash);
    }

    @Override
    public Repository startTracking() {
        return getRepository().startTracking();
    }

    @Override
    public void flush() {
        getRepository().flush();
    }

    @Override
    public void flushNoReconnect() {
        getRepository().flushNoReconnect();
    }

    @Override
    public void commit() {
        getRepository().commit();
    }

    @Override
    public void rollback() {
        getRepository().rollback();
    }

    @Override
    public void syncToRoot(byte[] root) {
        getRepository().syncToRoot(root);
    }

    @Override
    public boolean isClosed() {
        return getRepository().isClosed();
    }

    @Override
    public void close() {
        getRepository().close();
    }

    @Override
    public void reset() {
        getRepository().reset();
    }

    @Override
    public void updateBatch(HashMap<ByteArrayWrapper, AccountState> accountStates, HashMap<ByteArrayWrapper, ContractDetails> contractDetailes) {
        getRepository().updateBatch(accountStates, contractDetailes);
    }

    @Override
    public byte[] getRoot() {
        return getRepository().getRoot();
    }

    @Override
    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, AccountState> cacheAccounts, HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {
        getRepository().loadAccount(addr, cacheAccounts, cacheDetails);
    }

    @Override
    public Repository getSnapshotTo(byte[] root) {
        return getRepository().getSnapshotTo(root);
    }

    @Override
    public Repository clone() {
        return getSnapshotTo(getRoot());
    }

    @Override
    public int getStorageSize(byte[] addr) {
        return getRepository().getStorageSize(addr);
    }

    @Override
    public Set<DataWord> getStorageKeys(byte[] addr) {
        return getRepository().getStorageKeys(addr);
    }

    @Override
    public Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys) {
        return getRepository().getStorage(addr, keys);
    }
}
