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

import org.ethereum.datasource.Source;
import org.ethereum.db.BlockStore;
import org.ethereum.db.IndexedBlockStore;
import org.ethereum.db.PruneManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roman Mandeleil
 * Created on: 27/01/2015 01:05
 */
public class DefaultConfig {
    private static Logger logger = LoggerFactory.getLogger("general");

    CommonConfig commonConfig = CommonConfig.getDefault();

    SystemProperties config = SystemProperties.getDefault();

    public DefaultConfig() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
    }

    private BlockStore blockStore;

    public BlockStore blockStore() {
        if (blockStore == null) {
            //commonConfig.fastSyncCleanUp();
            IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
            Source<byte[], byte[]> block = commonConfig.cachedDbSource("block");
            Source<byte[], byte[]> index = commonConfig.cachedDbSource("index");
            indexedBlockStore.init(index, block);
            blockStore = indexedBlockStore;
        }
        return blockStore;
    }

    private PruneManager pruneManager;

    public PruneManager pruneManager() {
        if (pruneManager == null) {
            if (config.databasePruneDepth() >= 0) {
                pruneManager = new PruneManager((IndexedBlockStore) blockStore(), commonConfig.stateSource().getJournalSource(),
                        commonConfig.stateSource().getNoJournalSource(), config.databasePruneDepth());
            } else {
                pruneManager = new PruneManager(null, null, null, -1); // dummy
            }
        }
        return pruneManager;
    }
}
