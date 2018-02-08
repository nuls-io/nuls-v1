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
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.spring.lite.core.SpringLiteContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Niels
 */
public class NulsContext {

    private static final HashMap<String, Short> CHAIN_ID_MAP = new HashMap<String, Short>();
    public static String DEFAULT_ENCODING = "UTF-8";
    public static String CHAIN_ID = "NULS";
    public static IniEntity NULS_CONFIG;
    public static IniEntity MODULES_CONFIG;

    public static byte[] getMagicNumber() {
        return MAGIC_NUMBER;
    }

    public static void setMagicNumber(byte[] magicNumber) {
        MAGIC_NUMBER = magicNumber;
    }

    public static byte[] MAGIC_NUMBER;

    private NulsContext() {
        CHAIN_ID = "NULS";
        CHAIN_ID_MAP.put(CHAIN_ID, Short.parseShort("1"));
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

    public String getModuleVersion(String module) {
        return "";
    }

    public Block getBestBlock() {
        if (bestBlock == null) {
            if (bestBlock == null) {
                bestBlock = getGenesisBlock();
            }
        }
        return bestBlock;
    }

    public void setBestBlock(Block bestBlock) {
        this.bestBlock = bestBlock;
    }

    public Short getChainId(String chainName) {
        return CHAIN_ID_MAP.get(chainName);
    }

    public void addChainId(String chainName, Short id) {
        CHAIN_ID_MAP.put(chainName, id);
    }

    public Na getTxFee() {
        return txFee;
    }

    public void setTxFee(Na txFee) {
        this.txFee = txFee;
    }

    public static final <T> T getServiceBean(Class<T> tClass) {
        try {
            return SpringLiteContext.getBean(tClass);
        } catch (Exception e) {
            return getServiceBean(tClass,0L);
        }
    }

    private static <T> T getServiceBean(Class<T> tClass, long l) {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e1) {
            Log.error(e1);
        }
        try {
            return SpringLiteContext.getBean(tClass);
        } catch (Exception e) {
            if(l>60000){
                Log.error(e);
                return null;
            }
            return getServiceBean(tClass,l+100L);
        }

    }
}
