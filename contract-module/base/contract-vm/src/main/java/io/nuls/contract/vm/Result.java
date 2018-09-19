package io.nuls.contract.vm;

import io.nuls.contract.vm.code.VariableType;

public class Result {

    private VariableType variableType;

    private Object value;

    private boolean ended;

    private boolean exception;

    private boolean error;

    public Result() {
    }

    public Result(VariableType variableType) {
        this.variableType = variableType;
    }

    public void value(Object value) {
        this.value = value;
        this.ended = true;
    }

    public void exception(ObjectRef exception) {
        //java.lang.Exception
        this.value(exception);
        this.variableType = exception.getVariableType();
        this.exception = true;
    }

    public void error(ObjectRef error) {
        //java.lang.Error
        this.value(error);
        this.variableType = error.getVariableType();
        this.error = true;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public Object getValue() {
        return value;
    }

    public boolean isEnded() {
        return ended;
    }

    public boolean isException() {
        return exception;
    }

    public boolean isError() {
        return error;
    }

    @Override
    public String toString() {
        return "Result{" +
                "variableType=" + variableType +
                ", value=" + value +
                ", ended=" + ended +
                ", exception=" + exception +
                ", error=" + error +
                '}';
    }

}
