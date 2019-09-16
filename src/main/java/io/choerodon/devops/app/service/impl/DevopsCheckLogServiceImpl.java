package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.util.UtilityElf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.api.vo.kubernetes.CheckLog;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.app.service.DevopsDeployRecordService;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectCategoryDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.OrgServiceClientOperator;
import io.choerodon.devops.infra.mapper.*;
import io.choerodon.devops.infra.util.ConvertUtils;


@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);
    private static final String SUCCESS = "success";
    private static final String FAILED = "failed";
    private static final ExecutorService executorService = new ThreadPoolExecutor(0, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new UtilityElf.DefaultThreadFactory("devops-upgrade", false));

    @Autowired
    private AppServiceVersionMapper appServiceVersionMapper;
    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;
    @Autowired
    private DevopsClusterMapper devopsClusterMapper;
    @Autowired
    private AppServiceShareRuleMapper applicationShareMapper;
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
    @Autowired
    private DevopsEnvApplicationService devopsEnvApplicationService;
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;
    @Autowired
    private PipelineRecordMapper pipelineRecordMapper;
    @Autowired
    private DevopsDeployRecordService devopsDeployRecordService;
    @Autowired
    private DevopsProjectMapper devopsProjectMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private OrgServiceClientOperator orgServiceClientOperator;
    @Autowired
    private DevopsConfigMapper devopsConfigMapper;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
    @Autowired
    private DevopsBranchMapper devopsBranchMapper;


    @Override
    public void checkLog(String version) {
        LOGGER.info("start upgrade task");
        LOGGER.info("sync config begin!!!");

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
//                syncAppShare(logs);
//                syncDeployRecord(logs);
//                syncClusterAndCertifications(logs);
//                syncConfig();
                  syncBranch();

            } else {
                LOGGER.info("version not matched");
            }

            devopsCheckLogDTO.setLog(JSON.toJSONString(logs));
            devopsCheckLogDTO.setEndCheckDate(new Date());

            devopsCheckLogMapper.insert(devopsCheckLogDTO);
        }


        private void syncBranch() {
            //删除状态为已删除的分支
            devopsBranchMapper.deleteByIsDelete();
            //删除重复的分支
            devopsBranchMapper.deleteDuplicateBranch();
        }

        private void syncConfig() {

            LOGGER.info("sync config begin!!!");

            //避免2次修复数据
            List<DevopsConfigDTO> configs = devopsConfigMapper.existAppServiceConfig();
            if (configs.isEmpty()) {
                DevopsConfigDTO harborDefault = devopsConfigMapper.queryByNameWithNoProject("harbor_default");
                DevopsConfigDTO chartDefault = devopsConfigMapper.queryByNameWithNoProject("chart_default");
                List<DevopsConfigDTO> addHarborConfigs = new ArrayList<>();
                List<DevopsConfigDTO> addChartConfigs = new ArrayList<>();
                appServiceMapper.selectAll().forEach(appServiceDTO -> {
                    if (appServiceDTO.getHarborConfigId() != null && !appServiceDTO.getHarborConfigId().equals(harborDefault.getId())) {
                        DevopsConfigDTO devopsConfigDTO = devopsConfigMapper.selectByPrimaryKey(appServiceDTO.getHarborConfigId());
                        if (devopsConfigDTO == null) {
                            appServiceMapper.updateHarborConfigNullByServiceId(appServiceDTO.getId());
                        } else {
                            devopsConfigDTO.setId(null);
                            devopsConfigDTO.setProjectId(null);
                            devopsConfigDTO.setAppServiceId(appServiceDTO.getId());
                            addHarborConfigs.add(devopsConfigDTO);
                        }
                    }
                    if (appServiceDTO.getChartConfigId() != null && !appServiceDTO.getChartConfigId().equals(chartDefault.getId())) {
                        DevopsConfigDTO devopsConfigDTO = devopsConfigMapper.selectByPrimaryKey(appServiceDTO.getChartConfigId());
                        if (devopsConfigDTO == null) {
                            appServiceMapper.updateChartConfigNullByServiceId(appServiceDTO.getId());
                        } else {
                            devopsConfigDTO.setId(null);
                            devopsConfigDTO.setProjectId(null);
                            devopsConfigDTO.setAppServiceId(appServiceDTO.getId());
                            addChartConfigs.add(devopsConfigDTO);
                        }
                    }
                });
                devopsConfigMapper.deleteByProject();
                appServiceMapper.updateHarborConfigNullByConfigId(harborDefault.getId());
                appServiceMapper.updateChartConfigNullByConfigId(chartDefault.getId());
                addHarborConfigs.forEach(devopsConfigDTO -> {
                    devopsConfigMapper.insert(devopsConfigDTO);
                    AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(devopsConfigDTO.getAppServiceId());
                    appServiceDTO.setHarborConfigId(devopsConfigDTO.getId());
                    appServiceMapper.updateByPrimaryKey(appServiceDTO);
                });
                addChartConfigs.forEach(devopsConfigDTO -> {
                    devopsConfigMapper.insert(devopsConfigDTO);
                    AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(devopsConfigDTO.getAppServiceId());
                    appServiceDTO.setChartConfigId(devopsConfigDTO.getId());
                    appServiceMapper.updateByPrimaryKey(appServiceDTO);
                });
                LOGGER.info("sync config end!!!");
            }


        }

        private void syncEnvAppRelevance(List<CheckLog> logs) {
            List<DevopsEnvAppServiceDTO> applicationInstanceDTOS = ConvertUtils.convertList(appServiceInstanceMapper.selectAll(), DevopsEnvAppServiceDTO.class);

            applicationInstanceDTOS.stream().distinct().forEach(v -> {
                CheckLog checkLog = new CheckLog();
                checkLog.setContent(String.format(
                        "Sync environment application relationship,envId: %s, appServiceId: %s", v.getEnvId(), v.getAppServiceId()));
                try {
                    devopsEnvApplicationService.baseCreate(v);
                    checkLog.setResult(SUCCESS);
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
            appServiceVersionMapper.selectAll().stream()
                    .filter(versionDTO -> versionDTO.getIsPublish() != null && versionDTO.getIsPublish().equals(1L))
                    .forEach(versionDTO -> {
                        CheckLog checkLog = new CheckLog();
                        checkLog.setContent(String.format(
                                "Sync application share rule,versionId: %s, appServiceId: %s", versionDTO.getId(), versionDTO.getAppServiceId()));
                        AppServiceShareRuleDTO appServiceShareRuleDTO = new AppServiceShareRuleDTO();
                        appServiceShareRuleDTO.setShareLevel("organization");
                        appServiceShareRuleDTO.setVersion(versionDTO.getVersion());
                        appServiceShareRuleDTO.setAppServiceId(versionDTO.getAppServiceId());
                        if (applicationShareMapper.insert(appServiceShareRuleDTO) != 1) {
                            checkLog.setResult(FAILED);
                        } else {
                            checkLog.setResult(SUCCESS);
                        }
                        logs.add(checkLog);
                    });
            LOGGER.info("update publish Time.");
            appServiceVersionMapper.updatePublishTime();
        }


        private void syncDeployRecord(List<CheckLog> checkLogs) {
            LOGGER.info("修复部署记录数据开始");
            //部署实例的记录
            List<DevopsDeployRecordDTO> devopsDeployRecordDTOS = devopsEnvCommandMapper.listAllInstanceCommand()
                    .stream()
                    .filter(devopsEnvCommandDTO -> devopsEnvCommandDTO.getProjectId() != null)
                    .map(devopsEnvCommandDTO -> new DevopsDeployRecordDTO(devopsEnvCommandDTO.getProjectId(), "manual", devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getEnvId().toString(), devopsEnvCommandDTO.getCreationDate()))
                    .collect(Collectors.toList());


            //流水线内部署实例的记录
            devopsDeployRecordDTOS.addAll(
                    pipelineRecordMapper.listAllPipelineRecordAndEnv(null)
                            .stream()
                            .map(pipelineRecordDTO -> new DevopsDeployRecordDTO(pipelineRecordDTO.getProjectId(), "auto", pipelineRecordDTO.getId(), pipelineRecordDTO.getEnv(), pipelineRecordDTO.getCreationDate()))
                            .collect(Collectors.toList()));

            devopsDeployRecordDTOS.stream().sorted(Comparator.comparing(DevopsDeployRecordDTO::getDeployTime)).forEach(devopsDeployRecordDTO -> devopsDeployRecordService.baseCreate(devopsDeployRecordDTO));

            LOGGER.info("修复部署记录数据结束");

        }

        private void syncClusterAndCertifications(List<CheckLog> checkLogs) {
            LOGGER.info("开始迁移集群和证书到项目下!");
            Map<Long, List<DevopsClusterDTO>> clusters = devopsClusterMapper.selectAll().stream()
                    .collect(Collectors.groupingBy(DevopsClusterDTO::getOrganizationId));
            Map<Long, List<CertificationDTO>> orgCertifications = devopsCertificationMapper.listAllOrgCertification()
                    .stream()
                    .collect(Collectors.groupingBy(CertificationDTO::getOrganizationId));

            List<Long> categoryIds = orgServiceClientOperator.baseProjectCategoryList(0L, "普通项目群")
                    .getList().stream().map(ProjectCategoryDTO::getId).collect(Collectors.toList());

            Set<Long> allOrgIds = new HashSet<>(clusters.keySet());
            allOrgIds.addAll(orgCertifications.keySet());

            allOrgIds.forEach(organizationId -> {
                // 创建组织下的项目
                ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO();
                projectCreateDTO.setName("默认运维项目");
                projectCreateDTO.setCode("def-ops-proj");
                projectCreateDTO.setCategoryIds(categoryIds);
                projectCreateDTO.setOrganizationId(organizationId);
                ProjectDTO projectDTO = baseServiceClientOperator.createProject(organizationId, projectCreateDTO);

                // 迁移集群
                if (clusters.containsKey(organizationId)) {
                    clusters.get(organizationId).forEach(cluster -> {
                        CheckLog checkLog = new CheckLog();
                        checkLog.setContent(String.format(
                                "Sync cluster migration to the project,clusterId: %s, organizationId: %s", cluster.getId(), organizationId));
                        if (projectDTO != null) {
                            cluster.setProjectId(projectDTO.getId());
                            checkLog.setResult(devopsClusterMapper.updateByPrimaryKeySelective(cluster) != 1 ? FAILED : SUCCESS);
                        } else {
                            checkLog.setResult(FAILED);
                        }
                        checkLogs.add(checkLog);
                    });
                }

                // 迁移证书
                if (orgCertifications.containsKey(organizationId)) {
                    orgCertifications.get(organizationId).forEach(cert -> {
                        CheckLog checkLog = new CheckLog();
                        checkLog.setContent(String.format("Migrate organization certification to the project, org-cert-id: %s, organizationId: %s", cert.getId(), organizationId));
                        if (projectDTO != null) {
                            cert.setProjectId(projectDTO.getId());
                            checkLog.setResult(devopsCertificationMapper.updateByPrimaryKeySelective(cert) != 1 ? FAILED : SUCCESS);
                        } else {
                            checkLog.setResult(FAILED);
                        }
                        checkLogs.add(checkLog);
                    });
                }
            });
            LOGGER.info("迁移集群及证书到项目下已完成！");
        }
    }
}
