package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class TimeService {

    private String webTimeUrl;


    private TimeService() {
        webTimeUrl = "http://www.baidu.com";
    }

    private void syncWebTime() {
        try {
            URL url = new URL(webTimeUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            Date date = new Date(connection.getDate());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.debug(sdf.format(date));
        } catch (IOException e) {
            throw new NulsRuntimeException(ErrorCode.NET_SERVER_START_ERROR, e);
        }

    }

    public static void main(String[] args) {
        TimeService timeService = new TimeService();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    timeService.syncWebTime();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.error(e);
                    }
                }
            }
        }).start();

    }
}
