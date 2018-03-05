package io.nuls.core.module.manager;

import io.nuls.core.chain.intf.NulsVersion;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.io.HttpDownloadUtils;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.utils.str.VersionUtils;

import java.io.IOException;
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


    public static void start() {
        //todo
        NulsContext.NEWEST_VERSION = "1.0.0";
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
            return;
        }
        Map<String, Object> map = null;
        try {
            map = JSONUtils.json2map(jsonStr);
        } catch (Exception e) {
            Log.error(e);
        }
        map.get("");
        //todo
        //获取网络上/下载中心最新版本号
        // 验证版本号是否合法
        // 比较版本号是否需要本地升级，不需要则返回
        //获取详细版本描述文件
//            下载本次新增文件到特定目录，当存在验证不通过的jar文件，记录日志并停止更新
//            备份本地应删除的文件、并删除
//            将下载完的文件移动到正确位置
//        重启动
    }

    public static void check() {

    }


}
