package io.nuls.core.context;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.manager.ModuleManager;
import io.nuls.core.module.BaseNulsModule;

import java.util.Date;

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

    public static String nulsVersion;

    public Block getGenesisBlock() {
        Block block = new Block(1,1, Sha256Hash.twiceOf("0000".getBytes()));
        return block;
    }

    /**
     * get BaseNulsModule Object
     *
     * @param moduleName
     * @return
     */
    public BaseNulsModule getModule(String moduleName) {
        return ModuleManager.getInstance().getModule(moduleName);
    }

    public BaseNulsModule getModule(Class moduleClass) {
        return ModuleManager.getInstance().getModule(moduleClass);
    }

    public BaseNulsModule getModule(int moduleId) {
        return ModuleManager.getInstance().getModuleById(moduleId);
    }

    /**
     * get Service by interface
     *
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> T getService(Class<T> tClass) {
        return ModuleManager.getInstance().getService(tClass);
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
