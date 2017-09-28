package io.nuls.task;

import io.nuls.exception.InchainException;
import io.nuls.util.date.DateUtil;
import io.nuls.util.log.Log;

import java.util.Date;


public class InchainThread extends Thread {

    public enum STATUS {
        RUNING,
        HUNGUP,
    }

    private STATUS status;
    private InchainModule module = null;
    private Date starttime;

    public InchainThread(InchainModule module) {
        this(module,"noName");
    }

    public InchainThread(InchainModule module, String name) {
        super();
        if(null==module){
            throw new InchainException("thread must knows the module");
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

    public InchainModule getModule() {
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
