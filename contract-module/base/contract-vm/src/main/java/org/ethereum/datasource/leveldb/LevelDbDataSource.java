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
package org.ethereum.datasource.leveldb;

import org.ethereum.datasource.DbSettings;
import org.ethereum.datasource.DbSource;

import java.util.Map;
import java.util.Set;

/**
 * @author Roman Mandeleil
 * @since 18.01.2015
 */
public class LevelDbDataSource implements DbSource<byte[]> {

    String name;
    boolean alive;

    public LevelDbDataSource() {
    }

    public LevelDbDataSource(String name) {
        this.name = name;
    }

    @Override
    public void init() {
    }

    @Override
    public void init(DbSettings settings) {
    }

    @Override
    public void reset() {
    }

    @Override
    public byte[] prefixLookup(byte[] key, int prefixBytes) {
        return null;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] get(byte[] key) {
        return null;
    }

    @Override
    public void put(byte[] key, byte[] value) {
    }

    @Override
    public void delete(byte[] key) {
    }

    @Override
    public Set<byte[]> keys() {
        return null;
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
    }

    @Override
    public boolean flush() {
        return false;
    }

    @Override
    public void close() {
    }

}
