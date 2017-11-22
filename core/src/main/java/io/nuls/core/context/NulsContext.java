package io.nuls.core.context;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.manager.ModuleManager;
import io.nuls.core.module.NulsModule;

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

    public Block getGenesisBlock() {
        Block block = new Block();
        BlockHeader header = new BlockHeader(1, System.currentTimeMillis());
        block.setHeader(header);

        return block;
    }

    /**
     * get NulsModule Object
     *
     * @param moduleName
     * @return
     */
    public NulsModule getModule(String moduleName) {
        return ModuleManager.getInstance().getModule(moduleName);
    }

    public NulsModule getModule(Class moduleClass) {
        return ModuleManager.getInstance().getModule(moduleClass);
    }

    public NulsModule getModule(int moduleId) {
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

    public int getVersion() {
        return 0;
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
