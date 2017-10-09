package io.nuls.task;

import io.nuls.exception.NulsRuntimeException;
import io.nuls.util.date.DateUtil;
import io.nuls.util.log.Log;

import java.util.Date;


public class NulsThread extends Thread {

    public enum STATUS {
        RUNING,
        HUNGUP,
    }

    private STATUS status;
    private NulsModule module = null;
    private Date starttime;

    public NulsThread(NulsModule module) {
        this(module,"noName");
    }

    public NulsThread(NulsModule module, String name) {
        super();
        if(null==module){
            throw new NulsRuntimeException("thread must knows the module");
        }
        this.setName(name);
        starttime = new Date();
        this.module = module;
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

    public NulsModule getModule() {
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

}
