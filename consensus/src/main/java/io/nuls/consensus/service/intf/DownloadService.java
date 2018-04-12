package io.nuls.consensus.service.intf;

import io.nuls.consensus.constant.DownloadStatus;

/**
 * Created by ln on 2018/4/8.
 */
public interface DownloadService {

    boolean start();

    boolean stop();

    boolean reset();

    DownloadStatus getStatus();
}
