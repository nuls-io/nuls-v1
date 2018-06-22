package io.nuls.protocol.sdk.service;

import io.nuls.sdk.model.Result;

/**
 * @author: Charlie
 * @date: 2018/6/13
 */
public interface BlockService {


    /**
     *Get the Newest block hight
     * @return
     * If the operation is successful, 'success' is true, and data is Long type;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getNewestBlockHight();

    /**
     *Get the Newest block Hash
     * @return
     * If the operation is successful, 'success' is true, and data is Long type;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getNewestBlockHash();

    /**
     * Get the Newest block header
     * @return
     * If the operation is successful, 'success' is true, and data is blockHeaderDto;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getNewestBlockHeader();

    /**
     * Get the block header accoding to block height
     * @param height The block height
     * @return
     * If the operation is successful, 'success' is true, and data is blockHeaderDto;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getBlockHeader(int height);


    /**
     * Get the block header accoding to block hash
     * @param hash The block hash
     * @return
     * If the operation is successful, 'success' is true, and data is blockHeaderDto;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getBlockHeader(String hash);

    /**
     * Get the block accoding to block height
     * @param height The block height
     * @return
     * If the operation is successful, 'success' is true, and data is BlockDto;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getBlock(int height);

    /**
     * Get the block accoding to block hash
     * @param hash The block hash
     * @return
     * If the operation is successful, 'success' is true, and data is BlockDto;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getBlock(String hash);
}
