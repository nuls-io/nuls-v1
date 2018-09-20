/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.contract.vm.program;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ProgramResult {

    private long gasUsed;

    private String result;

    private boolean revert;

    private boolean error;

    private String errorMessage;

    private String stackTrace;

    private BigInteger balance;

    private BigInteger nonce;

    private List<ProgramTransfer> transfers = new ArrayList<>();

    private List<String> events = new ArrayList<>();

    public ProgramResult revert(String errorMessage) {
        this.revert = true;
        this.errorMessage = errorMessage;
        return this;
    }

    public ProgramResult error(String errorMessage) {
        this.error = true;
        this.errorMessage = errorMessage;
        return this;
    }

    public void view() {
        this.transfers = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public ProgramResult() {
    }

    public boolean isSuccess() {
        return !error && !revert;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isRevert() {
        return revert;
    }

    public void setRevert(boolean revert) {
        this.revert = revert;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public List<ProgramTransfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<ProgramTransfer> transfers) {
        this.transfers = transfers;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProgramResult that = (ProgramResult) o;

        if (gasUsed != that.gasUsed) {
            return false;
        }
        if (revert != that.revert) {
            return false;
        }
        if (error != that.error) {
            return false;
        }
        if (result != null ? !result.equals(that.result) : that.result != null) {
            return false;
        }
        if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null) {
            return false;
        }
        if (stackTrace != null ? !stackTrace.equals(that.stackTrace) : that.stackTrace != null) {
            return false;
        }
        if (balance != null ? !balance.equals(that.balance) : that.balance != null) {
            return false;
        }
        if (nonce != null ? !nonce.equals(that.nonce) : that.nonce != null) {
            return false;
        }
        if (transfers != null ? !transfers.equals(that.transfers) : that.transfers != null) {
            return false;
        }
        return events != null ? events.equals(that.events) : that.events == null;
    }

    @Override
    public int hashCode() {
        int result1 = (int) (gasUsed ^ (gasUsed >>> 32));
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (revert ? 1 : 0);
        result1 = 31 * result1 + (error ? 1 : 0);
        result1 = 31 * result1 + (errorMessage != null ? errorMessage.hashCode() : 0);
        result1 = 31 * result1 + (stackTrace != null ? stackTrace.hashCode() : 0);
        result1 = 31 * result1 + (balance != null ? balance.hashCode() : 0);
        result1 = 31 * result1 + (nonce != null ? nonce.hashCode() : 0);
        result1 = 31 * result1 + (transfers != null ? transfers.hashCode() : 0);
        result1 = 31 * result1 + (events != null ? events.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString() {
        return "ProgramResult{" +
                "gasUsed=" + gasUsed +
                ", result=" + result +
                ", revert=" + revert +
                ", error=" + error +
                ", errorMessage=" + errorMessage +
                ", stackTrace=" + stackTrace +
                ", balance=" + balance +
                ", nonce=" + nonce +
                ", transfers=" + transfers +
                ", events=" + events +
                '}';
    }

}
