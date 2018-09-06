package io.nuls.contract.vm;

public class RevertException extends RuntimeException {

    private String stackTraceMessage;

    public RevertException(String message, String stackTraceMessage) {
        super(message);
        this.stackTraceMessage = stackTraceMessage;
    }

    public String getStackTraceMessage() {
        return stackTraceMessage;
    }

}
