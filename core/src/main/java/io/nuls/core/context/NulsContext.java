package io.nuls.core.context;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.module.manager.ServiceManager;

/**
 * @author Niels
 */
public class NulsContext {

    private NulsContext() {
    }

    private static final NulsContext NC = new NulsContext();
    public static String DEFAULT_ENCODING = "UTF-8";

    /**
     * get zhe only instance of NulsContext
     *
     * @return
     */
    public static final NulsContext getInstance() {
        return NC;
    }

    /**
     * cache the best block
     */
    private Block bestBlock;
    private Block genesisBlock;

    public static String nulsVersion = "1.0";

    public Block getGenesisBlock() {
        return genesisBlock;
    }

    public void setGenesisBlock(Block block){
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

}
