package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.util.UtilityElf;
import io.choerodon.devops.api.vo.kubernetes.CheckLog;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.ApplicationShareRuleDTO;
import io.choerodon.devops.infra.dto.DevopsCheckLogDTO;
import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDTO;
import io.choerodon.devops.infra.mapper.*;
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
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private PipelineRecordMapper pipelineRecordMapper;
    @Autowired
    private PipelineStageService pipelineStageService;
    @Autowired
    private PipelineTaskService pipelineTaskService;
    @Autowired
    private PipelineAppDeployService pipelineAppDeployService;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;

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
//                syncEnvAppRelevance(logs);
//                syncAppShare();
                syncDeployRecord(logs);

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
                        "Sync environment application relationship,envId: %s, appId: %s", v.getEnvId(), v.getAppServiceId()));
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

        private void syncAppShare(List<CheckLog> logs) {
            LOGGER.info("delete application market data.");
            applicationShareMapper.deleteAll();
            LOGGER.info("insert application share rule.");
            applicationVersionMapper.selectAll().stream()
                    .filter(versionDTO -> versionDTO.getIsPublish() != null && versionDTO.getIsPublish().equals(1L))
                    .forEach(versionDTO -> {
                        CheckLog checkLog = new CheckLog();
                        checkLog.setContent(String.format(
                                "Sync application share rule,versionId: %s, appServiceId: %s", versionDTO.getId(), versionDTO.getAppServiceId()));
                        ApplicationShareRuleDTO applicationShareRuleDTO = new ApplicationShareRuleDTO();
                        applicationShareRuleDTO.setShareLevel("organization");
                        applicationShareRuleDTO.setVersion(versionDTO.getVersion());
                        applicationShareRuleDTO.setAppServiceId(versionDTO.getAppServiceId());
                        if (applicationShareMapper.insert(applicationShareRuleDTO) != 1) {
                            checkLog.setResult("failed");
                        } else {
                            checkLog.setResult("success");
                        }
                        logs.add(checkLog);
                    });
            LOGGER.info("update publish Time.");
            applicationVersionMapper.updatePublishTime();
        }


        private void syncDeployRecord(List<CheckLog> checkLogs) {
            LOGGER.info("修复部署记录数据开始");
            //部署实例的记录
            List<DevopsDeployRecordDTO> devopsDeployRecordDTOS = devopsEnvCommandMapper.listAllInstanceCommand()
                    .stream()
                    .filter(devopsEnvCommandDTO -> devopsEnvCommandDTO.getProjectId() != null)
                    .map(devopsEnvCommandDTO -> {
                        DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(devopsEnvCommandDTO.getProjectId(), "manual", devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getEnvId().toString(), devopsEnvCommandDTO.getCreationDate());
                        return devopsDeployRecordDTO;
                    }).collect(Collectors.toList());


            //流水线内部署实例的记录
            devopsDeployRecordDTOS.addAll(pipelineRecordMapper.listAllPipelineRecordAndEnv(null).stream().map(pipelineRecordDTO -> {
                DevopsDeployRecordDTO devopsDeployRecordDTO = new DevopsDeployRecordDTO(pipelineRecordDTO.getProjectId(), "auto", pipelineRecordDTO.getId(), pipelineRecordDTO.getEnv(), pipelineRecordDTO.getCreationDate());
                return devopsDeployRecordDTO;
            }).collect(Collectors.toList()));

            devopsDeployRecordDTOS.stream().sorted(Comparator.comparing(DevopsDeployRecordDTO::getDeployTime)).forEach(devopsDeployRecordDTO -> devopsDeployRecordService.baseCreate(devopsDeployRecordDTO));

            LOGGER.info("修复部署记录数据结束");

        }


    }
}
