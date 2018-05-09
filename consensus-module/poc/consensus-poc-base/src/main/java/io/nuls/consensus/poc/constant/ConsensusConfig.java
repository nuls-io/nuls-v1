/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.consensus.poc.constant;

import io.nuls.account.model.Address;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ln on 2018/4/13.
 */
public class ConsensusConfig {

    private final static String CFG_CONSENSUS_SECTION = "consensus";
    private final static String PROPERTY_PARTAKE_PACKING = "partake.packing";
    private final static String PROPERTY_SEED_NODES = "seed.nodes";
    private final static String SEED_NODES_DELIMITER = ",";

    private static boolean partakePacking = false;
    private static List<byte[]> seedNodeList = new ArrayList<>();

    public static void initConfiguration() throws Exception {

        Block genesisBlock = GenesisBlock.getInstance();
        NulsContext.getInstance().setGenesisBlock(genesisBlock);

        partakePacking = NulsConfig.MODULES_CONFIG.getCfgValue(CFG_CONSENSUS_SECTION, PROPERTY_PARTAKE_PACKING, false);
        Set<String> seedAddressSet = new HashSet<>();
        String addresses = NulsConfig.MODULES_CONFIG.getCfgValue(CFG_CONSENSUS_SECTION, PROPERTY_SEED_NODES, "");
        if (StringUtils.isBlank(addresses)) {
            return;
        }
        String[] array = addresses.split(SEED_NODES_DELIMITER);
        if (null == array) {
            return;
        }
        for (String address : array) {
            seedAddressSet.add(address);
        }
        for(String address : seedAddressSet) {
            Address ads = Address.fromHashs(address);
            seedNodeList.add(ads.getBase58Bytes());
        }
    }

    public static boolean isPartakePacking() {
        return partakePacking;
    }

    public static List<byte[]> getSeedNodeList() {
        return seedNodeList;
    }
}
