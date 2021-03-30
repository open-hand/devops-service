package io.choerodon.devops.app.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsClusterNodeService;

@ConditionalOnProperty(value = "local.test", havingValue = "false", matchIfMissing = true)
@Component
public class ClusterTask {

    @Autowired
    private DevopsClusterNodeService devopsClusterNodeService;

    /**
     * 定时更新集群安装状态
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    public void updateCluster() {
        devopsClusterNodeService.update();
    }
}
