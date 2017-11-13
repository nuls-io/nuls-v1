package io.nuls.core.constant;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
public enum ModuleStatusEnum {

    //UNINITED,INITED,
    UNSTARTED, STARTING, RUNNING, STOPED, STOPPING, DESTROYED, DESTROYING, EXCEPTION;

    public String toString() {
        return name();
    }
}
