package io.nuls.mq.intf;

/**
 * Created by Niels on 2017/10/10.
 * nuls.io
 */
public final class DisruptorEvent<T> {

    private String name;

    private T data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
