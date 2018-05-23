package io.nuls.message.bus.processor.manager;

import com.lmax.disruptor.WorkHandler;
import io.nuls.core.tools.disruptor.DisruptorData;
import io.nuls.core.tools.disruptor.DisruptorUtil;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.module.service.ModuleService;
import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.message.bus.constant.MessageBusErrorCode;
import io.nuls.message.bus.handler.intf.NulsMessageHandler;
import io.nuls.message.bus.module.MessageBusModuleBootstrap;
import io.nuls.message.bus.processor.MessageCheckingProcessor;
import io.nuls.message.bus.processor.thread.MessageDispatchThread;
import io.nuls.message.bus.processor.thread.NulsMessageCall;
import io.nuls.protocol.message.SmallBlockMessage;
import io.nuls.protocol.message.TransactionMessage;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.model.SmallBlock;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * 消息处理的管理器
 * Message processing manager.
 *
 * @author: Charlie
 * @date: 2018/5/6
 */
public class ProcessorManager<M extends BaseMessage, H extends NulsMessageHandler<? extends BaseMessage>> {

    private static final ProcessorManager INSTANCE = new ProcessorManager();

    private final Map<String, H> handlerMap = new HashMap<>();
    private final Map<Class, Set<String>> messageHandlerMapping = new HashMap<>();
    private DisruptorUtil<DisruptorData<ProcessData<M>>> disruptorService = DisruptorUtil.getInstance();
    private ExecutorService pool;
    private ExecutorService txHandlerPool;
    private ExecutorService blockHandlerpool;
    private String disruptorName = MessageBusConstant.DISRUPTOR_NAME;

    public static ProcessorManager getInstance() {
        return INSTANCE;
    }

    private ProcessorManager() {
    }

    public final void init(boolean messageChecking) {

        pool = TaskManager.createThreadPool(MessageBusConstant.THREAD_COUNT, 0,
                new NulsThreadFactory(MessageBusConstant.MODULE_ID_MESSAGE_BUS, MessageBusConstant.THREAD_POOL_NAME));

        txHandlerPool = TaskManager.createThreadPool(1, 0,
                new NulsThreadFactory(MessageBusConstant.MODULE_ID_MESSAGE_BUS, "nuls-tx-process-dispatcher"));

        blockHandlerpool = TaskManager.createThreadPool(1, 0,
                new NulsThreadFactory(MessageBusConstant.MODULE_ID_MESSAGE_BUS, "nuls-block-process-dispatcher"));

        NulsThreadFactory nulsThreadFactory = new NulsThreadFactory(ModuleService.getInstance().getModuleId(MessageBusModuleBootstrap.class), disruptorName);
        disruptorService.createDisruptor(disruptorName, MessageBusConstant.DEFAULT_RING_BUFFER_SIZE, nulsThreadFactory);

        List<MessageDispatchThread> handlerList = new ArrayList<>();
        for (int i = 0; i < MessageBusConstant.THREAD_COUNT; i++) {
            MessageDispatchThread handler = new MessageDispatchThread(this);
            handlerList.add(handler);
        }
        WorkHandler[] arrayHandler = handlerList.toArray(new MessageDispatchThread[handlerList.size()]);
        disruptorService.handleEventWith(disruptorName, new MessageCheckingProcessor()).thenHandleEventsWithWorkerPool(arrayHandler);
//        disruptorService.handleEventsWithWorkerPool(disruptorName,arrayHandler);

        disruptorService.start(disruptorName);
    }


    public void shutdown() {
        disruptorService.shutdown(disruptorName);
    }

    public void offer(ProcessData<M> data) {
        disruptorService.offer(disruptorName, data);
    }

    public String registerMessageHandler(String handlerId, Class<M> messageClass, H handler) {
        AssertUtil.canNotEmpty(messageClass, "registerMessageHandler faild");
        AssertUtil.canNotEmpty(handler, "registerMessageHandler faild");
        if (StringUtils.isBlank(handlerId)) {
            handlerId = StringUtils.getNewUUID();
        }
        handlerMap.put(handlerId, handler);
        cacheHandlerMapping(messageClass, handlerId);
        return handlerId;
    }

    private void cacheHandlerMapping(Class<M> messageClass, String handlerId) {

        Set<String> ids = messageHandlerMapping.get(messageClass);
        if (null == ids) {
            ids = new HashSet<>();
        }
        ids.add(handlerId);
        messageHandlerMapping.put(messageClass, ids);
    }

    public void removeMessageHandler(String handlerId) {
        handlerMap.remove(handlerId);
    }

    private Set<NulsMessageHandler> getHandlerList(Class<M> clazz) {
        Set<String> ids = messageHandlerMapping.get(clazz);
        Set<NulsMessageHandler> set = new HashSet<>();
        do {
            if (null == ids || ids.isEmpty()) {
                break;
            }
            for (String id : ids) {
                if (StringUtils.isBlank(id)) {
                    continue;
                }
                NulsMessageHandler handler = handlerMap.get(id);
                if (null == handler) {
                    continue;
                }
                set.add(handler);
            }
        } while (false);
        if (!clazz.equals(BaseMessage.class)) {
            set.addAll(getHandlerList((Class<M>) clazz.getSuperclass()));
        }
        return set;
    }


    public void executeHandlers(ProcessData<M> data) throws InterruptedException {
        if (null == data) {
            throw new NulsRuntimeException(MessageBusErrorCode.FAILED, "execute message message faild,the message is null!");
        }
        BaseMessage message = data.getData();

        Set<NulsMessageHandler> handlerSet = this.getHandlerList((Class<M>) data.getData().getClass());

        if(message instanceof TransactionMessage) {
            for (NulsMessageHandler handler : handlerSet) {
                txHandlerPool.execute(new NulsMessageCall(data, handler));
            }
        } else if(message instanceof SmallBlockMessage) {
            for (NulsMessageHandler handler : handlerSet) {
                blockHandlerpool.execute(new NulsMessageCall(data, handler));
            }
        } else {
            for (NulsMessageHandler handler : handlerSet) {
                pool.execute(new NulsMessageCall(data, handler));
            }
        }
    }
}
