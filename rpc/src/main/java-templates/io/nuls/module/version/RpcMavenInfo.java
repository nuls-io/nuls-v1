package io.nuls.module.version;

import io.nuls.core.chain.intf.NulsVersion;
import io.nuls.core.utils.spring.lite.annotation.MavenInfo;

/**
 * @author: Niels Wang
 * @date: 2018/3/1
 */
@MavenInfo
public class RpcMavenInfo implements NulsVersion {

    public static final String VERSION = "${project.version}";
    public static final String GROUP_ID = "${project.groupId}";
    public static final String ARTIFACT_ID = "${project.artifactId}";

    public String getVersion() {
        return VERSION;
    }

    public String getArtifactId() {
        return ARTIFACT_ID;
    }

    public String getGroupId(){
        return GROUP_ID;
    }


}
