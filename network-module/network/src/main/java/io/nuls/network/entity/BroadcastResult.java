/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.network.entity;

import io.nuls.kernel.constant.ErrorCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class BroadcastResult implements Cloneable {

    private boolean success;

    private ErrorCode errorCode;

    private String hash;

    private List<Node> broadcastNodes;

    private int waitReplyCount;

    private int repliedCount;

    public BroadcastResult() {
        broadcastNodes = new ArrayList<>();
    }

    public BroadcastResult(boolean success, ErrorCode errorCode) {
        this();
        this.success = success;
        this.errorCode = errorCode;
    }

    public BroadcastResult(boolean success, ErrorCode errorCode, List<Node> broadcastNodes) {
        this(success, errorCode);
        this.broadcastNodes = broadcastNodes;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<Node> getBroadcastNodes() {
        return broadcastNodes;
    }

    public void setBroadcastNodes(List<Node> broadcastNodes) {
        this.broadcastNodes = broadcastNodes;
    }

    public int getWaitReplyCount() {
        return waitReplyCount;
    }

    public void setWaitReplyCount(int waitReplyCount) {
        this.waitReplyCount = waitReplyCount;
    }

    public int getRepliedCount() {
        return repliedCount;
    }

    public void setRepliedCount(int repliedCount) {
        this.repliedCount = repliedCount;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public Object clone() {
        BroadcastResult result = new BroadcastResult();
        result.setHash(this.hash);
        result.setSuccess(this.success);
        result.setWaitReplyCount(this.waitReplyCount);
        result.setRepliedCount(this.repliedCount);
        result.setBroadcastNodes(this.broadcastNodes);
        result.setErrorCode(this.getErrorCode());
        return result;
    }


}
