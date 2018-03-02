package io.nuls.core.module.manager;

import io.nuls.core.chain.intf.NulsVersion;
import io.nuls.core.context.NulsContext;

import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/3/1
 */
public class VersionManager {

    public static void start() {
        //todo
        NulsContext.NEWEST_VERSION = "1.0.0";
        NulsContext.VERSION = "1.0.0";
        List<NulsVersion> versionList = NulsContext.getServiceBeanList(NulsVersion.class);
        //todo
        //获取网络上/下载中心最新版本号
            // 验证版本号是否合法
            // 比较版本号是否需要本地升级，不需要则返回
        //获取详细版本描述文件
//            备份本地应删除的文件、并删除
//            下载本次新增文件
//        重启动
    }

    public static void check(){

    }


}
