package io.choerodon.devops.app.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsMiddlewareService;

//@ConditionalOnProperty(value = "local.test",havingValue = "false",matchIfMissing = true)
@Component
public class DeployTask {
    @Autowired
    private DevopsMiddlewareService devopsMiddlewareService;

    /**
     * 定时更新中间件的安装状态,每3分钟执行一次
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    public void updateStatus(){
        devopsMiddlewareService.updateMiddlewareStatus();
    }
}
