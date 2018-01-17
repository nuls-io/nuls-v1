package io.nuls.consensus.entity;

/**
 * @author Niels
 * @date 2018/1/16
 */
public class DownloadRound {

    private long start;
    private long end;

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
