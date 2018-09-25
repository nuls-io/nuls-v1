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

package io.nuls.client.rpc.resources.thread;

import io.nuls.client.rpc.resources.dto.UpgradeProcessDTO;
import io.nuls.client.rpc.resources.model.JarSig;
import io.nuls.client.rpc.resources.model.VersionFile;
import io.nuls.client.rpc.resources.util.FileUtil;
import io.nuls.client.version.SyncVersionRunner;
import io.nuls.client.version.constant.VersionConstant;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.crypto.Sha256Hash;
import io.nuls.core.tools.io.HttpDownloadUtils;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.VersionUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.func.TimeService;

import java.io.*;
import java.net.URLDecoder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Niels Wang
 */
public class UpgradeThread implements Runnable {

    private static final UpgradeThread INSTANCE = new UpgradeThread();

    private UpgradeThread() {
    }

    public static UpgradeThread getInstance() {
        return INSTANCE;
    }

    private Lock lock = new ReentrantLock();

    private boolean upgrading = false;
    private UpgradeProcessDTO process;
    private String version;

    @Override
    public void run() {
        if (!upgrading) {
            return;
        }
        try {
            SyncVersionRunner syncor = SyncVersionRunner.getInstance();
            this.version = syncor.getNewestVersion();
            String versionFileHash = syncor.getVersionFileHash();
            if (!VersionUtils.higherThan(version, NulsConfig.VERSION)) {
                setFailedMessage("The version is wrong!");
                return;
            }
            process.setPercentage(1);
            VersionFile versionJson = getVersionJson(syncor, versionFileHash);
            if (null == versionJson) {
                return;
            }
            process.setPercentage(5);
            process.setStatus(VersionConstant.DOWNLOADING);
            String root = this.getClass().getClassLoader().getResource("").getPath();
            root = URLDecoder.decode(root,"UTF-8");
            String newDirPath = root + "/temp/" + version;
            File newDir = new File(newDirPath);
            if (!newDir.exists()) {
                newDir.mkdirs();
            }
            String urlRoot = syncor.getRootUrl() + "/" + version;
            boolean result = this.download(urlRoot + "/bin.zip", newDirPath + "/bin.zip", versionJson.getBinSig());
            if (!result) {
                deleteTemp(root + "/temp/");
                return;
            }
            process.setPercentage(12);
            result = this.download(urlRoot + "/conf.zip", newDirPath + "/conf.zip", versionJson.getConfSig());
            if (!result) {
                deleteTemp(root + "/temp/");
                return;
            }
            process.setPercentage(15);
            File libsDir = new File(newDirPath + "/libs");
            if (libsDir.exists()) {
                FileUtil.deleteFolder(libsDir);
            }
            libsDir.mkdirs();
            process.setPercentage(20);
            int count = 0;
            int size = versionJson.getJarSigList().size();
            for (JarSig jarSig : versionJson.getJarSigList()) {
                File file = new File(root + "/libs/" + jarSig.getFileName());
                if (file.exists() && this.verifySig(file, Hex.decode(jarSig.getSig()))) {
                    FileUtil.copyFile(file, new File(newDirPath + "/libs/" + jarSig.getFileName()));
                    result = true;
                } else {
                    result = this.download(urlRoot + "/libs/" + jarSig.getFileName(), newDirPath + "/libs/" + jarSig.getFileName(), jarSig.getSig());
                }
                if (!result) {
                    deleteTemp(root + "/temp/");
                    return;
                }
                count++;
                process.setPercentage(20 + (count * 70) / (size));
            }
            process.setStatus(VersionConstant.INSTALLING);
            String oldDirPath = root + "/temp/old";
            result = this.copy(root + "/bin", oldDirPath + "/bin");
            if (!result) {
                deleteTemp(root + "/temp/");
                return;
            }
            process.setPercentage(92);
            result = this.copy(root + "/conf", oldDirPath + "/conf");
            if (!result) {
                deleteTemp(root + "/temp/");
                return;
            }
            process.setPercentage(95);
            result = this.copy(root + "/libs", oldDirPath + "/libs");
            if (!result) {
                deleteTemp(root + "/temp/");
                return;
            }
            process.setPercentage(100);
            process.setStatus(VersionConstant.WAITING_RESTART);
        } catch (Exception e) {
            Log.error(e);
            setFailedMessage(e.getMessage());
            upgrading = false;
        }
    }

    private void rollbackAndDeleteTemp(String root) throws UnsupportedEncodingException {
        File rootBin = new File(root + "/bin");
        FileUtil.deleteFolder(rootBin);
        File rootConf = new File(root + "/conf");
        FileUtil.deleteFolder(rootConf);
        File rootLibs = new File(root + "/libs");
        FileUtil.deleteFolder(rootLibs);

        FileUtil.copyFolder(new File(root + "/temp/old/bin"), rootBin);
        FileUtil.copyFolder(new File(root + "/temp/old/conf"), rootConf);
        FileUtil.copyFolder(new File(root + "/temp/old/libs"), rootLibs);

        FileUtil.deleteFolder(new File(root + "/temp"));

    }

    private void deleteTemp(String tempPath) {
        FileUtil.deleteFolder(new File(tempPath));
    }

    private VersionFile getVersionJson(SyncVersionRunner syncor, String versionFileHash) {
        if (!upgrading) {
            setFailedMessage("The upgrade has stopped");
            return null;
        }
        String jsonStr = null;
        try {
            byte[] bytes = HttpDownloadUtils.download(syncor.getRootUrl() + "/" + version + "/version.json");
            String hash = Hex.encode(Sha256Hash.hash(bytes));
            if (!hash.equals(versionFileHash)) {
                setFailedMessage("Signature verification is incorrect:" + version + "/version.json");
                return null;
            }
            jsonStr = new String(bytes, NulsConfig.DEFAULT_ENCODING);
        } catch (IOException e) {
            Log.error(e);
            setFailedMessage("Download version json faild!");
            return null;
        }
        try {
            return JSONUtils.json2pojo(jsonStr, VersionFile.class);
        } catch (Exception e) {
            Log.error(e);
            setFailedMessage("Parse version json faild!");
            return null;
        }
    }

    private boolean download(String url, String filePath, String signature) throws IOException {
        if (!upgrading) {
            setFailedMessage("The upgrade has stopped");
            return false;
        }
        byte[] bytes = HttpDownloadUtils.download(url);
        if (!verifySig(bytes, Hex.decode(signature))) {
            setFailedMessage("Signature verification is incorrect:" + url);
            return false;
        }
        boolean result = FileUtil.writeFile(bytes, filePath);
        if (!result) {
            setFailedMessage("Write file failed:" + filePath);
        }
        return result;
    }

    private boolean copy(String filePath, String targetPath) {
        if (!upgrading) {
            setFailedMessage("The upgrade has stopped");
            return false;
        }
        File source = new File(filePath);
        File target = new File(targetPath);
        if (source.isFile()) {
            FileUtil.copyFile(source, target);
        } else {
            FileUtil.copyFolder(source, target);
        }
        return true;
    }

    public boolean start() {
        lock.lock();
        try {
            if (!upgrading) {
                this.process = new UpgradeProcessDTO();
                this.upgrading = true;
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean stop() {
        lock.lock();
        try {
            if (upgrading) {
                upgrading = false;
            }
            String root = UpgradeThread.class.getClassLoader().getResource("").getPath();
            deleteTemp(root + "/temp/");
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean isUpgrading() {
        lock.lock();
        try {
            return upgrading;
        } finally {
            lock.unlock();
        }
    }

    public UpgradeProcessDTO getProcess() {
        if (!isUpgrading()) {
            return new UpgradeProcessDTO();
        }
        return process;
    }

    private void setFailedMessage(String message) {
        process.setStatus(VersionConstant.FAILED);
        process.setMessage(message);
        process.setTime(TimeService.currentTimeMillis());
    }

    private boolean verifySig(byte[] bytes, byte[] sig) {
        byte[] hash = Sha256Hash.hash(bytes);
        return VersionConstant.EC_KEY.verify(hash, sig);
    }

    private boolean verifySig(File file, byte[] sig) throws IOException {
        InputStream input = new FileInputStream(file);
        byte[] bytes;
        try {
            bytes = new byte[input.available()];
            input.read(bytes);
        } finally {
            input.close();
        }
        byte[] hash = Sha256Hash.hash(bytes);
        return VersionConstant.EC_KEY.verify(hash, sig);
    }
}
