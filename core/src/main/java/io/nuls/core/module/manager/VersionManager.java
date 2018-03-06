package io.nuls.core.module.manager;

import io.nuls.core.chain.intf.NulsVersion;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.HttpDownloadUtils;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.VersionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
        //todo
        List<NulsVersion> versionList = NulsContext.getServiceBeanList(NulsVersion.class);
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
        NulsContext.VERSION = localVersion;
        String jsonStr = null;
        try {
            jsonStr = new String(HttpDownloadUtils.download(HIGHEST_VERDION_FILE_URL), NulsContext.DEFAULT_ENCODING);
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
        //todo 验证签名
        NulsContext.NEWEST_VERSION = version;
        //todo 自动更新开关
        boolean autoUpdate = true;
        if (VersionUtils.equalsWith(version, localVersion)) {
            return;
        } else if (autoUpdate) {
            updateFile(localVersion, version, versionList);
        }
    }

    private static void updateFile(String oldVersion, String newVersion, List<NulsVersion> versionList) throws NulsException {
        //todo
        // 获取详细版本描述文件
        String jsonStr = null;
        try {
            jsonStr = new String(HttpDownloadUtils.download(VERDION_JSON_URL), NulsContext.DEFAULT_ENCODING);
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

        try {
            Enumeration<URL> urls = VersionUtils.class.getClassLoader().getResources("libs");
            if (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                fillJarMap(url, jarMap);
            }
        } catch (IOException e) {
            Log.error(e);
        }
        // jarMap is empty means it's development environment
        if (jarMap.isEmpty()) {
            return;
        }
//      下载本次新增文件到特定目录，当存在验证不通过的jar文件，记录日志并停止更新
        List<Map<String, Object>> libList = (List<Map<String, Object>>) map.get("libs");
        //check the temp folder exist,and delete
        URL rootUrl = VersionManager.class.getResource("");
        File tempFolder = new File(rootUrl.getPath() + "/temp");
        if (tempFolder.exists()) {
            deleteFile(tempFolder);
        }
        for (Map<String, Object> lib : libList) {
            String file = (String) lib.get("file");
            String libSign = (String) lib.get("sign");
            if (jarMap.get(file) == null) {
                downloadLib(file, sign);
            }
        }
//        备份本地应删除的文件、并删除
//        将下载完的文件移动到正确位置
//        重启动

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

    private static void downloadLib(String file, String sign) throws NulsException {
        // todo auto-generated method stub(niels)

    }

}
