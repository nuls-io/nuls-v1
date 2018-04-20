/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.poc.protocol.context;

import io.nuls.consensus.poc.protocol.constant.PocConsensusConstant;
import io.nuls.consensus.poc.protocol.model.block.GenesisBlock;
import io.nuls.consensus.poc.protocol.service.BlockService;
import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.poc.constant.ConsensusStatus;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.BaseNulsData;
import io.nuls.protocol.model.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ln on 2018/4/13.
 */
public class ConsensusContext {

    private static boolean partakePacking = false;
    private static List<String> seedNodeList;

    public static void initConfiguration() {

        Block genesisBlock = GenesisBlock.getInstance();
        NulsContext.getInstance().setGenesisBlock(genesisBlock);

        partakePacking = NulsConfig.MODULES_CONFIG.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_PARTAKE_PACKING, false);
        seedNodeList = new ArrayList<>();
        Set<String> seedAddressSet = new HashSet<>();
        String addresses = NulsConfig.MODULES_CONFIG.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_SEED_NODES, "");
        if (StringUtils.isBlank(addresses)) {
            return;
        }
        String[] array = addresses.split(PocConsensusConstant.SEED_NODES_DELIMITER);
        if (null == array) {
            return;
        }
        for (String address : array) {
            seedAddressSet.add(address);
        }
        ConsensusContext.seedNodeList.addAll(seedAddressSet);
    }

    public static boolean isPartakePacking() {
        return partakePacking;
    }

    public static List<String> getSeedNodeList() {
        return seedNodeList;
    }
}
