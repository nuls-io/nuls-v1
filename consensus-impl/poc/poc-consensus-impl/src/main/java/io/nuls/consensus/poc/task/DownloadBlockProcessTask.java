/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.consensus.poc.task;

import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.process.DownloadBlockProcess;
import io.nuls.consensus.poc.provider.DownloadBlockProvider;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.protocol.constant.DownloadStatus;
import io.nuls.protocol.intf.DownloadService;

/**
 * Created by ln on 2018/4/13.
 */
public class DownloadBlockProcessTask implements Runnable {

    private DownloadBlockProcess downloadBlockProcess;
    private DownloadBlockProvider downloadBlockProvider;

    private DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);

    public DownloadBlockProcessTask(DownloadBlockProcess downloadBlockProcess, DownloadBlockProvider downloadBlockProvider) {
        this.downloadBlockProcess = downloadBlockProcess;
        this.downloadBlockProvider = downloadBlockProvider;
    }

    @Override
    public void run() {
        try {
            doTask();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void doTask() throws Exception {
        //wait consensus ready running
        if(downloadService.getStatus() != DownloadStatus.SUCCESS) {
            return;
        }
        BlockContainer blockContainer = downloadBlockProvider.get();
        downloadBlockProcess.process(blockContainer);
    }
}
