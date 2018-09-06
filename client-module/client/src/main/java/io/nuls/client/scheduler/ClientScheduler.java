package io.nuls.client.scheduler;

import io.nuls.client.process.TokenStatisticsProcess;
import io.nuls.client.task.TokenStatisticsProcessTask;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.kernel.thread.manager.TaskManager;

import java.util.Calendar;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: Charlie
 * @date: 2018/8/22
 */
public class ClientScheduler {

    public static ClientScheduler INSTANCE = new ClientScheduler();

    private ScheduledThreadPoolExecutor threadPool;

    private ClientScheduler(){}

    public boolean start(){
        threadPool = TaskManager.createScheduledThreadPool(1, new NulsThreadFactory((short) 0, "clientScheduler"));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR,calendar.get(Calendar.HOUR) + 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long next = calendar.getTime().getTime();
        long offset = (next - System.currentTimeMillis()) / 1000;
        System.out.println("距下个整点(秒)：" + offset + "   ==================");
        System.out.println("距下个整点(分钟)：" + offset/60 + "   ==================");

        TokenStatisticsProcess tokenStatisticsProcess = new TokenStatisticsProcess();
        threadPool.scheduleAtFixedRate(new TokenStatisticsProcessTask(tokenStatisticsProcess), 0, 5, TimeUnit.SECONDS);
        return true;
    }
}
