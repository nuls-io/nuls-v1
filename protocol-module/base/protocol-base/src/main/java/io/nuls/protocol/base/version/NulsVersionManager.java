/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import java.util.List;
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

    private static Map<String, ProtocolTempInfoPo> tempContainerMap = new ConcurrentHashMap<>();

    private static VersionManagerStorageService versionManagerStorageService;

    private static Map<String, Integer> consensusVersionMap = new ConcurrentHashMap<>();

    public static void init() throws Exception {
        loadConfig();
        VersionManagerStorageService vmss = NulsContext.getServiceBean(VersionManagerStorageService.class);
        Integer mainVersion = vmss.getMainVersion();
        if (mainVersion != null) {
            NulsContext.MAIN_NET_VERSION = mainVersion;
        }
        NulsContext.CHANGE_HASH_SERIALIZE_HEIGHT = vmss.getChangeTxHashBlockHeight();
        ProtocolContainer container = getProtocolContainer(1);
        if (container != null) {
            container.setStatus(ProtocolContainer.VALID);
        }
    }

    public static void loadVersionByHeight(long versionHeight) {
        List<ProtocolInfoPo> infoPoList = getVersionManagerStorageService().getProtocolInfoList(versionHeight);
        //获取数据库已保存的协议信息
        if (infoPoList != null && !infoPoList.isEmpty()) {
            for (ProtocolInfoPo infoPo : infoPoList) {
                ProtocolContainer container = getProtocolContainer(infoPo.getVersion());
                copyProtocolFromInfoPo(container, infoPo);
            }
        }
        //获取数据库已保存的临时协议信息，如果发现临时协议信息在最新版本里能查询到就复制到最新版本信息里
        List<ProtocolTempInfoPo> tempInfoPoList = getVersionManagerStorageService().getProtocolTempInfoList(versionHeight);
        if (tempInfoPoList == null) {
            return;
        }
        for (int i = 0; i < tempInfoPoList.size(); i++) {
            ProtocolTempInfoPo tempInfoPo = tempInfoPoList.get(i);
            ProtocolContainer container = getProtocolContainer(tempInfoPo.getVersion());
            if (container != null) {
                copyProtocolFromTempInfoPo(container, tempInfoPo);
                //如果有协议升级了，要做升级相关处理
                if (container.getStatus() == ProtocolContainer.VALID && container.getVersion() > NulsContext.MAIN_NET_VERSION) {
                    NulsContext.MAIN_NET_VERSION = container.getVersion();
                    getVersionManagerStorageService().saveMainVersion(NulsContext.MAIN_NET_VERSION);
                    //如果是版本号为2的协议生效后，记录一下生效区块的高度，从当前高度后的交易，序列化hash方法需要改变
                    if (container.getVersion() == 2) {
                        getVersionManagerStorageService().saveChangeTxHashBlockHeight(container.getEffectiveHeight());
                        NulsContext.CHANGE_HASH_SERIALIZE_HEIGHT = container.getEffectiveHeight();
                    }
                }
                continue;
            }
            //如果有临时协议已经生效，说明当前版本不是最新版本，需要强制升级
            if (tempInfoPo.getStatus() == ProtocolContainer.VALID) {
                NulsContext.mastUpGrade = true;
            }
            tempContainerMap.put(tempInfoPo.getProtocolKey(), tempInfoPo);
        }
    }


    public static void copyProtocolFromInfoPo(ProtocolContainer container, ProtocolInfoPo infoPo) {
        container.setCurrentDelay(infoPo.getCurrentDelay());
        container.setCurrentPercent(infoPo.getCurrentPercent());
        container.setAddressSet(infoPo.getAddressSet());
        container.setStatus(infoPo.getStatus());
        container.setRoundIndex(infoPo.getRoundIndex());
        container.setEffectiveHeight(infoPo.getEffectiveHeight());
        container.setPrePercent(infoPo.getPrePercent());
    }

    public static void copyProtocolFromTempInfoPo(ProtocolContainer container, ProtocolTempInfoPo infoPo) {
        container.setCurrentDelay(infoPo.getCurrentDelay());
        container.setCurrentPercent(infoPo.getCurrentPercent());
        container.setAddressSet(infoPo.getAddressSet());
        container.setStatus(infoPo.getStatus());
        container.setRoundIndex(infoPo.getRoundIndex());
        container.setEffectiveHeight(infoPo.getEffectiveHeight());
        container.setPrePercent(infoPo.getPrePercent());
    }

    /**
     * 读取配置文件信息，生成协议容器
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
                        NulsContext.getInstance().exit(1);
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

    public static Map<String, ProtocolTempInfoPo> getTempProtocolContainers() {
        return tempContainerMap;
    }

    public static void setTempProtocolContainers(Map<String, ProtocolTempInfoPo> tempContainerMap) {
        NulsVersionManager.tempContainerMap = tempContainerMap;
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

    public static ProtocolContainer getProtocolContainer(int version, int percent, long delay) {
        ProtocolContainer container = containerMap.get(version);
        if (container != null && container.getPercent() == percent && container.getDelay() == delay) {
            return container;
        }
        return null;
    }

    public static ProtocolTempInfoPo getTempProtocolContainer(String key) {
        return tempContainerMap.get(key);
    }

    public static void addTempProtocolContainer(ProtocolTempInfoPo tempInfoPo) {
        tempContainerMap.put(tempInfoPo.getProtocolKey(), tempInfoPo);
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
     * @param id 配置文件中消息协议id
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

    public static Map<String, Integer> getConsensusVersionMap() {
        return consensusVersionMap;
    }

    public static void setConsensusVersionMap(Map<String, Integer> consensusVersionMap) {
        NulsVersionManager.consensusVersionMap = consensusVersionMap;
    }

}
