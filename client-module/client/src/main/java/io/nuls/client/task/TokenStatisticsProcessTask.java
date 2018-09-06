package io.nuls.client.task;

import io.nuls.client.process.TokenStatisticsProcess;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.ModuleStatusEnum;
import io.nuls.kernel.module.BaseModuleBootstrap;
import io.nuls.kernel.module.service.ModuleService;

/**
 * @author: Charlie
 * @date: 2018/8/22
 */
public class TokenStatisticsProcessTask implements Runnable {

    private TokenStatisticsProcess tokenStatisticsProcess;

    public TokenStatisticsProcessTask (TokenStatisticsProcess tokenStatisticsProcess){
        this.tokenStatisticsProcess = tokenStatisticsProcess;
    }

    private boolean startSuccess = false;

    @Override
    public void run() {
        try {
            while (!startSuccess){
                startSuccess = true;
                ModuleService moduleService = ModuleService.getInstance();
                for (BaseModuleBootstrap module : moduleService.getModuleList()){
                    if( moduleService.getModuleState(module.getModuleId()) != ModuleStatusEnum.RUNNING){
                        startSuccess = false;
                    }
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            tokenStatisticsProcess.process();
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
