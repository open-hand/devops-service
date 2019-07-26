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
import io.choerodon.devops.api.vo.kubernetes.CheckLog;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.choerodon.devops.infra.dto.DevopsCheckLogDTO;
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDTO;
import io.choerodon.devops.infra.mapper.ApplicationInstanceMapper;
import io.choerodon.devops.infra.mapper.ApplicationShareRuleMapper;
import io.choerodon.devops.infra.mapper.ApplicationVersionMapper;
import io.choerodon.devops.infra.mapper.DevopsCheckLogMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);
    private static final ExecutorService executorService = new ThreadPoolExecutor(0, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new UtilityElf.DefaultThreadFactory("devops-upgrade", false));

    @Autowired
    private ApplicationVersionMapper applicationVersionMapper;
    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;
    @Autowired
    private ApplicationShareRuleMapper applicationShareMapper;
    @Autowired
    private ApplicationInstanceMapper applicationInstanceMapper;
    @Autowired
    private DevopsEnvApplicationService devopsEnvApplicationService;

    @Override
    public void checkLog(String version) {
        LOGGER.info("start upgrade task");
        executorService.submit(new UpgradeTask(version));
    }

    class UpgradeTask implements Runnable {
        private String version;
        private Long env;

        UpgradeTask(String version) {
            this.version = version;
        }


        UpgradeTask(String version, Long env) {
            this.version = version;
            this.env = env;
        }

        @Override
        public void run() {
            DevopsCheckLogDTO devopsCheckLogDTO = new DevopsCheckLogDTO();
            List<CheckLog> logs = new ArrayList<>();
            devopsCheckLogDTO.setBeginCheckDate(new Date());
            if ("0.19.0".equals(version)) {
                syncEnvAppRelevance(logs);
                syncAppShare();
            } else {
                LOGGER.info("version not matched");
            }

            devopsCheckLogDTO.setLog(JSON.toJSONString(logs));
            devopsCheckLogDTO.setEndCheckDate(new Date());

            devopsCheckLogMapper.insert(devopsCheckLogDTO);
        }

        private void syncEnvAppRelevance(List<CheckLog> logs) {
            List<DevopsEnvApplicationDTO> applicationInstanceDTOS = ConvertUtils.convertList(applicationInstanceMapper.selectAll(), DevopsEnvApplicationDTO.class);

            applicationInstanceDTOS.stream().distinct().forEach(v -> {
                CheckLog checkLog = new CheckLog();
                checkLog.setContent(String.format(
                        "Sync environment application relationship,envId: %s, appId: %s", v.getEnvId(), v.getAppId()));
                try {
                    devopsEnvApplicationService.baseCreate(v);
                    checkLog.setResult("success");
                } catch (Exception e) {
                    checkLog.setResult("fail");
                    LOGGER.info(e.getMessage(), e);
                }
                logs.add(checkLog);
            });
        }

        private void syncAppShare() {
            LOGGER.info("update publish level to organization.");
            applicationShareMapper.updatePublishLevel();
            LOGGER.info("update publish level success.");
            LOGGER.info("update publish Time.");
            applicationVersionMapper.updatePublishTime();
            LOGGER.info("update publish time success.");
        }
    }
}
