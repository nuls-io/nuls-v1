package io.nuls.core.constant;

/**
 * @author Niels
 */
public enum ModuleStatusEnum {

    NOT_FOUND,
    UNINITIALIZED,
    /**
     * initialized
     */
    INITIALIZED,
    INITIALIZING,
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
