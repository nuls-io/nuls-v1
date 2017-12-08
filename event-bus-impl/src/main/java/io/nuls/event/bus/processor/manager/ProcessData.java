package io.nuls.event.bus.processor.manager;

import io.nuls.core.event.BaseNulsEvent;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class ProcessData<E extends BaseNulsEvent> {

    private final E event;

    private String peerId;

    public ProcessData(E event){
        this.event = event;
    }

    public ProcessData(E event, String peerId) {
        this.event = event;
        this.peerId = peerId;
    }

    public E getEvent() {
        return event;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }
}
