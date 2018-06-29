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
 *
 */

package io.nuls.client.version;

import io.nuls.client.rpc.resources.thread.UpgradeThread;
import io.nuls.client.rpc.resources.util.FileUtil;
import io.nuls.client.version.constant.VersionConstant;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.crypto.Sha256Hash;
import io.nuls.core.tools.io.HttpDownloadUtils;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * @author: Niels Wang
 */
public class SyncVersionRunner implements Runnable {
    /**
     * 从网络中获取的当前软件的最新版本号
     * The latest version of the current software obtained from the network.
     */
    private static String NEWEST_VERSION;
    private static String VERSION_FILE_HASH;
    private static String INFORMATION;

    private boolean first = true;

    private static final SyncVersionRunner INSTANCE = new SyncVersionRunner();

    private SyncVersionRunner() {

    }

    public static SyncVersionRunner getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        try {
            syncNewestVersionInfo();
            if (!first) {
                checkLocalTempFiles();
            }
            first = false;
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void checkLocalTempFiles() {
        if (!UpgradeThread.getInstance().isUpgrading()) {
            String root = SyncVersionRunner.class.getClassLoader().getResource("").getPath();
            File file = new File(root + "/temp");
            if (file.exists()) {
                FileUtil.deleteFolder(file);
            }
        }
    }

    private void syncNewestVersionInfo() throws NulsException, UnsupportedEncodingException {
        String jsonStr = null;
        try {
            jsonStr = new String(HttpDownloadUtils.download(VersionConstant.VERDION_JSON_URL), NulsConfig.DEFAULT_ENCODING);
        } catch (IOException e) {
            throw new NulsException(KernelErrorCode.DOWNLOAD_VERSION_FAILD);
        }
        Map<String, Object> map = null;
        try {
            map = JSONUtils.json2map(jsonStr);
        } catch (Exception e) {
            throw new NulsException(KernelErrorCode.PARSE_JSON_FAILD);
        }
        String version = (String) map.get("version");
        String versionFileHash = (String) map.get("versionFileHash");
        String signature = (String) map.get("signature");
        boolean result = VersionConstant.EC_KEY.verify(Sha256Hash.hash((version + "&" + versionFileHash).getBytes("UTF-8")), Hex.decode(signature));
        if (!result) {
            return;
        }
        NEWEST_VERSION = version;
        VERSION_FILE_HASH = versionFileHash;
        INFORMATION = (String) map.get("information");
    }

    public String getNewestVersion() {
        if (null == NEWEST_VERSION) {
            try {
                syncNewestVersionInfo();
            } catch (Exception e) {
                Log.error(e);
            }
        }
        return NEWEST_VERSION;
    }

    public String getVersionFileHash() {
        return VERSION_FILE_HASH;
    }

    public String getInformation() {
        return INFORMATION;
    }
}
