package io.nuls;

import io.nuls.mq.intf.IQueueService;
import io.nuls.queue.service.QueueServiceImpl;
import io.nuls.util.log.Log;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Niels on 2017/9/21.
 */
public class MQModuleTest {

    private final String queueName = "test1";
    private IQueueService<Long> service = QueueServiceImpl.getInstance();


    @Test
    public void test() {
        //创建
        boolean b = service.createQueue(queueName, 64, true);
        assertTrue(b);

        //写入
        int count = 10;
        long start = System.currentTimeMillis();
        for (; count >= 0; count--) {
            service.offer(queueName, count - 1l);
        }
        Log.info("offer count=" + count + ",use time(ms):" + (System.currentTimeMillis() - start));
        assertTrue(true);

        //取出
        while (true) {
            Log.info("start poll....");

            Long data = null;
//            try {
//                data = intf.take(queueName);
//            } catch (InterruptedException e) {
//                log.error("", e);
//            }
            data = service.poll(queueName);
            if (data == null) {
                break;
            }
            Log.info("poll data:" + data);
        }


        service.destroyQueue(queueName);
        assertTrue(true);
    }


}
