package io.nuls.core.constant;

/**
 * @author Niels
 * @date 2017/9/27
 */
public enum ModuleStatusEnum {

    NOT_FOUND,
    UNINITED,
    INITED,
    INITING,
    STARTING,
    RUNNING,
    STOPED,
    STOPPING,
    DESTROYED,
    DESTROYING,
    EXCEPTION,;

    @Override
    public String toString() {
        return name();
    }
}
