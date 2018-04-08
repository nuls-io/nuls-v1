package io.nuls.consensus.download;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;

import java.util.List;
import java.util.Random;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadUtils {

    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);

    public Block getBlockByHash(String hash) {
        List<Node> nodes = networkService.getAvailableNodes();
        if(nodes == null || nodes.size() == 0) {
            throw  new NulsRuntimeException(ErrorCode.NET_NODE_NOT_FOUND);
        }
        Node node = nodes.get(new Random().nextInt(nodes.size()));
        return getBlockByHash(hash, node);
    }

    public Block getBlockByHash(String hash, Node node) {
        List<Block> blocks = getBlocks(node, hash, hash, -1l, 1);
        if(blocks == null || blocks.size() == 0) {
            return null;
        } else {
            return blocks.get(0);
        }
    }

    public List<Block> getBlocks(Node node, String startHash, String endHash, long startHeight, int size) {
        return null;
    }
}
