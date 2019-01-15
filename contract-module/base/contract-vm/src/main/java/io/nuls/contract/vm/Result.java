/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
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
