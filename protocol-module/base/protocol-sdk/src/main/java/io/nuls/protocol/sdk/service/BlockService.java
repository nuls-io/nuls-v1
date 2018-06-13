package io.nuls.protocol.sdk.service;

import io.nuls.sdk.model.Result;

/**
 * @author: Charlie
 * @date: 2018/6/13
 */
public interface BlockService {

    /**
     * Get the block header accoding to block height
     * @param height The block height
     * @return
     * If the operation is successful, 'success' is true, and data is ;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getHeaderByHeight(int height);


    /**
     * Get the block header accoding to block hash
     * @param hash The block hash
     * @return
     * If the operation is successful, 'success' is true, and data is ;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getHeaderByHash(String hash);

    /**
     * Get the block accoding to block height
     * @param height The block height
     * @return
     * If the operation is successful, 'success' is true, and data is ;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getBlockByHeight(int height);

    /**
     * Get the block accoding to block hash
     * @param hash The block hash
     * @return
     * If the operation is successful, 'success' is true, and data is ;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getBlockByHash(String hash);
}
