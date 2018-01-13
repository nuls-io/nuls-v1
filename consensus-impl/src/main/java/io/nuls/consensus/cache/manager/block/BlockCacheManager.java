package io.nuls.consensus.cache.manager.block;

import io.nuls.cache.util.CacheMap;
import io.nuls.consensus.constant.ConsensusCacheConstant;
import io.nuls.consensus.event.GetBlockHeaderEvent;
import io.nuls.consensus.utils.DownloadDataUtils;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.SmallBlock;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.service.NetworkService;

/**
 * @author Niels
 * @date 2017/12/12
 */
public class BlockCacheManager {
    private static final String HEIGHT_HASH_CACHE = "blocks-height-hash";
    private static final BlockCacheManager INSTANCE = new BlockCacheManager();

    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);
    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);

    private CacheMap<String, BlockHeader> headerCacheMap;

    private CacheMap<String, Block> blockCacheMap;
    private CacheMap<String, SmallBlock> smallBlockCacheMap;

    private DownloadDataUtils downloadDataUtils = DownloadDataUtils.getInstance();

    private long bestHeight;
    private long storedHeight;

    private BlockCacheManager() {
    }

    public static BlockCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        smallBlockCacheMap = new CacheMap<>(ConsensusCacheConstant.SMALL_BLOCK_CACHE_NAME, 32, ConsensusCacheConstant.LIVE_TIME, 0);
        blockCacheMap = new CacheMap<>(ConsensusCacheConstant.BLOCK_CACHE_NAME, 64, ConsensusCacheConstant.LIVE_TIME, 0);
        headerCacheMap = new CacheMap<>(ConsensusCacheConstant.TEMP_BLOCK_HEADER_CACHE_NAME, 32, ConsensusCacheConstant.LIVE_TIME, 0);
    }

    public void cacheBlockHeader(BlockHeader header, String sender) {
        long height = header.getHeight();
        boolean discard = true;
        do {
            ValidateResult result = header.verify();
            if (result.isFailed()) {
                discard = true;
                break;
            }
            if (height <= storedHeight) {
                discard = true;
                break;
            }
            if (height <= bestHeight) {
                //todo 验证出块人、签名，如果签名正确，则处罚（分叉[red]、网络延时出块失败[yellow]）


            }
            long nextHeight = 1 + bestHeight;
            if (height == nextHeight) {
                discard = false;
                break;
            }
            if (height > nextHeight) {
                headerCacheMap.put(header.getHash().getDigestHex(), header);
                GetBlockHeaderEvent event = new GetBlockHeaderEvent();
                event.setEventBody(new BasicTypeData<>(height - 1));
                eventBroadcaster.sendToNode(event, sender);
                discard = true;
                break;
            }
        } while (false);
        if (discard) {
            return;
        }
        headerCacheMap.put(header.getHash().getDigestHex(), header);
        downloadDataUtils.requestSmallBlock(header.getHash(), sender);
        checkNextBlockHeader(header.getHash().getDigestHex(), sender);
    }

    private void checkNextBlockHeader(String preHash, String nodeId) {
        for (BlockHeader header : headerCacheMap.values()) {
            if (header.getPreHash().getDigestHex().equals(preHash)) {
                this.cacheBlockHeader(header, nodeId);
            }
        }
    }

    public BlockHeader getBlockHeader(String hash) {
        return headerCacheMap.get(hash);
    }

    public void cacheBlock(Block block) {
        blockCacheMap.put(block.getHeader().getHash().getDigestHex(), block);
    }

    public Block getBlock(String hash) {
        return blockCacheMap.get(hash);
    }

    public void cacheSmallBlock(SmallBlock smallBlock, String nodeId) {
        smallBlockCacheMap.put(smallBlock.getBlockHash().getDigestHex(), smallBlock);
        downloadDataUtils.requestTxGroup(smallBlock.getBlockHash(), nodeId);
    }

    public SmallBlock getSmallBlock(String hash) {
        return smallBlockCacheMap.get(hash);
    }

    public void clear() {
        this.blockCacheMap.clear();
        this.headerCacheMap.clear();
        this.smallBlockCacheMap.clear();
    }

    public void destroy() {
        this.blockCacheMap.destroy();
        this.headerCacheMap.destroy();
        this.smallBlockCacheMap.destroy();
    }

    public void removeBlock(String hash) {
        this.blockCacheMap.remove(hash);
        this.smallBlockCacheMap.remove(hash);
        this.headerCacheMap.remove(hash);
    }

    public long getBestHeight() {
        return bestHeight;
    }

    public long getStoredHeight() {
        return storedHeight;
    }

    public void setStoredHeight(long storedHeight) {
        this.storedHeight = storedHeight;
    }

    public Block getConfirmedBlock(long height) {
        //todo 这里改成主动的
//        Set<String> hashset = blockHeightCacheMap.get(height);
//        if (null == hashset) {
//            return null;
//        }
//        String rightBlockHash = null;
//        int confirmedCount = 0;
//        for (String hash : hashset) {
//            int count = hashConfirmedCountMap.get(hash);
//            if (count > confirmedCount) {
//                rightBlockHash = hash;
//                confirmedCount = count;
//            }
//        }
//        if (confirmedCount <= PocConsensusConstant.CONFIRM_BLOCK_COUNT) {
//            return null;
//        }
//        return blockCacheMap.get(rightBlockHash);
        return null;
    }

    public BlockHeader getBlockHeader(long height) {
//todo       Set<String> hashset = blockHeightCacheMap.get(height);
//        if (null == hashset || hashset.size() != 1) {
//            return null;
//        }
//        for (String hash : hashset) {
//            return getBlockHeader(hash);
//        }
        return null;
    }

    public Block getBlock(long height) {
        // todo auto-generated method stub(niels)
        return null;
    }
}
