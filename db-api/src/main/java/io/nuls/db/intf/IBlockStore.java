package io.nuls.db.intf;

import io.nuls.db.entity.Block;
import io.nuls.db.entity.BlockHeader;

import java.util.List;


/**
 * Created by zhouwei on 2017/9/29.
 */
public interface IBlockStore extends IStore<Block,String>{

    /**
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<Block> getList(Integer pageNum, Integer pageSize);

    /**
     * 获取最新区块
     * @return
     */
    Block getBestBlock();

    /**
     *  通过区块hash获取区块信息
     * @param hash
     * @return
     */
    Block getByKey(byte[] hash);
    /**
     * 通过高度获取区块信息
     * @param height 区块高度
     * @return
     */
    Block getBlockByHeight(long height);

    /**
     * 通过hash值获取区块头信息
     * @param hash
     * @return
     */
    BlockHeader getBlockHeaderByKey(String hash);


    /**
     * 通过hash值获取区块头信息
     * @param hash
     * @return
     */
    BlockHeader getBlockHeaderByKey(byte[] hash);

    /**
     * 通过区块高度获取区块头信息
     * @param height
     * @return
     */
    BlockHeader getBlockHeaderByHeight(long height);
}
