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
package org.ethereum.config;

import org.ethereum.core.Repository;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.*;
import org.ethereum.datasource.inmem.HashMapDB;
import org.ethereum.datasource.leveldb.LevelDbDataSource;
import org.ethereum.db.DbFlushManager;
import org.ethereum.db.HeaderStore;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.db.StateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class CommonConfig {
    private static final Logger logger = LoggerFactory.getLogger("general");
    private Set<DbSource> dbSources = new HashSet<>();

    private static CommonConfig defaultInstance;

    public static CommonConfig getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new CommonConfig();
        }
        return defaultInstance;
    }

    public SystemProperties systemProperties() {
        return SystemProperties.getSpringDefault();
    }

    public Repository defaultRepository() {
        return new RepositoryRoot(stateSource(), null);
    }

    public Repository repository(byte[] stateRoot) {
        return new RepositoryRoot(stateSource(), stateRoot);
    }

    /**
     * A source of nodes for state trie and all contract storage tries. <br/>
     * This source provides contract code too. <br/><br/>
     * <p>
     * Picks node by 16-bytes prefix of its key. <br/>
     * Within {@link NodeKeyCompositor} this source is a part of ref counting workaround<br/><br/>
     *
     * <b>Note:</b> is eligible as a public node provider, like in {@link Eth63};
     * {@link StateSource} is intended for inner usage only
     *
     * @see NodeKeyCompositor
     * @see RepositoryRoot#RepositoryRoot(Source, byte[])
     * @see Eth63
     */
    private Source<byte[], byte[]> trieNodeSource;

    public Source<byte[], byte[]> trieNodeSource() {
        if (trieNodeSource == null) {
            DbSource<byte[]> db = blockchainDB();
            Source<byte[], byte[]> src = new PrefixLookupSource<>(db, NodeKeyCompositor.PREFIX_BYTES);
            trieNodeSource = new XorDataSource<>(src, HashUtil.sha3("state".getBytes()));
        }
        return trieNodeSource;
    }

    private StateSource stateSource;

    public StateSource stateSource() {
        if (this.stateSource == null) {
            //fastSyncCleanUp();
            StateSource stateSource = new StateSource(blockchainSource("state"),
                    systemProperties().databasePruneDepth() >= 0);

            dbFlushManager().addCache(stateSource.getWriteCache());

            this.stateSource = stateSource;
        }
        return this.stateSource;
    }

    public Source<byte[], byte[]> cachedDbSource(String name) {
        AbstractCachedSource<byte[], byte[]> writeCache = new AsyncWriteCache<byte[], byte[]>(blockchainSource(name)) {
            @Override
            protected WriteCache<byte[], byte[]> createCache(Source<byte[], byte[]> source) {
                WriteCache.BytesKey<byte[]> ret = new WriteCache.BytesKey<>(source, WriteCache.CacheType.SIMPLE);
                ret.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
                ret.setFlushSource(true);
                return ret;
            }
        }.withName(name);
        dbFlushManager().addCache(writeCache);
        return writeCache;
    }

    public Source<byte[], byte[]> blockchainSource(String name) {
        return new XorDataSource<>(blockchainDbCache(), HashUtil.sha3(name.getBytes()));
    }

    private AbstractCachedSource<byte[], byte[]> blockchainDbCache;

    public AbstractCachedSource<byte[], byte[]> blockchainDbCache() {
        if (blockchainDbCache == null) {
            WriteCache.BytesKey<byte[]> ret = new WriteCache.BytesKey<>(
                    new BatchSourceWriter<>(blockchainDB()), WriteCache.CacheType.SIMPLE);
            ret.setFlushSource(true);
            blockchainDbCache = ret;
        }
        return blockchainDbCache;
    }

    public DbSource<byte[]> keyValueDataSource(String name) {
        return keyValueDataSource(name, DbSettings.DEFAULT);
    }

    public DbSource<byte[]> keyValueDataSource(String name, DbSettings settings) {
        String dataSource = systemProperties().getKeyValueDataSource();
        try {
            DbSource<byte[]> dbSource;
            if ("inmem".equals(dataSource)) {
                dbSource = new HashMapDB<>();
            } else {
                dbSource = levelDbDataSource();
            }
            dbSource.setName(name);
            dbSource.init(settings);
            dbSources.add(dbSource);
            return dbSource;
        } finally {
            logger.debug(dataSource + " key-value data source created: " + name);
        }
    }

    protected LevelDbDataSource levelDbDataSource() {
        return new LevelDbDataSource();
    }

    private void resetDataSource(Source source) {
        if (source instanceof DbSource) {
            ((DbSource) source).reset();
        } else {
            throw new Error("Cannot cleanup non-db Source");
        }
    }

    private DbSource<byte[]> headerSource;

    public DbSource<byte[]> headerSource() {
        if (headerSource == null) {
            headerSource = keyValueDataSource("headers");
        }
        return headerSource;
    }

    private HeaderStore headerStore;

    public HeaderStore headerStore() {
        if (this.headerStore == null) {
            DbSource<byte[]> dataSource = headerSource();

            WriteCache.BytesKey<byte[]> cache = new WriteCache.BytesKey<>(
                    new BatchSourceWriter<>(dataSource), WriteCache.CacheType.SIMPLE);
            cache.setFlushSource(true);
            dbFlushManager().addCache(cache);

            HeaderStore headerStore = new HeaderStore();
            Source<byte[], byte[]> headers = new XorDataSource<>(cache, HashUtil.sha3("header".getBytes()));
            Source<byte[], byte[]> index = new XorDataSource<>(cache, HashUtil.sha3("index".getBytes()));
            headerStore.init(index, headers);

            this.headerStore = headerStore;
        }
        return this.headerStore;
    }

    private DbSource<byte[]> blockchainDB;

    public DbSource<byte[]> blockchainDB() {
        if (blockchainDB == null) {
            DbSettings settings = DbSettings.newInstance()
                    .withMaxOpenFiles(systemProperties().getConfig().getInt("database.maxOpenFiles"))
                    .withMaxThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));

            blockchainDB = keyValueDataSource("blockchain", settings);
        }
        return blockchainDB;
    }

    private DbFlushManager dbFlushManager;

    public DbFlushManager dbFlushManager() {
        if (dbFlushManager == null) {
            dbFlushManager = new DbFlushManager(systemProperties(), dbSources, blockchainDbCache());
        }
        return dbFlushManager;
    }

}
