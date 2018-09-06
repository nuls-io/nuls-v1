package io.nuls.contract.vm;

public class ErrorException extends RuntimeException {

    private long gasUsed;

    private String stackTraceMessage;

    public ErrorException(String message, long gasUsed, String stackTraceMessage) {
        super(message);
        this.gasUsed = gasUsed;
        this.stackTraceMessage = stackTraceMessage;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public String getStackTraceMessage() {
        return stackTraceMessage;
    }

}
