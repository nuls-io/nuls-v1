/**
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
package io.nuls.core.context;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.module.manager.ServiceManager;
import io.nuls.core.utils.cfg.IniEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Niels
 */
public class NulsContext {

    private static final HashMap<String, Integer> CHAIN_ID_MAP = new HashMap<String, Integer>();
    public static String DEFAULT_ENCODING = "UTF-8";
    public static String CHAIN_ID = "NULS";
    public static IniEntity NULS_CONFIG;
    public static IniEntity MODULES_CONFIG;

    private NulsContext() {
        CHAIN_ID = "NULS";
        CHAIN_ID_MAP.put(CHAIN_ID, 1);
    }

    private static final NulsContext NC = new NulsContext();

    /**
     * get zhe only instance of NulsContext
     *
     * @return
     */
    public static final NulsContext getInstance() {
        return NC;
    }

    private Na txFee;
    /**
     * cache the best block
     */
    private Block bestBlock;
    private Block genesisBlock;

    public static Set<String> LOCAL_ADDRESS_LIST = new HashSet<>();
    public static String DEFAULT_ACCOUNT_ID;

    public static String nulsVersion = "1.0";

    public Block getGenesisBlock() {
        return genesisBlock;
    }

    public void setGenesisBlock(Block block) {
        this.genesisBlock = block;
    }

    /**
     * get Service by interface
     *
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> T getService(Class<T> tClass) {
        return ServiceManager.getInstance().getService(tClass);
    }


    public String getModuleVersion(String module) {
        return "";
    }

    public Block getBestBlock() {
        if (bestBlock == null) {
            // find the best from database

            //bestBlock = blockDao().getBestBlock();

            //when database not found create GenesisBlock
            if (bestBlock == null) {
                bestBlock = getGenesisBlock();
            }
        }
        return bestBlock;
    }

    public void setBestBlock(Block bestBlock) {
        this.bestBlock = bestBlock;
    }

    public int getChainId(String chainName) {
        return CHAIN_ID_MAP.get(chainName);
    }

    public void addChainId(String chainName, Integer id) {
        CHAIN_ID_MAP.put(chainName, id);
    }

    public Na getTxFee() {
        return txFee;
    }

    public void setTxFee(Na txFee) {
        this.txFee = txFee;
    }

    public static final <T> T getServiceBean(Class<T> tClass) {
        return getInstance().getService(tClass);
    }
}
