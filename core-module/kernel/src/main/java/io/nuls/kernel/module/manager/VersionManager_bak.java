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

package io.nuls.kernel.module.manager;

import io.nuls.core.tools.io.HttpDownloadUtils;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.VersionUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.NulsVersion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Niels Wang
 * @date: 2018/3/1
 */
public class VersionManager_bak {

    private static final String ROOT_URL = "https://raw.githubusercontent.com/nuls-io/nuls-wallet-release/master/";
    private static final String VERDION_JSON_URL = ROOT_URL + "version.json";
    private static final String DOWNLOAD_FILE_FOLDER_URL = ROOT_URL + "libs/";

    public static void start() throws NulsException {
        List<NulsVersion> versionList = null;
        try {
            versionList = SpringLiteContext.getBeanList(NulsVersion.class);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(e);
        }
        String localVersion = null;
        for (NulsVersion version : versionList) {
            if (null == localVersion) {
                localVersion = version.getVersion();
                continue;
            }
            if (VersionUtils.higherThan(version.getVersion(), localVersion)) {
                localVersion = version.getVersion();
                continue;
            }
        }
        NulsConfig.VERSION = localVersion;

//        URL libsUrl = VersionUtils.class.getClassLoader().getResource("libs");
//        if (null == libsUrl) {
//            //devlopment
//            return;
//        }
//
//        String jsonStr = null;
//        try {
//            jsonStr = new String(HttpDownloadUtils.download(HIGHEST_VERDION_FILE_URL), NulsConfig.DEFAULT_ENCODING);
//        } catch (IOException e) {
//            Log.error(e);
//            return;
//        }
//        Map<String, Object> map = null;
//        try {
//            map = JSONUtils.json2map(jsonStr);
//        } catch (Exception e) {
//            Log.error(e);
//            throw new NulsException(KernelErrorCode.FAILED, "Parse version json faild!");
//        }
//        String version = (String) map.get("version");
//        String sign = (String) map.get("sign");
//        //todo 验证签名
//        NulsConfig.NEWEST_VERSION = version;
//        //todo 自动更新开关
//        boolean autoUpdate = true;
//        if (VersionUtils.equalsWith(version, localVersion)) {
//            return;
//        } else if (autoUpdate) {
//            try {
//                updateFile(localVersion, version, versionList, libsUrl);
//            } catch (IOException e) {
//                throw new NulsException(e);
//            }
//        }
    }

    private static void updateFile(String oldVersion, String newVersion, List<NulsVersion> versionList, URL libsUrl) throws NulsException, IOException {
        String jsonStr = null;
        try {
            jsonStr = new String(HttpDownloadUtils.download(VERDION_JSON_URL), NulsConfig.DEFAULT_ENCODING);
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(KernelErrorCode.DOWNLOAD_VERSION_FAILD);
        }
        Map<String, Object> map = null;
        try {
            map = JSONUtils.json2map(jsonStr);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(KernelErrorCode.PARSE_JSON_FAILD);
        }
        String version = (String) map.get("version");
        String sign = (String) map.get("sign");
        //todo 验证签名,不通过则抛出异常
        //when the json change at the moment,restart;
        if (!newVersion.equals(version)) {
            start();
            return;
        }
        Map<String, String> jarMap = new HashMap<>();
        fillJarMap(libsUrl, jarMap);
        // jarMap is empty means it's development environment
        if (jarMap.isEmpty()) {
            return;
        }
        List<Map<String, Object>> libList = (List<Map<String, Object>>) map.get("libs");
        //check the temp folder exist,and delete
        URL rootUrl = VersionManager_bak.class.getResource("");
        File tempFolder = new File(rootUrl.getPath() + "/temp");
        if (tempFolder.exists()) {
            deleteFile(tempFolder);
        }
        tempFolder.mkdir();
        List<String> newVersionJarList = new ArrayList<>();
        for (Map<String, Object> lib : libList) {
            String file = (String) lib.get("file");
            newVersionJarList.add(file);
            String libSign = (String) lib.get("sign");
            //todo check sign
            if (jarMap.get(file) == null) {
                downloadLib(tempFolder.getPath(), file, sign);
            }
        }
        File bakFolder = new File(rootUrl.getPath() + "/bak");
        if (bakFolder.exists()) {
            deleteFile(bakFolder);
        }
        bakFolder.mkdir();
        List<String> removeList = new ArrayList<>();
        try {
            for (String key : jarMap.keySet()) {
                if (newVersionJarList.contains(key)) {
                    continue;
                }
                File jar = new File(jarMap.get(key));
                boolean b = jar.renameTo(new File(bakFolder.getPath() + "/" + key));
                if (!b) {
                    throw new NulsException(KernelErrorCode.FILE_OPERATION_FAILD);
                }
                removeList.add(jar.getName());
            }
        } catch (NulsException e) {
            List<String> failedList = new ArrayList<>();
            for (String fileName : removeList) {
                File file = new File(bakFolder.getPath() + "/" + fileName);
                boolean b = file.renameTo(new File(libsUrl.getPath() + "/" + fileName));
                if (!b) {
                    failedList.add(bakFolder.getPath() + "/" + fileName);
                }
            }
            failedOpration(failedList);
        }
        File[] files = tempFolder.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        List<String> moved = new ArrayList<>();
        try {
            for (File file : files) {
                boolean b = file.renameTo(new File(libsUrl.getPath() + "/" + file.getName()));
                if (!b) {
                    throw new NulsException(KernelErrorCode.FILE_OPERATION_FAILD);
                }
                moved.add(file.getName());
            }
        } catch (NulsException e) {
            for (String fileName : moved) {
                File newFile = new File(libsUrl.getPath() + "/" + fileName);
                if (newFile.exists()) {
                    newFile.delete();
                }
            }
            File[] bakFiles = bakFolder.listFiles();
            List<String> failedList = new ArrayList<>();
            for (File file : bakFiles) {
                boolean b = file.renameTo(new File(libsUrl.getPath() + "/" + file.getName()));
                if (!b) {
                    failedList.add(file.getPath());
                }
            }
            failedOpration(failedList);
        }
//todo 重启动，或者发送通知
    }

    private static void failedOpration(List<String> failedList) throws NulsException {
        if (!failedList.isEmpty()) {
            StringBuilder str = new StringBuilder();
            for (String fileName : failedList) {
                str.append(fileName);
                str.append(";");
            }
            throw new NulsException(KernelErrorCode.FILE_OPERATION_FAILD);
        }
    }

    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                deleteFile(sub);
            }
        }
        file.delete();
    }

    private static void fillJarMap(URL url, Map<String, String> jarMap) throws IOException {
        File folder = new File(url.getPath());
        for (File file : folder.listFiles()) {
            jarMap.put(file.getName(), file.getPath());
        }
    }

    private static void downloadLib(String folderPath, String file, String sign) throws NulsException {
        byte[] bytes;
        try {
            bytes = HttpDownloadUtils.download(DOWNLOAD_FILE_FOLDER_URL + file);
        } catch (IOException e) {
            throw new NulsException(e);
        }
        //验证签名

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(folderPath + "/" + file));
            outputStream.write(bytes);
            outputStream.flush();
        } catch (Exception e) {
            throw new NulsException(e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new NulsException(e);
            }
        }
    }


}
