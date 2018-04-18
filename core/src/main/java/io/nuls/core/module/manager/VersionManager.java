package io.nuls.core.module.manager;

import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.intf.NulsVersion;
import io.nuls.core.utils.io.HttpDownloadUtils;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.spring.lite.core.SpringLiteContext;
import io.nuls.core.utils.str.VersionUtils;

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
public class VersionManager {

    private static final String ROOT_URL = "https://raw.githubusercontent.com/niels1286/testVersion/master/";
    private static final String HIGHEST_VERDION_FILE_URL = ROOT_URL + "versionNo.json";
    private static final String VERDION_JSON_URL = ROOT_URL + "version.json";
    private static final String DOWNLOAD_FILE_FOLDER_URL = ROOT_URL + "libs/";


    public static void start() throws NulsException {
        URL libsUrl = VersionUtils.class.getClassLoader().getResource("libs");
        if (null == libsUrl) {
            //devlopment
            return;
        }
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
        String jsonStr = null;
        try {
            jsonStr = new String(HttpDownloadUtils.download(HIGHEST_VERDION_FILE_URL), NulsConfig.DEFAULT_ENCODING);
        } catch (IOException e) {
            Log.error(e);
            return;
        }
        Map<String, Object> map = null;
        try {
            map = JSONUtils.json2map(jsonStr);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(ErrorCode.FAILED, "Parse version json faild!");
        }
        String version = (String) map.get("version");
        String sign = (String) map.get("sign");
        //todo 验证签名
        NulsConfig.NEWEST_VERSION = version;
        //todo 自动更新开关
        boolean autoUpdate = true;
        if (VersionUtils.equalsWith(version, localVersion)) {
            return;
        } else if (autoUpdate) {
            try {
                updateFile(localVersion, version, versionList, libsUrl);
            } catch (IOException e) {
                throw new NulsException(e);
            }
        }
    }

    private static void updateFile(String oldVersion, String newVersion, List<NulsVersion> versionList, URL libsUrl) throws NulsException, IOException {
        String jsonStr = null;
        try {
            jsonStr = new String(HttpDownloadUtils.download(VERDION_JSON_URL), NulsConfig.DEFAULT_ENCODING);
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(ErrorCode.FAILED, "Download version json faild!");
        }
        Map<String, Object> map = null;
        try {
            map = JSONUtils.json2map(jsonStr);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(ErrorCode.FAILED, "Parse version json faild!");
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
        URL rootUrl = VersionManager.class.getResource("");
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
                    throw new NulsException(ErrorCode.FAILED, "move the file fiald:" + key);
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
                    throw new NulsException(ErrorCode.FAILED, "move the file fiald:" + file.getPath());
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
            throw new NulsException(ErrorCode.FAILED, "move the file fiald:" + str.toString());
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
