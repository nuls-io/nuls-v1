package io.nuls.event.bus.module.impl;

import com.lmax.disruptor.WorkHandler;
import io.nuls.core.module.NulsModule;
import io.nuls.event.bus.constant.EventBusConstant;
import io.nuls.event.bus.module.intf.EventBusModule;
import io.nuls.event.bus.processor.manager.ProcessorManager;
import io.nuls.event.bus.processor.service.impl.ProcessorServiceImpl;
import io.nuls.event.bus.processor.service.intf.ProcessorService;
import io.nuls.event.bus.utils.disruptor.DisruptorEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niels on 2017/11/6.
 * nuls.io
 */
public class EventBusModuleImpl extends EventBusModule {

    public EventBusModuleImpl() {
        super();
    }

    @Override
    public void start() {
        ProcessorManager.init();
        this.registerService(ProcessorServiceImpl.getInstance());
    }

    @Override
    public void shutdown() {
        ProcessorManager.shutdown();
    }

    @Override
    public void destroy() {
        ProcessorManager.shutdown();
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public int getVersion() {
        return EventBusConstant.EVENT_BUS_MODULE_VERSION;
    }

}
