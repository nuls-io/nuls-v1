package io.nuls.consensus.poc.process;

import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.core.utils.log.ChainLog;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.base.download.DownloadUtils;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Block;

import java.io.IOException;

/**
 * Created by ln on 2018-04-18.
 */
public class DownloadBlockProcess {

    private BlockProcess blockProcess;

    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);

    public void process(BlockContainer blockContainer) throws IOException {
        downloadBlock(blockContainer);
    }

    private boolean downloadBlock(BlockContainer blockContainer) throws IOException {
        if(blockContainer.getNode() == null) {
            Log.warn("Handling an Orphan Block Error, Unknown Source Node");
            return false;
        }

        Block preBlock = new DownloadUtils().getBlockByHash(blockContainer.getBlock().getHeader().getPreHash().getDigestHex(), blockContainer.getNode());
        if(preBlock != null) {
            ChainLog.debug("get pre block success {} - {}", preBlock.getHeader().getHeight(), preBlock.getHeader().getHash().getDigestHex());

            addBlock(new BlockContainer(preBlock, blockContainer.getNode(), BlockContainerStatus.DOWNLOADING));
            return true;
        } else {
            //ChainLog.debug("get pre block fail {} - {}", preBlock.getHeader().getHeight(), preBlock.getHeader().getHash().getDigestHex());

            //失败情况的处理，从其它所以可用的节点去获取，如果都不成功，那么就失败，包括本次失败的节点，再次获取一次
            for(Node node : networkService.getAvailableNodes()) {
                if(node.getVersionMessage().getBestBlockHeight() >= blockContainer.getBlock().getHeader().getHeight()) {
                    preBlock = new DownloadUtils().getBlockByHash(blockContainer.getBlock().getHeader().getPreHash().getDigestHex(), blockContainer.getNode());
                    if(preBlock != null) {
                        addBlock(new BlockContainer(preBlock, node, BlockContainerStatus.DOWNLOADING));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private void addBlock(BlockContainer blockContainer) throws IOException {
        blockProcess.addBlock(blockContainer);
    }

    public void setBlockProcess(BlockProcess blockProcess) {
        this.blockProcess = blockProcess;
    }
}
