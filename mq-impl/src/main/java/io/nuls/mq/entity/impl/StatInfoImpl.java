package io.nuls.mq.entity.impl;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.mq.entity.StatInfo;
import io.nuls.mq.manager.QueueManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 队列统计信息
 * Created by Niels on 2017/9/21.
 *
 */
public class StatInfoImpl implements StatInfo {
    private String name;
    //加载时间
    private long startTime;
    private long lastSize;//初始化数量
    //    总进入数量
    private AtomicLong inCount = new AtomicLong();
    //    总取出数量
    private AtomicLong outCount = new AtomicLong();
    private int latelySecond = 10;
    private long lastInCount = 0l;
    private long lastOutCount = 0l;
    //    最近添加速度
    private long latelyInTps;
    //    最近取出速度
    private long latelyOutTps;


    /**
     * @param queueName    队列名称
     * @param lastSize     启动后从磁盘加载的数据条数
     * @param latelySecond 速度统计的时间段（秒）
     */
    public StatInfoImpl(String queueName, long lastSize, int latelySecond) {
        if (queueName==null||queueName.trim().isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.QUEUE_NAME_ERROR);
        }
        this.name = queueName.substring(6);
        this.lastSize = lastSize;
        this.latelySecond = latelySecond;
        this.startTime = System.currentTimeMillis();
    }

    public void putOne() {
        inCount.addAndGet(1);
    }

    public void takeOne() {
        outCount.addAndGet(1);
    }


    private String dateFormat(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public String toString() {
        StringBuilder log = new StringBuilder();
        log.append("队列监控：");
        log.append(name);
        log.append(",启动时间：");
        log.append(dateFormat(startTime));
        log.append(",遗留数量：");
        log.append(lastSize);
        log.append(",总进入：");
        log.append(inCount.get());
        log.append(",总取出：");
        log.append(outCount.get());
        log.append(",当前积压：");
        log.append(QueueManager.size(name));
        log.append(",最近");
        log.append(latelySecond);
        log.append("秒进入速度：");
        log.append(latelyInTps);
        log.append("条/秒");
        log.append(",最近");
        log.append(latelySecond);
        log.append("秒取出速度：");
        log.append(latelyOutTps);
        log.append("条/秒\n");
        return log.toString();
    }

    /**
     * @return 本次启动后总写入条数
     */
    public AtomicLong getInCount() {
        return inCount;
    }

    /**
     * @return 本次启动后总读出条数
     */
    public AtomicLong getOutCount() {
        return outCount;
    }

    /**
     * @return 队列名称
     */
    public String getName() {
        return name;
    }

    /**
     * @return 本次启动时间点
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return 启动前文件中积压的未读取条数
     */
    public long getLastSize() {
        return lastSize;
    }

    /**
     * @return 获取速度计算的统计时间（秒）
     */
    public int getLatelySecond() {
        return latelySecond;
    }

    public long getLastInCount() {
        return lastInCount;
    }

    public long getLastOutCount() {
        return lastOutCount;
    }

    /**
     * @return 最近写入速度（条/秒）
     */
    public long getLatelyInTps() {
        return latelyInTps;
    }

    /**
     * @return 最近读出速度（条/秒）
     */
    public long getLatelyOutTps() {
        return latelyOutTps;
    }

    public void setLastInCount(long lastInCount) {
        this.lastInCount = lastInCount;
    }

    public void setLastOutCount(long lastOutCount) {
        this.lastOutCount = lastOutCount;
    }

    public void setLatelyInTps(long latelyInTps) {
        this.latelyInTps = latelyInTps;
    }

    public void setLatelyOutTps(long latelyOutTps) {
        this.latelyOutTps = latelyOutTps;
    }
}
