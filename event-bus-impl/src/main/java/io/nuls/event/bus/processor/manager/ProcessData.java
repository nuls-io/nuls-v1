package io.nuls.event.bus.processor.manager;

import io.nuls.core.event.BaseEvent;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class ProcessData<E extends BaseEvent> {

    private final E data;

    private String peerId;

    public ProcessData(E data){
        this.data = data;
    }

    public ProcessData(E data, String peerId) {
        this.data = data;
        this.peerId = peerId;
    }

    public E getData() {
        return data;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }
}
