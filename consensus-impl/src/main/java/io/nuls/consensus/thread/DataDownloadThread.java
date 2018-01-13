package io.nuls.consensus.thread;

import io.nuls.consensus.utils.DownloadDataUtils;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2018/1/12
 */
public class DataDownloadThread implements Runnable {
    private DownloadDataUtils downloadDataUtils ;
    @Override
    public void run() {
        try {
            Thread.sleep(1000L);
            if(null==downloadDataUtils){
                downloadDataUtils = DownloadDataUtils.getInstance();
            }
            downloadDataUtils.reRequest();
        } catch (InterruptedException e) {
            Log.error(e);
        }

    }
}
