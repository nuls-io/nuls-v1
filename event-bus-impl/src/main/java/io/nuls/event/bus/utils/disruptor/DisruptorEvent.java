package io.nuls.event.bus.utils.disruptor;

/**
 *
 * @author Niels
 * @date 2017/11/6
 */
public class DisruptorEvent<T> {

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
