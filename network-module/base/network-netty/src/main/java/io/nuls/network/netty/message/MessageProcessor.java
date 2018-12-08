package io.nuls.network.netty.message;

import io.netty.buffer.ByteBuf;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.Node;
import io.nuls.network.netty.message.filter.MessageFilterChain;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.message.base.MessageHeader;

import java.util.ArrayList;
import java.util.List;

public class MessageProcessor {

    private final static MessageProcessor INSTANCE = new MessageProcessor();

    private MessageBusService messageBusService;
    private NetworkMessageHandlerPool networkMessageHandlerPool = new NetworkMessageHandlerPool();

    private MessageProcessor() {
    }

    public static MessageProcessor getInstance() {
        return INSTANCE;
    }

    public void processor(ByteBuf buffer, Node node) throws NulsException {
        List<BaseMessage> messageList = analysisMessage(buffer);
        handlerMessage(messageList, node);
    }

    private List<BaseMessage> analysisMessage(ByteBuf buffer) throws NulsException {
        List<BaseMessage> messageList;
        try {
            messageList = new ArrayList<>();
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
            while (!byteBuffer.isFinished()) {
                MessageHeader header = byteBuffer.readNulsData(new MessageHeader());
                byteBuffer.setCursor(byteBuffer.getCursor() - header.size());
                BaseMessage message = getMessageBusService().getMessageInstance(header.getModuleId(), header.getMsgType()).getData();
                message = byteBuffer.readNulsData(message);
                messageList.add(message);
            }

        } catch (Exception e) {
            throw new NulsException(KernelErrorCode.DATA_ERROR, e);
        } finally {
            buffer.clear();
        }
        return messageList;
    }

    private void handlerMessage(List<BaseMessage> messageList, Node node) {
        for (BaseMessage message : messageList) {
            if (MessageFilterChain.getInstance().doFilter(message)) {
                MessageHeader header = message.getHeader();

                if (node.getMagicNumber() == 0L) {
                    node.setMagicNumber(header.getMagicNumber());
                }

                handler(message, node);
            } else {
                node.getChannel().close();
                if(node.getDisconnectListener() != null) {
                    node.getDisconnectListener().action();
                }
            }
        }
    }

    private void handler(BaseMessage message, Node node) {
        if (message == null) {
            return;
        }
        if (isNetworkMessage(message)) {
            networkMessageHandlerPool.execute(message, node);
        } else {
            messageBusService.receiveMessage(message, node);
        }
    }


    private boolean isNetworkMessage(BaseMessage message) {
        return message.getHeader().getModuleId() == NetworkConstant.NETWORK_MODULE_ID;
    }

    public MessageBusService getMessageBusService() {
        if (messageBusService == null) {
            messageBusService = NulsContext.getServiceBean(MessageBusService.class);
        }
        return messageBusService;
    }

}
