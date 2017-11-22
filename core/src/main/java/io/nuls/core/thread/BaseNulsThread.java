package io.nuls.core.thread;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.manager.ModuleManager;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.log.Log;

import java.util.Date;


public abstract class BaseNulsThread implements Runnable{

    public enum STATUS {
        RUNING,
        HUNGUP,
    }
    private String name;
    private Thread realThread;
    private STATUS status;
    private BaseNulsModule module = null;
    private Date starttime;

    public BaseNulsThread(BaseNulsModule module, String name) {
        super();
        if(null==module){
            throw new NulsRuntimeException(ErrorCode.THREAD_MODULE_CANNOT_NULL);
        }
        this.setName(name);
        starttime = new Date();
        this.module = module;
        ModuleManager.getInstance().regThread(name,this);
        Log.debug(this.getClass().getName() + " start runing");
    }


    public String getStartTimeStr() {
        return DateUtil.convertDate(this.starttime);
    }


    public void setStatus(STATUS status) {
        this.status = status;
    }

    public STATUS getStatus() {
        return this.status;
    }

    public BaseNulsModule getModule() {
        return module;
    }

    public String getInfo(){
        StringBuilder str = new StringBuilder();
        str.append("threadName:");
        str.append(this.getName());
        str.append(",startTime:");
        str.append(getStartTimeStr());
        str.append(",status:");
        str.append(getStatus());
        str.append("\n");
        return str.toString();
    }

    public synchronized void start() {
        if(null==realThread){
            realThread = new Thread(this);
            realThread.start();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
