package io.nuls.core.utils.date;

import io.nuls.core.utils.network.RequestUtil;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * 时间服务，存储网络时间与本地时间，所有与网络交互的时间，经过换算，保证网络时间同步
 * @author ln
 *
 */
public final class TimeService {

	private static final Logger log = LoggerFactory.getLogger(TimeService.class);
	
	/** 时间偏移差距触发点，超过该值会导致本地时间重设，单位毫秒 **/
	public static final long TIME_OFFSET_BOUNDARY = 1000L;
	
	/*
	 * 网络时间刷新间隔
	 */
	private static final long TIME_REFRESH_TIME = 600000L;

	/*
	 * 模拟网络时钟 
	 */
	private volatile static Date mockTime;
	/*
	 * 网络时间与本地时间的偏移
	 */
	private static long netTimeOffset;
	//网络时间是否通过第一种方式进行了同步，如果没有，则通过第二种方式同步
	private static boolean netTimeHasSync;
	
	/*
	 * 系统启动时间
	 */
	private final static Date systemStartTime = new Date();
	
	private long lastInitTime;
	private boolean running;



	
	@PostConstruct
	public void start() {
		startSyn();
	}
	
	@PreDestroy
	public void stop() {
		running = false;
	}

	
	/**
	 * 异步启动时间服务
	 */
	private void startSyn() {
		initTime();
		Thread monitorThread = new Thread() {
			@Override
			public void run() {
				monitorTimeChange();
			}
		};
		monitorThread.setName("time change monitor");
		monitorThread.start();
	}
	
	private void initTime() {
		
		String timeServiceUrl = "http://time.inchain.org/now";
		
		long nowTime = System.currentTimeMillis();
		
		String res = RequestUtil.doGet(timeServiceUrl, null);
		try {
			TimeObj time = JSONUtils.json2pojo(res, TimeObj.class);
			if(time.isSuccess()) {
				long netTime = time.getTime();
				
				long newLocalNowTime = System.currentTimeMillis();
				
				netTimeOffset = (netTime + (newLocalNowTime - nowTime) / 2) - System.currentTimeMillis();

				initSuccess();
			} else {
				initError();
			}
		} catch (Exception e) {
			initError();
		}
	}
	
	private void initError() {
		//1分钟后重试
		lastInitTime = System.currentTimeMillis() - 60000L;
		
		log.info("---------------本地时间调整出错---------------");
	}
	
	private void initSuccess() {
		lastInitTime = System.currentTimeMillis();
		if(!netTimeHasSync) {
			netTimeHasSync = true;
		}
	}

	/**
	 * 启动一个服务，监控本地时间的变化
	 * 如果本地时间有变化，则设置 TimeService.netTimeOffset;
	 */
	public void monitorTimeChange() {
		
		//不监控本地时间变化是最保险的，难免遇到机器卡顿的时候，会把本来正确的时间置为错误的
		//如果用户在运行过程中自己去调整电脑的时候，这是他的问题，出错也无可厚非
		
		long lastTime = System.currentTimeMillis();
		
		running = true;
		
		while(running) {
			//动态调整网络时间
			
			long newTime = System.currentTimeMillis();
			if(Math.abs(newTime - lastTime) > TIME_OFFSET_BOUNDARY) {
				log.info("本地时间调整了：{}", newTime - lastTime);
				initTime();
			} else if(currentTimeMillis() - lastInitTime > TIME_REFRESH_TIME) {
				//每隔一段时间更新网络时间
				initTime();
			}
			lastTime = newTime;
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
			}
		}
	}
	
	/**
     * 以给定的秒数推进（或倒退）网络时间
     */
    public static Date rollMockClock(int seconds) {
        return rollMockClockMillis(seconds * 1000);
    }

    /**
     * 以给定的毫秒数推进（或倒退）网络时间
     */
    public static Date rollMockClockMillis(long millis) {
        if (mockTime == null){
        	throw new IllegalStateException("You need to use setMockClock() first.");
		}
        mockTime = new Date(mockTime.getTime() + millis);
        return mockTime;
    }

    /**
     * 将网络时间设置为当前时间
     */
    public static void setMockClock() {
        mockTime = new Date();
    }

    /**
     * 将网络时间设置为给定时间（以秒为单位）
     */
    public static void setMockClock(long mockClockSeconds) {
        mockTime = new Date(mockClockSeconds * 1000);
    }

    /**
	 * 当前网络时间
	 * @return long
	 */
    public static Date now() {
        return mockTime != null ? mockTime : new Date();
    }

    /**
	 * 当前毫秒时间
	 * @return long
	 */
    public static long currentTimeMillis() {
        return System.currentTimeMillis() + netTimeOffset;
    }
	
	/**
	 * 当前时间秒数
	 * @return long
	 */
	public static long currentTimeSeconds() {
		return currentTimeMillis() / 1000;
	}
	
	/**
	 * 获取系统运行时长（返回毫秒数）
	 * @return long
	 */
	public static long getSystemRuningTimeMillis() {
		return System.currentTimeMillis() - systemStartTime.getTime();
	}
	
	/**
	 * 获取系统运行时长（返回秒数）
	 * @return long
	 */
	public static long getSystemRuningTimeSeconds() {
		return getSystemRuningTimeMillis() / 1000;
	}
	
	/**
	 * 返回当前时间毫秒数的字节数组
	 * @return byte[]
	 */
	public static byte[] currentTimeMillisOfBytes() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
		try {
			Utils.int64ToByteStreamLE(currentTimeMillis(), bos);
			return bos.toByteArray();
		} catch (Exception e) {
			return null;
		} finally {
			try {
				bos.close();
			} catch (IOException e) {
				Log.error(e);
			}
		}
	}

	/**
	 * 设置网络偏移时间
	 * @param netTimeOffset
	 */
	public static void setNetTimeOffset(long netTimeOffset) {
		if(!netTimeHasSync) {
			TimeService.netTimeOffset = netTimeOffset;
		}
	}
	public static long getNetTimeOffset() {
		return netTimeOffset;
	}


	class TimeObj{
		private long time;
		private boolean success;

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}
	}
}
