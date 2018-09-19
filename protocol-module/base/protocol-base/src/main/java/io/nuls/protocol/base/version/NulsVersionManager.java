package io.nuls.protocol.base.version;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.base.utils.xml.XmlLoader;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.storage.po.ProtocolInfoPo;
import io.nuls.protocol.storage.po.ProtocolTempInfoPo;
import io.nuls.protocol.storage.service.VersionManagerStorageService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NulsVersionManager {

    /**
     * 存放所有配置文件里对应的消息
     */
    private static Map<String, Class<? extends BaseMessage>> messageProtocolMap = new ConcurrentHashMap<>();
    /**
     * 存放所有配置文件里对应的交易
     */
    private static Map<String, Class<? extends Transaction>> txProtocolMap = new ConcurrentHashMap<>();
    /**
     * 存放所有钱包版本的协议容器
     */
    private static Map<Integer, ProtocolContainer> containerMap = new ConcurrentHashMap<>();

    private static VersionManagerStorageService versionManagerStorageService;

//    public static void test() {
//        for (Map.Entry<String, Class<? extends Transaction>> entry : txProtocolMap.entrySet()) {
//            System.out.println(entry.getValue());
//        }
//        System.out.println();
//        for (Map.Entry<String, Class<? extends BaseMessage>> entry : messageProtocolMap.entrySet()) {
//            System.out.println(entry.getValue());
//        }
//        for (Map.Entry<Integer, ProtocolContainer> entry : containerMap.entrySet()) {
//            System.out.println("----- ProtocolContainer -----");
//            try {
//                System.out.println(JSONUtils.obj2PrettyJson(entry));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            System.out.println("-----------------------------");
//        }
//    }

    public static void init() throws Exception {
        loadConfig();
        VersionManagerStorageService vmss = NulsContext.getServiceBean(VersionManagerStorageService.class);
        Integer mainVersion = vmss.getMainVersion();
        if (mainVersion != null) {
            NulsContext.MAIN_NET_VERSION = mainVersion;
        }
        NulsContext.CHANGE_HASH_SERIALIZE_HEIGHT = vmss.getChangeTxHashBlockHeight();
    }

    /***
     * 读取数据库里已经保存的版本升级信息
     */
    public static void loadVersion() {
        //获取当前已存储的主网版本信息
        VersionManagerStorageService vmss = NulsContext.getServiceBean(VersionManagerStorageService.class);
        checkHasLaterVersion();
        //从数据库获取各个版本的升级信息，赋值到对应的协议容器里
        for (ProtocolContainer protocolContainer : containerMap.values()) {
            ProtocolInfoPo protocolInfoPo = vmss.getProtocolInfoPo(protocolContainer.getVersion());
            if (protocolContainer.getVersion() == 1) {
                protocolContainer.setStatus(ProtocolContainer.VALID);
                protocolContainer.setEffectiveHeight(0L);
                protocolContainer.setCurrentDelay(0L);
                protocolContainer.setCurrentPercent(100);
                protocolContainer.setRoundIndex(0);
            } else if (protocolInfoPo != null) {
                protocolContainer.setCurrentDelay(protocolInfoPo.getCurrentDelay());
                protocolContainer.setStatus(protocolInfoPo.getStatus());
                protocolContainer.setAddressSet(protocolInfoPo.getAddressSet());
                protocolContainer.setEffectiveHeight(protocolInfoPo.getEffectiveHeight());
                protocolContainer.setCurrentPercent(protocolInfoPo.getCurrentPercent());
                protocolContainer.setRoundIndex(protocolInfoPo.getRoundIndex());
            }
            //如果有对应版本的临时协议数据时，将临时数据赋值到container上，然后删除临时数据
            ProtocolTempInfoPo tempInfoPo = getVersionManagerStorageService().getProtocolTempInfoPo(protocolContainer.getProtocolKey());
            if (tempInfoPo != null) {
                protocolContainer.setRoundIndex(tempInfoPo.getRoundIndex());
                protocolContainer.setCurrentDelay(tempInfoPo.getCurrentDelay());
                protocolContainer.setAddressSet(tempInfoPo.getAddressSet());
                protocolContainer.setStatus(tempInfoPo.getStatus());
                protocolContainer.setEffectiveHeight(tempInfoPo.getEffectiveHeight());
                protocolContainer.setCurrentPercent(tempInfoPo.getCurrentPercent());
                protocolInfoPo = new ProtocolInfoPo(tempInfoPo);
                getVersionManagerStorageService().saveProtocolInfoPo(protocolInfoPo);
                getVersionManagerStorageService().removeProtocolTempInfo(tempInfoPo.getProtocolKey());
            }

            //赋值完成后，检查是否有超过当前主网版本的协议已生效，有则修改当前主网协议版本号
            if (protocolContainer.getStatus() == ProtocolContainer.VALID) {
                if (NulsContext.MAIN_NET_VERSION < protocolContainer.getVersion()) {
                    NulsContext.MAIN_NET_VERSION = protocolContainer.getVersion();
                    getVersionManagerStorageService().saveMainVersion(NulsContext.MAIN_NET_VERSION);
                    //如果是版本号为2的协议生效后，记录一下生效区块的高度，从当前高度后的交易，序列化hash方法需要改变
                    if (protocolContainer.getVersion() == 2) {
                        getVersionManagerStorageService().saveChangeTxHashBlockHeight(protocolContainer.getEffectiveHeight());
                        NulsContext.CHANGE_HASH_SERIALIZE_HEIGHT = protocolContainer.getEffectiveHeight();
                    }
                }
            }
        }
    }

    /**
     * 读取配置文件信息，生成协议容器
     *
     * @throws Exception
     */
    public static void loadConfig() throws Exception {
        XmlLoader.loadXml(NulsConstant.NULS_VERSION_XML, new NulsVersionHandler());
    }

    /**
     * 检查是否有更高版本，如果有切生效的话，需要强制升级
     */
    private static void checkHasLaterVersion() {
        Map<String, ProtocolTempInfoPo> protocolTempMap = getVersionManagerStorageService().getProtocolTempMap();
        for (ProtocolTempInfoPo tempInfoPo : protocolTempMap.values()) {
            if (tempInfoPo.getVersion() > NulsContext.CURRENT_PROTOCOL_VERSION) {
                if (tempInfoPo.getStatus() == ProtocolContainer.VALID) {
                    //linux系统直接停止运行
                    //其他有桌面程序的系统检查到NulsContext.mastUpGrade = true时，在页面上提示需强制升级
                    if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("LINUX") != -1) {
                        Log.error("The version is too low to upgrade");
                        NulsContext.getInstance().exit(0);
                        return;
                    } else {
                        NulsContext.mastUpGrade = true;
                        return;
                    }
                }
            }
        }
    }

    /**
     * 获取当前版本协议的容器
     *
     * @return ProtocolContainer 协议容器
     */
    public static ProtocolContainer getCurrentProtocolContainer() {
        return containerMap.get(NulsContext.CURRENT_PROTOCOL_VERSION);
    }

    /**
     * 获取主网版本协议的容器
     *
     * @return ProtocolContainer 协议容器
     */
    public static ProtocolContainer getMainProtocolContainer() {
        return containerMap.get(NulsContext.MAIN_NET_VERSION);
    }

    public static Map<Integer, ProtocolContainer> getAllProtocolContainers() {
        return containerMap;
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
        return NulsContext.MAIN_NET_VERSION;
    }

    public static Integer getCurrentVersion() {
        return NulsContext.CURRENT_PROTOCOL_VERSION;
    }

    private static VersionManagerStorageService getVersionManagerStorageService() {
        if (versionManagerStorageService == null) {
            versionManagerStorageService = NulsContext.getServiceBean(VersionManagerStorageService.class);
        }
        return versionManagerStorageService;
    }

}
