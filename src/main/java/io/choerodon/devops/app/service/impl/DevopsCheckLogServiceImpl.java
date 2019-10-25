package io.choerodon.devops.app.service.impl;

import static io.choerodon.core.iam.InitRoleCode.PROJECT_OWNER;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.zaxxer.hikari.util.UtilityElf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import retrofit2.Response;
import retrofit2.Retrofit;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.RoleAssignmentSearchVO;
import io.choerodon.devops.api.vo.kubernetes.CheckLog;
import io.choerodon.devops.api.vo.kubernetes.ProjectCreateDTO;
import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.harbor.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.BaseServiceClient;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.*;


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
    private DevopsEnvAppServiceMapper devopsEnvAppServiceMapper;
    @Autowired
    private DevopsEnvCommandMapper devopsEnvCommandMapper;
    @Autowired
    private PipelineRecordMapper pipelineRecordMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsConfigMapper devopsConfigMapper;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
    @Autowired
    private DevopsBranchMapper devopsBranchMapper;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private DevopsDeployRecordMapper devopsDeployRecordMapper;
    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;
    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private DevopsProjectService devopsProjectService;

    @Override
    public void checkLog(String version) {
        LOGGER.info("start upgrade task");
        executorService.execute(new UpgradeTask(version));

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
            try {
                DevopsCheckLogDTO devopsCheckLogDTO = new DevopsCheckLogDTO();
                List<CheckLog> logs = new ArrayList<>();
                devopsCheckLogDTO.setBeginCheckDate(new Date());
                if ("0.19.0".equals(version)) {
                    syncEnvAppRelevance(logs);
                    // 0.20.0删除此方法
//                    syncAppShare(logs);
                    syncDeployRecord(logs);
                    syncClusterAndCertifications(logs);
                    syncConfig();
                    syncEnvAndAppServiceStatus();
                    syncBranch();
                    LOGGER.info("修复数据完成");
                } else if ("0.18.13".equals(version)) {
                    syncHarbor();
                } else {
                    LOGGER.info("version not matched");
                }

                devopsCheckLogDTO.setLog(JSON.toJSONString(logs));
                devopsCheckLogDTO.setEndCheckDate(new Date());

                devopsCheckLogMapper.insert(devopsCheckLogDTO);
            } catch (Throwable ex) {
                LOGGER.warn("Exception occurred when applying data migration. The ex is: {}", ex);
            }
        }


        private void syncBranch() {
            LOGGER.info("Start syncing branches.");
            //删除状态为已删除的分支
            devopsBranchMapper.deleteByIsDelete();
            //删除重复的分支
            devopsBranchMapper.deleteDuplicateBranch();
            LOGGER.info("End syncing branches.");

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

        private void syncHarbor() {
            LOGGER.info("开始同步创建harbor用户账号");
            ConfigurationProperties configurationProperties = new ConfigurationProperties(harborConfigurationProperties);
            configurationProperties.setType("harbor");
            Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
            HarborClient harborClient = retrofit.create(HarborClient.class);
            ResponseEntity<PageInfo<OrganizationDTO>> organizationDOS = baseServiceClient.listOrganizations(0, 0);
            if (organizationDOS.getStatusCode().is2xxSuccessful()) {
                try {
                    for (OrganizationDTO organizationDO : organizationDOS.getBody().getList()) {
                        OrganizationDTO organization = baseServiceClientOperator.queryOrganizationById(organizationDO.getId());
                        List<ProjectDTO> projectDTOS = baseServiceClientOperator.listIamProjectByOrgId(organizationDO.getId(), null, null, null);
                        for (ProjectDTO projectDTO : projectDTOS) {
                            LOGGER.info("开始创建{}{}{}的用户", projectDTO.getId(), projectDTO.getName(), projectDTO.getCode());
                            DevopsProjectDTO devopsProjectDTO = null;
                            try {
                                devopsProjectDTO = devopsProjectService.baseQueryByProjectId(projectDTO.getId());
                            } catch (CommonException e) {
                                //未同步成功的项目不处理
                            }
                            if (devopsProjectDTO != null) {
                                String username = devopsProjectDTO.getHarborProjectUserName() == null ? String.format("user%s%s", organization.getId(), projectDTO.getId()) : devopsProjectDTO.getHarborProjectUserName();
                                String email = devopsProjectDTO.getHarborProjectUserEmail() == null ? String.format("%s@harbor.com", username) : devopsProjectDTO.getHarborProjectUserEmail();
                                String password = devopsProjectDTO.getHarborProjectUserPassword() == null ? String.format("%spassword", username) : devopsProjectDTO.getHarborProjectUserPassword();
                                User user = new User(username, email, password, username);
                                //创建用户
                                Response<Void> result = null;
                                try {
                                    Response<List<User>> users = harborClient.listUser(username).execute();
                                    if (users.raw().code() != 200) {
                                        throw new CommonException(users.errorBody().string());
                                    }
                                    if (users.body().isEmpty()) {
                                        result = harborClient.insertUser(user).execute();
                                        if (result.raw().code() != 201) {
                                            throw new CommonException(result.errorBody().string());
                                        }
                                    } else {
                                        Boolean exist = users.body().stream().anyMatch(user1 -> user1.getUsername().equals(username));
                                        if (!exist) {
                                            result = harborClient.insertUser(user).execute();
                                            if (result.raw().code() != 201) {
                                                throw new CommonException(result.errorBody().string());
                                            }
                                        }
                                    }
                                    //给项目绑定角色
                                    Response<List<ProjectDetail>> projects = harborClient.listProject(organization.getCode() + "-" + projectDTO.getCode()).execute();
                                    if (projects.body() != null && !projects.body().isEmpty()) {
                                        Response<SystemInfo> systemInfoResponse = harborClient.getSystemInfo().execute();
                                        if (systemInfoResponse.raw().code() != 200) {
                                            throw new CommonException(systemInfoResponse.errorBody().string());
                                        }

                                        if (systemInfoResponse.body().getHarborVersion().equals("v1.4.0")) {
                                            Role role = new Role();
                                            role.setUsername(user.getUsername());
                                            role.setRoles(Arrays.asList(1));
                                            result = harborClient.setProjectMember(projects.body().get(0).getProjectId(), role).execute();
                                        } else {
                                            ProjectMember projectMember = new ProjectMember();
                                            MemberUser memberUser = new MemberUser();
                                            memberUser.setUsername(username);
                                            projectMember.setMemberUser(memberUser);
                                            result = harborClient.setProjectMember(projects.body().get(0).getProjectId(), projectMember).execute();
                                        }
                                        if (result.raw().code() != 201 && result.raw().code() != 200 && result.raw().code() != 409) {
                                            throw new CommonException(result.errorBody().string());
                                        }
                                        LOGGER.info("分配{}的角色成功", projectDTO.getName());
                                    }
                                    if (devopsProjectDTO.getHarborProjectUserPassword() == null) {
                                        devopsProjectDTO.setHarborProjectUserName(user.getUsername());
                                        devopsProjectDTO.setHarborProjectIsPrivate(false);
                                        devopsProjectDTO.setHarborProjectUserPassword(user.getPassword());
                                        devopsProjectDTO.setHarborProjectUserEmail(user.getEmail());
                                    }
                                    devopsProjectService.baseUpdate(devopsProjectDTO);
                                    LOGGER.info("创建{}的用户成功", projectDTO.getName());
                                } catch (IOException e) {
                                    throw new CommonException(e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.info("failed", e);
                    throw new CommonException(e);
                }
            }
            LOGGER.info("同步创建harbor用户账号成功");
        }

        private void syncEnvAppRelevance(List<CheckLog> logs) {
            LOGGER.info("Start syncing relevance.");
            List<DevopsEnvAppServiceDTO> applicationInstanceDTOS =appServiceInstanceMapper.listAllDistinctWithoutDeleted();

            applicationInstanceDTOS.forEach(v -> {
                CheckLog checkLog = new CheckLog();
                checkLog.setContent(String.format(
                        "Sync environment application relationship,envId: %s, appServiceId: %s", v.getEnvId(), v.getAppServiceId()));
                try {
                    devopsEnvAppServiceMapper.insertIgnore(v);
                    checkLog.setResult(SUCCESS);
                } catch (Exception e) {
                    checkLog.setResult(FAILED);
                    LOGGER.info(e.getMessage(), e);
                }
                logs.add(checkLog);
            });
            LOGGER.info("End syncing relevance.");
        }

        /**
         * 修复应用服务和环境的状态字段
         */
        private void syncEnvAndAppServiceStatus() {
            LOGGER.info("Start syncing status.");
            // 为应用服务的 `is_failed` 字段在迁移数据中修复 null 值为0
            appServiceMapper.updateIsFailedNullToFalse();

            // 将`is_failed`为1的值的应用服务和环境的纪录的`is_synchro`字段设为1
            appServiceMapper.updateIsSynchroToTrueWhenFailed();
            devopsEnvironmentMapper.updateIsSynchroToTrueWhenFailed();

            // 将`is_active` 为 null 的应用服务和环境纪录该字段设为 1(true)
            appServiceMapper.updateIsActiveNullToTrue();
            devopsEnvironmentMapper.updateIsActiveNullToTrue();
            LOGGER.info("End syncing status.");
        }

        private void syncAppShare(List<CheckLog> logs) {
            DevopsCheckLogDTO devopsCheckLogDTO = new DevopsCheckLogDTO();
            devopsCheckLogDTO.setLog("sync appService share success!");
            if (devopsCheckLogMapper.select(devopsCheckLogDTO).size() > 0) {
                LOGGER.info("It's been synchronized appService share.");
                return;
            }
            LOGGER.info("delete application market data.");
            applicationShareMapper.deleteAll();
            LOGGER.info("insert application share rule.");
            appServiceVersionMapper.selectAll().stream()
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
            devopsCheckLogMapper.insert(devopsCheckLogDTO);
            LOGGER.info("sync appService share success!");
            appServiceVersionMapper.updatePublishTime();
        }


        /**
         * 迁移部署纪录数据，已经完成容错处理
         */
        private void syncDeployRecord(List<CheckLog> checkLogs) {
            LOGGER.info("修复部署记录数据开始。此过程耗时稍长");
            // 只查出数据库中需要修复的手动部署实例的记录
            List<DevopsDeployRecordDTO> manualRecords = devopsEnvCommandMapper.listAllInstanceCommandToMigrate()
                    .stream()
                    .map(devopsEnvCommandDTO -> new DevopsDeployRecordDTO(devopsEnvCommandDTO.getProjectId(), "manual", devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getEnvId().toString(), devopsEnvCommandDTO.getCreationDate()))
                    .collect(Collectors.toList());
            LOGGER.info("共{}条手动部署的纪录需要迁移。", manualRecords.size());


            // 只查出数据库中需要修复的自动部署的纪录
            List<DevopsDeployRecordDTO> autoRecords = pipelineRecordMapper.listAllPipelineRecordToMigrate()
                    .stream()
                    .map(pipelineRecordDTO -> new DevopsDeployRecordDTO(pipelineRecordDTO.getProjectId(), "auto", pipelineRecordDTO.getId(), pipelineRecordDTO.getEnv(), pipelineRecordDTO.getCreationDate()))
                    .collect(Collectors.toList());
            LOGGER.info("共{}条自动部署的纪录需要迁移。", autoRecords.size());

            // 所有待插入的纪录
            manualRecords.addAll(autoRecords);
            manualRecords = manualRecords.stream().sorted(Comparator.comparing(DevopsDeployRecordDTO::getDeployTime)).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(manualRecords)) {
                devopsDeployRecordMapper.batchInsertSelective(manualRecords);
            }

            LOGGER.info("修复部署记录数据结束");
        }

        private <T> List<T> peekListSize(List<T> objects, String message) {
            LOGGER.info(message, objects.size());
            return objects;
        }

        private void syncClusterAndCertifications(List<CheckLog> checkLogs) {
            LOGGER.info("开始迁移集群和证书到项目下!");
            Map<Long, List<DevopsClusterDTO>> clusters = peekListSize(devopsClusterMapper.listAllClustersToMigrate(),
                    "There are {} clusters to be migrated.")
                    .stream()
                    .collect(Collectors.groupingBy(DevopsClusterDTO::getOrganizationId));

            Map<Long, List<CertificationDTO>> orgCertifications = peekListSize(devopsCertificationMapper.listAllOrgCertificationToMigrate(), "There are {} certifications to be migrated.")
                    .stream()
                    .collect(Collectors.groupingBy(CertificationDTO::getOrganizationId));

            Set<Long> allOrgIds = new HashSet<>(clusters.keySet());
            allOrgIds.addAll(orgCertifications.keySet());
            LOGGER.info("The number of organizations involved is {}", allOrgIds.size());

            // 项目所有者的角色id
            Long projectOwnerId = baseServiceClientOperator.queryRoleIdByCode(PROJECT_OWNER);

            allOrgIds.forEach(organizationId -> {
                ProjectDTO projectDTO = queryOrCreateMigrationProject(organizationId, projectOwnerId);
                LOGGER.info("迁移id为{}的组织下的证书和集群到id为{}的项目下.", organizationId, projectDTO.getId());

                // 迁移集群
                if (clusters.containsKey(organizationId)) {
                    clusters.get(organizationId).forEach(cluster -> {
                        LOGGER.info("Sync cluster migration to the project,clusterId: {}, organizationId: {}", cluster.getId(), organizationId);
                        CheckLog checkLog = new CheckLog();
                        checkLog.setContent(String.format(
                                "Sync cluster migration to the project,clusterId: %s, organizationId: %s", cluster.getId(), organizationId));
                        if (projectDTO != null) {
                            // 如果集群对应的项目id已经是将要设置的项目id，跳过
                            if (Objects.equals(cluster.getProjectId(), projectDTO.getId())) {
                                return;
                            }

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
                        LOGGER.info("Migrate organization certification to the project, org-cert-id: {}, organizationId: {}", cert.getId(), organizationId);
                        CheckLog checkLog = new CheckLog();
                        checkLog.setContent(String.format("Migrate organization certification to the project, org-cert-id: %s, organizationId: %s", cert.getId(), organizationId));
                        if (projectDTO != null) {
                            // 如果证书对应的项目id已经是将要设置的项目id，跳过
                            if (Objects.equals(cert.getProjectId(), projectDTO.getId())) {
                                return;
                            }

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

    /**
     * 查询或创建用于迁移数据的项目
     *
     * @param organizationId     组织id
     * @param projectOwnerRoleId 项目所有者这个角色的id
     * @return 项目信息
     */
    private ProjectDTO queryOrCreateMigrationProject(Long organizationId, Long projectOwnerRoleId) {
        final String migrationProjectName = "默认运维项目";
        final String migrationProjectCode = "def-ops-proj";
        final String category = "GENERAL";

        ProjectDTO projectDTO = baseServiceClientOperator.queryProjectByCodeAndOrganizationId(migrationProjectCode, organizationId);

        // 如果不存在，则创建组织下的项目
        if (projectDTO == null) {
            ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO();
            projectCreateDTO.setName(migrationProjectName);
            projectCreateDTO.setCode(migrationProjectCode);
            projectCreateDTO.setCategory(category);
            projectCreateDTO.setOrganizationId(organizationId);

            projectDTO = baseServiceClientOperator.createProject(organizationId, projectCreateDTO);
            LOGGER.info("已创建项目，id为{}", projectDTO.getId());
        } else {
            LOGGER.info("查询当前组织下已有code为{}的项目，id为{}，该组织下的证书和集群将迁移至该项目", migrationProjectCode, projectDTO.getId());
        }

        if (!hasOwners(projectDTO.getId(), projectOwnerRoleId)) {
            addDefaultProjectOwners(organizationId, projectDTO.getId(), projectOwnerRoleId);
        }

        return projectDTO;
    }

    /**
     * 项目是否已有项目所有者
     *
     * @param projectId   项目id
     * @param ownerRoleId 项目所有者角色id
     * @return true 有，反之没有
     */
    private boolean hasOwners(Long projectId, Long ownerRoleId) {
        return !ObjectUtils.isEmpty(baseServiceClientOperator.pagingQueryUsersByRoleIdOnProjectLevel(new PageRequest(1, 1), new RoleAssignmentSearchVO(), ownerRoleId,
                projectId, true).getList());
    }

    private void addDefaultProjectOwners(Long organizationId, Long projectId, Long projectOwnerRoleId) {
        // 查询所有组织的管理员
        List<IamUserDTO> organizationOwners = baseServiceClientOperator.listAllOrganizationOwners(organizationId);

        if (ObjectUtils.isEmpty(organizationOwners)) {
            LOGGER.warn("There is no organization administrators in the organization with id {}, therefore it's not able to add project owners for project with id {}", organizationId, projectId);
            return;
        }

        // 分配权限
        baseServiceClientOperator.assignProjectOwnerForUsersInProject(projectId, organizationOwners.stream().map(IamUserDTO::getId).collect(Collectors.toSet()), projectOwnerRoleId);
        LOGGER.info("Successfully add organization administrators to this project with id {}", projectId);
    }
}
