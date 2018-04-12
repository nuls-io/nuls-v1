package io.nuls.consensus.download;

import io.nuls.consensus.constant.DownloadStatus;
import io.nuls.consensus.service.intf.DownloadService;

/**
 * Created by ln on 2018/4/8.
 */
public class DownloadServiceImpl implements DownloadService {

    private DownloadProcessor processor = DownloadProcessor.getInstance();

    @Override
    public boolean start() {
        return processor.startup();
    }

    @Override
    public boolean stop() {
        return processor.shutdown();
    }

    @Override
    public boolean reset() {
        processor.setDownloadStatus(DownloadStatus.WAIT);
        return true;
    }

    @Override
    public DownloadStatus getStatus() {
        return processor.getDownloadStatus();
    }
}
