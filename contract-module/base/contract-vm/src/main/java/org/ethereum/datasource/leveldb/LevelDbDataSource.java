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

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.DbSettings;
import org.ethereum.datasource.DbSource;
import org.ethereum.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.ethereum.util.ByteUtil.toHexString;

/**
 * @author Roman Mandeleil
 * @since 18.01.2015
 */
public class LevelDbDataSource implements DbSource<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger("db");

    SystemProperties config = SystemProperties.getDefault(); // initialized for standalone test

    String name;
    boolean alive;

    DbSettings settings = DbSettings.DEFAULT;

    // The native LevelDB insert/update/delete are normally thread-safe
    // However close operation is not thread-safe and may lead to a native crash when
    // accessing a closed DB.
    // The leveldbJNI lib has a protection over accessing closed DB but it is not synchronized
    // This ReadWriteLock still permits concurrent execution of insert/delete/update operations
    // however blocks them on init/close/delete operations
    private ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

    public LevelDbDataSource() {
    }

    public LevelDbDataSource(String name) {
        this.name = name;
        logger.debug("New LevelDbDataSource: " + name);
    }

    @Override
    public void init() {
        init(DbSettings.DEFAULT);
    }

    @Override
    public void init(DbSettings settings) {
        this.settings = settings;
        resetDbLock.writeLock().lock();
        try {
            logger.debug("~> LevelDbDataSource.init(): " + name);

            if (isAlive()) {
                return;
            }

            if (name == null) {
                throw new NullPointerException("no name set to the db");
            }

            try {
                logger.debug("Opening database");
                final Path dbPath = getPath();
                if (!Files.isSymbolicLink(dbPath.getParent())) {
                    Files.createDirectories(dbPath.getParent());
                }

                logger.debug("Initializing new or existing database: '{}'", name);

                alive = true;
            } catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
                throw new RuntimeException("Can't initialize database", ioe);
            }
            logger.debug("<~ LevelDbDataSource.init(): " + name);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    private Path getPath() {
        return Paths.get(config.databaseDir(), name);
    }

    @Override
    public void reset() {
        close();
        FileUtil.recursiveDelete(getPath().toString());
        init(settings);
    }

    @Override
    public byte[] prefixLookup(byte[] key, int prefixBytes) {
        throw new RuntimeException("LevelDbDataSource.prefixLookup() is not supported");
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
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> LevelDbDataSource.get(): " + name + ", key: " + toHexString(key));
            }
            return null;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void put(byte[] key, byte[] value) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> LevelDbDataSource.put(): " + name + ", key: " + toHexString(key) + ", " + (value == null ? "null" : value.length));
            }
            if (logger.isTraceEnabled()) {
                logger.trace("<~ LevelDbDataSource.put(): " + name + ", key: " + toHexString(key) + ", " + (value == null ? "null" : value.length));
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void delete(byte[] key) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> LevelDbDataSource.delete(): " + name + ", key: " + toHexString(key));
            }
            if (logger.isTraceEnabled()) {
                logger.trace("<~ LevelDbDataSource.delete(): " + name + ", key: " + toHexString(key));
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public Set<byte[]> keys() {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> LevelDbDataSource.keys(): " + name);
            }
            return null;
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    private void updateBatchInternal(Map<byte[], byte[]> rows) throws IOException {

    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> LevelDbDataSource.updateBatch(): " + name + ", " + rows.size());
            }
            try {
                updateBatchInternal(rows);
                if (logger.isTraceEnabled()) {
                    logger.trace("<~ LevelDbDataSource.updateBatch(): " + name + ", " + rows.size());
                }
            } catch (Exception e) {
                logger.error("Error, retrying one more time...", e);
                // try one more time
                try {
                    updateBatchInternal(rows);
                    if (logger.isTraceEnabled()) {
                        logger.trace("<~ LevelDbDataSource.updateBatch(): " + name + ", " + rows.size());
                    }
                } catch (Exception e1) {
                    logger.error("Error", e);
                    throw new RuntimeException(e);
                }
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public boolean flush() {
        return false;
    }

    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            if (!isAlive()) {
                return;
            }
            alive = false;

        } finally {
            resetDbLock.writeLock().unlock();
        }
    }
}
