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
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.base.utils.xml.XmlLoader;
import io.nuls.protocol.message.base.BaseMessage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/8/15
 */
public class NulsVersionHandler extends DefaultHandler {

    /**
     * 所有的协议
     */
    private static final String INCLUD = "include";
    /**
     * 协议标签
     */
    private static final String PROTOCOL = "protocol";
    /**
     *  协议class
     */
    private static final String INCLUD_SRC = "src";
    /**
     *  协议版本
     */
    private static final String PROTOCOL_VERSION = "version";
    /**
     *  覆盖率
     */
    private static final String PROTOCOL_PERCENT = "percent";
    /**
     *  满足覆盖率后的延迟块数
     */
    private static final String PROTOCOL_DELAY = "delay";
    /**
     *  该协议的区块
     */
    private static final String PROTOCOL_BLOCK = "block";
    /**
     *  该协议所继承的上一个版本的协议，继承具体的协议和区块
     */
    private static final String PROTOCOL_EXTEND = "extend";
    /**
     *  生效的Tx协议
     */
    private static final String PROTOCOL_TX = "tx";
    /**
     *  失效的Tx协议
     */
    private static final String PROTOCOL_TX_DISCARD = "tx-discard";
    /**
     *  生效的msg协议
     */
    private static final String PROTOCOL_MSG = "message";
    /**
     *  失效的msg协议
     */
    private static final String PROTOCOL_MSG_DISCARD = "message-discard";
    /**
     *  协议的id
     */
    private static final String REF = "ref";

    /**
     * 解析到一个Protocol时暂存的对象，当解析到Protocol结束时继续处理
     */
    private ProtocolContainer protocolContainer;

    /**
     * 解析到一个Protocol时暂存的继承协议的version值，当解析到Protocol结束时继续处理
     */
    private Integer extendTS = null;

    private Map<Integer, Class<? extends Transaction>> discardsTx = null;
    private Map<String, Class<? extends BaseMessage>> discardsMsg = null;


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        //读取模块配置的协议
        if(INCLUD.equals(qName)){
            String xmlName = attributes.getValue(INCLUD_SRC);
            XmlLoader.loadXml(xmlName, new ProtocolVersionHandler());
        }

        //读取版本协议
        if(PROTOCOL.equals(qName)) {
            protocolContainer = new ProtocolContainer();
            String version = attributes.getValue(PROTOCOL_VERSION);
            String percent = attributes.getValue(PROTOCOL_PERCENT);
            String delay = attributes.getValue(PROTOCOL_DELAY);
            if(!StringUtils.isNumeric(version) || !StringUtils.isNumeric(percent) ||!StringUtils.isNumeric(delay)){
                Log.error(KernelErrorCode.CONFIG_ERROR.getMsg());
                throw new SAXException();
            }
            protocolContainer.setVersion(Integer.parseInt(version.trim()));
            protocolContainer.setPercent(Integer.parseInt(percent.trim()));
            protocolContainer.setDelay(Integer.parseInt(delay.trim()));

            String extend = attributes.getValue(PROTOCOL_EXTEND);
            if(StringUtils.isNotBlank(extend) && !StringUtils.isNumeric(percent)){
                Log.error(KernelErrorCode.CONFIG_ERROR.getMsg());
                throw new SAXException();
            }
            extendTS = StringUtils.isBlank(extend) ? null : Integer.parseInt(extend.trim());

            String block = attributes.getValue(PROTOCOL_BLOCK);

            if(StringUtils.isNotBlank(block)) {
                Class blockClass = null;
                try {
                    blockClass = Class.forName(block.trim());
                    protocolContainer.setBlockClass(blockClass);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new SAXException();
                }
            }else{
                if(null != extendTS){
                    ProtocolContainer parentPC = NulsVersionManager.getProtocolContainer(extendTS);
                    if(null == parentPC || null == parentPC.getBlockClass()){
                        throw new SAXException();
                    }
                    protocolContainer.setBlockClass(parentPC.getBlockClass());
                }else{
                    Log.error(KernelErrorCode.CONFIG_ERROR.getMsg());
                    throw new SAXException();
                }
            }

            discardsTx = new HashMap<>();
            discardsMsg = new HashMap<>();
        }

        //失效Tx协议
        if(PROTOCOL_TX_DISCARD.equals(qName)){
            String discard = attributes.getValue(REF);
            if(!NulsVersionManager.containsTxId(discard)){
                throw new SAXException(discard);
            }
            Class txCLass = NulsVersionManager.getTxProtocol(discard.trim());
            Transaction tx = null;
            try {
                tx = (Transaction) txCLass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            discardsTx.put(tx.getType(), txCLass);
        }

        //新生效Tx协议
        if(PROTOCOL_TX.equals(qName)){
            String txId = attributes.getValue(REF);
            if(!NulsVersionManager.containsTxId(txId)){
                throw new SAXException(txId);
            }
            Class txCLass = NulsVersionManager.getTxProtocol(txId);
            Transaction tx = null;
            try {
                tx = (Transaction) txCLass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            protocolContainer.putTransaction(tx.getType(), txCLass);
        }

        //失效的msg协议
        if(PROTOCOL_MSG_DISCARD.equals(qName)){
            String msgId = attributes.getValue(REF);
            if(!NulsVersionManager.containsMessageId(msgId)){
                throw new SAXException(msgId);
            }
            Class txCLass = NulsVersionManager.getMessageProtocol(msgId);
            BaseMessage msg = null;
            try {
                msg = (BaseMessage) txCLass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            String moduleId = String.valueOf(msg.getHeader().getModuleId());
            String type = String.valueOf(msg.getHeader().getMsgType());
            String key = moduleId + "-" + type;
            discardsMsg.put(key, txCLass);
        }

        //新生效的msg协议
        if(PROTOCOL_MSG.equals(qName)){
            String msgId = attributes.getValue(REF);
            if(!NulsVersionManager.containsMessageId(msgId)){
                throw new SAXException(msgId);
            }
            Class txCLass = NulsVersionManager.getMessageProtocol(msgId);
            BaseMessage msg = null;
            try {
                msg = (BaseMessage) txCLass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            String moduleId = String.valueOf(msg.getHeader().getModuleId());
            String type = String.valueOf(msg.getHeader().getMsgType());
            String key = moduleId + "-" + type;
            protocolContainer.putMessage(key, txCLass);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        //读完一个协议标签时整理生效失效的数据
        if(PROTOCOL.equals(qName)) {
            if(null != extendTS) {
                ProtocolContainer parentPC = NulsVersionManager.getProtocolContainer(extendTS);
                if(null == parentPC){
                    throw new SAXException();
                }

                Map<Integer, Class<? extends Transaction>> parentTxMap = parentPC.getTxMap();
                for (Map.Entry<Integer, Class<? extends Transaction>> entry : parentTxMap.entrySet()) {
                    //添加继承的Tx协议，并过滤掉已失效的
                    if(!discardsTx.containsKey(entry.getKey())){
                        protocolContainer.putTransaction(entry.getKey(), entry.getValue());
                    }
                }

                Map<String, Class<? extends BaseMessage>> messageMap = parentPC.getMessageMap();
                for(Map.Entry<String, Class<? extends BaseMessage>> entry : messageMap.entrySet()){
                    if(!discardsMsg.containsKey(entry.getKey())){
                        protocolContainer.putMessage(entry.getKey(), entry.getValue());
                    }
                }
            }
            //设置协议配置的最大版本号，为当前钱包的版本号
            if(protocolContainer.getVersion() > NulsContext.CURRENT_PROTOCOL_VERSION) {
                NulsContext.CURRENT_PROTOCOL_VERSION = protocolContainer.getVersion();
            }
            NulsVersionManager.putContainerMap(protocolContainer.getVersion(), protocolContainer);

            extendTS = null;
            discardsTx = null;
            discardsMsg = null;
            protocolContainer = null;
        }
    }

}
