package io.nuls.protocol.base.version;


import io.nuls.core.tools.json.JSONUtils;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.base.utils.xml.NulsVersionHandler;
import io.nuls.protocol.base.utils.xml.XmlLoader;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.storage.po.UpgradeInfoPo;
import io.nuls.protocol.storage.service.VersionManagerStorageService;

import java.util.HashMap;
import java.util.Map;

public class NulsVersionManager {

    /**
     * 主网运行中的版本
     */
    private static volatile Integer mainVersion;

    /**
     * 本次升级的新版本
     */
    private static volatile Integer currentVersion;

    /**
     * 存放所有配置文件里对应的消息
     */
    private static Map<String, Class<? extends BaseMessage>> messageProtocolMap = new HashMap<>();

    /**
     * 存放所有配置文件里对应的交易
     */
    private static Map<String, Class<? extends Transaction>> txProtocolMap = new HashMap<>();


    /**
     * 存放所有版本的协议容器
     */
    private static Map<Integer, ProtocolContainer> containerMap = new HashMap<>();

    //TODO 开发调试方法
    public static void test() {
        for (Map.Entry<String, Class<? extends Transaction>> entry : txProtocolMap.entrySet()) {
            System.out.println(entry.getValue());
        }
        System.out.println();
        for (Map.Entry<String, Class<? extends BaseMessage>> entry : messageProtocolMap.entrySet()) {
            System.out.println(entry.getValue());
        }
        System.out.println();
        for (Map.Entry<Integer, ProtocolContainer> entry : containerMap.entrySet()) {
            System.out.println("----- ProtocolContainer -----");
            try {
                System.out.println(JSONUtils.obj2PrettyJson(entry));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("-----------------------------");
        }
    }


    public static void init() throws Exception {
        loadConfig();
        loadVersion();
    }

    private static void loadVersion() {
        VersionManagerStorageService vmss = NulsContext.getServiceBean(VersionManagerStorageService.class);
        mainVersion = vmss.getMainVersion();
        if (mainVersion == null) {
            mainVersion = 1;
        }
        currentVersion = NulsConstant.PROTOCOL_VERSION;

        //从数据库获取当前待升级新版本覆盖量
        for (Map.Entry<Integer, ProtocolContainer> entry : containerMap.entrySet()) {
            ProtocolContainer protocolContainer = entry.getValue();
            UpgradeInfoPo upgradeInfoPo = vmss.getUpgradeCount(protocolContainer.getVersion());
//            protocolContainer.setUpgradeCount(null == upgradeInfoPo ? 0 : upgradeInfoPo.getUpgradeCount());
        }
    }

    private static void loadConfig() throws Exception {
        XmlLoader.loadXml(NulsConstant.NULS_VERSION_XML, new NulsVersionHandler());
    }

    /**
     * 获取当前版本协议的容器
     *
     * @return ProtocolContainer 协议容器
     */
    public static ProtocolContainer getCurrentProtocolContainer() {
        return containerMap.get(currentVersion);
    }

    /**
     * 获取主网版本协议的容器
     *
     * @return ProtocolContainer 协议容器
     */
    public static ProtocolContainer getMainProtocolContainer() {
        return containerMap.get(mainVersion);
    }

    /**
     * 根据版本号获取协议的容器
     *
     * @param version 协议版本号
     * @return ProtocolContainer 协议容器
     */
    public static ProtocolContainer getProtocolContainer(int version) {
        return containerMap.get(version);
    }

    /**
     * 根据交易的协议id获取对应的交易class
     *
     * @param id 交易的协议id
     * @return Transaction 交易class
     */
    public static Class<? extends Transaction> getTxProtocol(String id) {
        return txProtocolMap.get(id);
    }

    /**
     * 根据消息的协议id获取对应的消息class
     *
     * @param id 消息的协议id
     * @return Message 消息class
     */
    public static Class<? extends BaseMessage> getMessageProtocol(String id) {
        return messageProtocolMap.get(id);
    }

    /**
     * 返回交易map中是否已含有该交易id
     *
     * @param id 交易id
     * @return boolean 结果
     */
    public static boolean containsTxId(String id) {
        return txProtocolMap.containsKey(id);
    }

    /**
     * 返回交易map中是否已含有该消息id
     *
     * @param id 消息id
     * @return boolean 结果
     */
    public static boolean containsMessageId(String id) {
        return messageProtocolMap.containsKey(id);
    }

    /**
     * 返回协议容器map中是否已含有该版本的容器
     *
     * @param version 容器版本
     * @return boolean 结果
     */
    public static boolean containsProtocolVersion(Integer version) {
        return containerMap.containsKey(version);
    }

    /**
     * 添加交易协议
     *
     * @param id      配置文件中交易协议id
     * @param txClass 交易class
     * @throws NulsException nuls异常
     */
    public static void putTxProtocol(String id, Class<? extends Transaction> txClass) {
        if (containsTxId(id) || null == txClass) {
            throw new RuntimeException();
        }
        txProtocolMap.put(id, txClass);
    }

    /**
     * 添加消息协议
     *
     * @param id           配置文件中消息协议id
     * @param messageClass
     * @throws NulsException
     */
    public static void putMessageProtocol(String id, Class<? extends BaseMessage> messageClass) {
        if (containsMessageId(id) || null == messageClass) {
            throw new RuntimeException();
        }
        messageProtocolMap.put(id, messageClass);
    }

    /**
     * 添加一个协议容器
     *
     * @param key               协议id
     * @param protocolContainer 容器实例
     */
    public static void putContainerMap(Integer key, ProtocolContainer protocolContainer) {
        if (null == key || null == protocolContainer) {
            throw new RuntimeException();
        }
        containerMap.put(key, protocolContainer);
    }

    public static Integer getMainVersion() {
        return mainVersion;
    }

    public static void setMainVersion(int mainVersion) {
        NulsVersionManager.mainVersion = mainVersion;
    }

    public static Integer getCurrentVersion() {
        return currentVersion;
    }


    public static void setCurrentVersion(int currentVersion) {
        NulsVersionManager.currentVersion = currentVersion;
    }
}
