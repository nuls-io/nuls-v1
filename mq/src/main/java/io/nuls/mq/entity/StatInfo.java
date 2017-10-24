package io.nuls.mq.entity;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
public interface StatInfo {
    public void putOne();

    public void takeOne();



    public String toString();

    /**
     * @return 本次启动后总写入条数
     */
    public AtomicLong getInCount();
    /**
     * @return 本次启动后总读出条数
     */
    public AtomicLong getOutCount();

    /**
     * @return 队列名称
     */
    public String getName();

    /**
     * @return 本次启动时间点
     */
    public long getStartTime();

    /**
     * @return 启动前文件中积压的未读取条数
     */
    public long getLastSize();

    /**
     * @return 获取速度计算的统计时间（秒）
     */
    public int getLatelySecond();

    public long getLastInCount();

    public long getLastOutCount();

    /**
     * @return 最近写入速度（条/秒）
     */
    public long getLatelyInTps();

    /**
     * @return 最近读出速度（条/秒）
     */
    public long getLatelyOutTps();

    public void setLastInCount(long lastInCount);

    public void setLastOutCount(long lastOutCount);

    public void setLatelyInTps(long latelyInTps);

    public void setLatelyOutTps(long latelyOutTps);

}
