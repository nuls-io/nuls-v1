package io.nuls.consensus.entity.block;

import io.nuls.core.chain.entity.BlockHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2018/1/12
 */
public class BifurcateProcessor {

    private static final BifurcateProcessor INSTANCE = new BifurcateProcessor();

    private List<BlockHeaderChain> chainList = new ArrayList<>();

    private BifurcateProcessor() {
    }

    public static BifurcateProcessor getInstance() {
        return INSTANCE;
    }

    public void addHeader(BlockHeader header){
        for(BlockHeaderChain chain:chainList){
//todo            chain.get
        }
    }

    public void removeHeader(String hash){
//todo
    }

    public BlockHeaderChain getLongestChain(){
        List<BlockHeaderChain> longestChainList = new ArrayList<>();
        for(BlockHeaderChain chain :chainList){
            if(longestChainList.isEmpty()||chain.size()>longestChainList.get(0).size()){
                longestChainList.clear();
                longestChainList.add(chain);
            }else if(longestChainList.isEmpty()||chain.size()==longestChainList.get(0).size()){
                longestChainList.add(chain);
            }
        }
        if(longestChainList.size()>1||longestChainList.isEmpty()){
            return null;
        }
        return longestChainList.get(0);
    }




}
