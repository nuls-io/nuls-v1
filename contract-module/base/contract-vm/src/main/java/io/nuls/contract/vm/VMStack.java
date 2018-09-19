package io.nuls.contract.vm;

import java.util.Stack;

public class VMStack extends Stack<Frame> {

    private final int maxSize;

    public VMStack(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public Frame push(Frame frame) {
        if (size() > maxSize) {
            frame.throwStackOverflowError();
        }
        return super.push(frame);
    }

    @Override
    public synchronized Frame pop() {
        return super.pop();
    }

}
