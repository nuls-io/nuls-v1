package io.nuls.consensus.constant;

/**
 * @author Niels
 * @date 2018/1/9
 */
public interface ConsensusCacheConstant {


    /**
     * 2 minutes alive
     */
    int LIVE_TIME = 120;

    String BLOCK_HEADER_CACHE_NAME = "block-header-cache";
    String SMALL_BLOCK_CACHE_NAME = "small-block-cache";
    String BLOCK_CACHE_NAME = "block-cache";
}
