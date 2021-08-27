package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.util.UtilityElf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.kubernetes.CheckLog;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsCheckLogDTO;
import io.choerodon.devops.infra.mapper.DevopsCheckLogMapper;


@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);
    private static final ExecutorService executorService = new ThreadPoolExecutor(0, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new UtilityElf.DefaultThreadFactory("devops-upgrade", false));

    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;
    @Autowired
    private DevopsBranchService devopsBranchService;
    @Autowired
    private DevopsGitlabCommitService devopsGitlabCommitService;
    @Autowired
    private DevopsEnvApplicationService devopsEnvApplicationService;
    @Autowired
    private DevopsIssueRelService devopsIssueRelService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;

    @Override
    public void checkLog(String version) {
        LOGGER.info("start upgrade task");
        executorService.execute(new UpgradeTask(version));
    }

    private static void printRetryNotice() {
        LOGGER.error("======================================================================================");
        LOGGER.error("Please retry data migration later in choerodon interface after cheorodon-front upgrade");
        LOGGER.error("======================================================================================");
    }


    class UpgradeTask implements Runnable {
        private String version;

        UpgradeTask(String version) {
            this.version = version;
        }

        UpgradeTask(String version, Long env) {
            this.version = version;
        }

        @Override
        public void run() {
            try {
                DevopsCheckLogDTO devopsCheckLogDTO = new DevopsCheckLogDTO();
                List<CheckLog> logs = new ArrayList<>();
                devopsCheckLogDTO.setBeginCheckDate(new Date());
                if ("1.1.0".equals(version)) {
                    devopsEnvApplicationService.fixData();
                    devopsDeployAppCenterService.fixData();
                } else {
                    LOGGER.info("version not matched");
                }

                devopsCheckLogDTO.setLog(JSON.toJSONString(logs));
                devopsCheckLogDTO.setEndCheckDate(new Date());

                devopsCheckLogMapper.insert(devopsCheckLogDTO);
            } catch (Exception ex) {
                printRetryNotice();
                LOGGER.warn("Exception occurred when applying data migration. The ex is: {}", ex);
            }
        }

    }
}
