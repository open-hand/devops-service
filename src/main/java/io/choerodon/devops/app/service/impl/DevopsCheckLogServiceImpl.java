package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hzero.core.base.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.pipeline.WarningSettingVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.enums.test.ApiTestTaskType;
import io.choerodon.devops.infra.feign.RdupmClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCheckLogMapper;
import io.choerodon.devops.infra.mapper.DevopsCiCdPipelineMapper;
import io.choerodon.devops.infra.mapper.DevopsCiContentMapper;
import io.choerodon.devops.infra.mapper.DevopsCiJobMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;


@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);

    public static final String FIX_ENV_DATA = "fixEnvAppData";
    public static final String FIX_APP_CENTER_DATA = "fixAppCenterData";
    public static final String FIX_PIPELINE_DATA = "fixPipelineData";
    private static final String PIPELINE_CONTENT_FIX = "pipelineContentFix";
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Value("${nexus.proxy.url:nexus.proxy.url}")
    private String nexusProxyUrl;

    @Value("${nexus.default.url:nexus.default.url}")
    private String nexusDefaultUrl;

    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;
    @Autowired
    private DevopsCdJobService devopsCdJobService;
    @Autowired
    private DevopsCiJobService devopsCiJobService;
    @Autowired
    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
    @Autowired
    private DevopsCiContentMapper devopsCiContentMapper;
    @Autowired
    private RdupmClient rdupmClient;
    @Autowired
    private DevopsCdApiTestInfoService devopsCdApiTestInfoService;
    @Autowired
    private DevopsCiStepOperator devopsCiStepOperator;
    @Autowired
    private DevopsCiJobMapper devopsCiJobMapper;
    @Autowired
    private DevopsCiStepService devopsCiStepService;


    @Override
    public void checkLog(String task) {
        DevopsCheckLogDTO devopsCheckLogDTO = new DevopsCheckLogDTO();
        devopsCheckLogDTO.setLog(task);
        DevopsCheckLogDTO existDevopsCheckLogDTO = devopsCheckLogMapper.selectOne(devopsCheckLogDTO);
        if (existDevopsCheckLogDTO != null) {
            LOGGER.info("fix data task {} has already been executed", task);
            return;
        }
        devopsCheckLogDTO.setBeginCheckDate(new Date());
        switch (task) {
            case FIX_APP_CENTER_DATA:
                devopsDeployAppCenterService.fixData();
                fixPipelineCdDeployData();
                break;
            case PIPELINE_CONTENT_FIX:
                pipelineContentFix();
                devopsCiPipelineDataFix();
                break;
            case FIX_PIPELINE_DATA:
                pipelineDataFix();
                break;
            default:
                LOGGER.info("version not matched");
                return;
        }
        devopsCheckLogDTO.setLog(task);
        devopsCheckLogDTO.setEndCheckDate(new Date());
        devopsCheckLogMapper.insert(devopsCheckLogDTO);
    }

    /**
     * 修复ci流水线数据
     */
    public void devopsCiPipelineDataFix() {

        fixBuildJob();
        fixSonarJob();
        fixChartJob();
        fixCustomJob();
//        List<DevopsCiJobDTO> devopsCiJobDTOList = devopsCiJobMapper.listOldDataByType();
//        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Start fix pipeline ci job data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//        Set<Long> errorJobIds = new HashSet<>();
//        for (DevopsCiJobDTO devopsCiJobDTO : devopsCiJobDTOList) {
//            try {
//                if (devopsCiJobDTO != null) {
//                    CiCdPipelineVO ciCdPipelineVO = devopsCiCdPipelineMapper.queryById(devopsCiJobDTO.getCiPipelineId());
//                    Long devopsCiJobId = devopsCiJobDTO.getId();
//                    Long projectId = ciCdPipelineVO.getProjectId();
//
//                    // 需要修复的内容
//                    // 1. job的所属分组信息
//                    // 将构建任务拆分为单步骤的任务
//                    List<DevopsCiStepVO> devopsCiStepVOList = new ArrayList<>();
//                    if (JobTypeEnum.BUILD.value().equals(devopsCiJobDTO.getType())) {
//                        CiConfigVO ciConfigVO = JSONObject.parseObject(devopsCiJobDTO.getMetadata(), CiConfigVO.class);
//                        List<CiConfigTemplateVO> config = ciConfigVO.getConfig();
//
//
//                        for (CiConfigTemplateVO ciConfigTemplateVO : config) {
//                            if (CiJobScriptTypeEnum.NPM.getType().equals(ciConfigTemplateVO.getType())) {
//                                DevopsCiStepVO devopsCiStepVO = new DevopsCiStepVO();
//                                devopsCiStepVO.setDevopsCiJobId(devopsCiJobId);
//                                devopsCiStepVO.setName("Npm构建");
//                                devopsCiStepVO.setSequence(ciConfigTemplateVO.getSequence());
//                                devopsCiStepVO.setType(DevopsCiStepTypeEnum.NPM_BUILD.value());
//                                devopsCiStepVO.setScript(ciConfigTemplateVO.getScript());
//
//                                devopsCiStepVOList.add(devopsCiStepVO);
//
//                                devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.BUILD.value());
//                            } else if (CiJobScriptTypeEnum.MAVEN.getType().equals(ciConfigTemplateVO.getType())) {
//                                DevopsCiStepVO mavenStep = new DevopsCiStepVO();
//                                mavenStep.setDevopsCiJobId(devopsCiJobId);
//                                mavenStep.setName("Maven构建");
//                                mavenStep.setSequence(ciConfigTemplateVO.getSequence());
//                                mavenStep.setType(DevopsCiStepTypeEnum.MAVEN_BUILD.value());
//                                mavenStep.setScript(ciConfigTemplateVO.getScript());
//                                DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = new DevopsCiMavenBuildConfigVO();
//                                devopsCiMavenBuildConfigVO.setRepos(ciConfigTemplateVO.getRepos());
//                                devopsCiMavenBuildConfigVO.setMavenSettings(ciConfigTemplateVO.getMavenSettings());
//                                devopsCiMavenBuildConfigVO.setNexusMavenRepoIds(ciConfigTemplateVO.getNexusMavenRepoIds());
//                                mavenStep.setMavenBuildConfig(devopsCiMavenBuildConfigVO);
//
//                                devopsCiStepVOList.add(mavenStep);
//
//                                devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.BUILD.value());
//
//                            } else if (CiJobScriptTypeEnum.DOCKER.getType().equals(ciConfigTemplateVO.getType())) {
//                                DevopsCiStepVO dockerStep = new DevopsCiStepVO();
//                                dockerStep.setDevopsCiJobId(devopsCiJobId);
//                                dockerStep.setName("Docker构建");
//                                dockerStep.setSequence(ciConfigTemplateVO.getSequence());
//                                dockerStep.setType(DevopsCiStepTypeEnum.DOCKER_BUILD.value());
//                                dockerStep.setScript(ciConfigTemplateVO.getScript());
//
//                                DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = ConvertUtils.convertObject(ciConfigTemplateVO, DevopsCiDockerBuildConfigDTO.class);
//                                SecurityConditionConfigVO securityCondition = ciConfigTemplateVO.getSecurityCondition();
//
//                                devopsCiDockerBuildConfigDTO.setSeverity(securityCondition.getLevel());
//                                devopsCiDockerBuildConfigDTO.setSecurityControlConditions(securityCondition.getSymbol());
//                                devopsCiDockerBuildConfigDTO.setVulnerabilityCount(securityCondition.getCondition());
//                                dockerStep.setDockerBuildConfig(devopsCiDockerBuildConfigDTO);
//                                devopsCiStepVOList.add(dockerStep);
//
//                                devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.DOCKER_BUILD.value());
//                            } else if (CiJobScriptTypeEnum.UPLOAD_JAR.getType().equals(ciConfigTemplateVO.getType())) {
//                                DevopsCiStepVO uploadJar = new DevopsCiStepVO();
//                                uploadJar.setDevopsCiJobId(devopsCiJobId);
//                                uploadJar.setName("上传Jar包至制品库");
//                                uploadJar.setSequence(ciConfigTemplateVO.getSequence());
//                                uploadJar.setType(DevopsCiStepTypeEnum.UPLOAD_JAR.value());
//                                uploadJar.setScript(ciConfigTemplateVO.getScript());
//
//                                DevopsCiMavenPublishConfigVO uploadJarConfig = new DevopsCiMavenPublishConfigVO();
//                                uploadJarConfig.setRepos(ciConfigTemplateVO.getRepos());
//                                uploadJarConfig.setMavenSettings(ciConfigTemplateVO.getMavenSettings());
//                                uploadJarConfig.setNexusMavenRepoIds(ciConfigTemplateVO.getNexusMavenRepoIds());
//                                uploadJarConfig.setNexusRepoId(ciConfigTemplateVO.getMavenDeployRepoSettings().getNexusRepoIds());
//                                uploadJar.setMavenPublishConfig(uploadJarConfig);
//
//                                devopsCiStepVOList.add(uploadJar);
//
//                                devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.BUILD.value());
//                            } else if (CiJobScriptTypeEnum.MAVEN_DEPLOY.getType().equals(ciConfigTemplateVO.getType())) {
//                                DevopsCiStepVO mavenDeploy = new DevopsCiStepVO();
//                                mavenDeploy.setDevopsCiJobId(devopsCiJobId);
//                                mavenDeploy.setName("Maven发布");
//                                mavenDeploy.setSequence(ciConfigTemplateVO.getSequence());
//                                mavenDeploy.setType(DevopsCiStepTypeEnum.MAVEN_BUILD.value());
//                                mavenDeploy.setScript(ciConfigTemplateVO.getScript());
//
//                                DevopsCiMavenPublishConfigVO devopsCiMavenPublishConfigVO = new DevopsCiMavenPublishConfigVO();
//                                devopsCiMavenPublishConfigVO.setRepos(ciConfigTemplateVO.getRepos());
//                                devopsCiMavenPublishConfigVO.setMavenSettings(ciConfigTemplateVO.getMavenSettings());
//                                devopsCiMavenPublishConfigVO.setNexusMavenRepoIds(ciConfigTemplateVO.getNexusMavenRepoIds());
//                                devopsCiMavenPublishConfigVO.setNexusRepoId(ciConfigTemplateVO.getMavenDeployRepoSettings().getNexusRepoIds());
//                                mavenDeploy.setMavenPublishConfig(devopsCiMavenPublishConfigVO);
//
//                                devopsCiStepVOList.add(mavenDeploy);
//
//                                devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.BUILD.value());
//                            }
//                        }
//                    } else if (JobTypeEnum.SONAR.value().equals(devopsCiJobDTO.getType())) {
//                        SonarQubeConfigVO sonarQubeConfigVO = JsonHelper.unmarshalByJackson(devopsCiJobDTO.getMetadata(), SonarQubeConfigVO.class);
//                        DevopsCiStepVO devopsCiStepVO = new DevopsCiStepVO();
//                        devopsCiStepVO.setDevopsCiJobId(devopsCiJobId);
//                        devopsCiStepVO.setName("SonarQube代码检查");
//                        devopsCiStepVO.setSequence(0L);
//                        devopsCiStepVO.setType(DevopsCiStepTypeEnum.SONAR.value());
//                        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = ConvertUtils.convertObject(sonarQubeConfigVO, DevopsCiSonarConfigDTO.class);
//                        devopsCiStepVO.setSonarConfig(devopsCiSonarConfigDTO);
//                        devopsCiStepVOList.add(devopsCiStepVO);
//
//                        devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.CODE_SCAN.value());
//
//                    } else if (JobTypeEnum.CHART.value().equals(devopsCiJobDTO.getType())) {
//                        DevopsCiStepVO devopsCiStepVO = new DevopsCiStepVO();
//                        devopsCiStepVO.setDevopsCiJobId(devopsCiJobId);
//                        devopsCiStepVO.setName("上传Chart至猪齿鱼");
//                        devopsCiStepVO.setSequence(0L);
//                        devopsCiStepVO.setType(DevopsCiStepTypeEnum.UPLOAD_CHART.value());
//                        devopsCiStepVOList.add(devopsCiStepVO);
//
//                        devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.BUILD.value());
//                    } else if (JobTypeEnum.CUSTOM.value().equals(devopsCiJobDTO.getType())) {
//                        devopsCiJobDTO.setScript(devopsCiJobDTO.getMetadata());
//
//                        devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.OTHER.value());
//                    }
//
//                    // 更新job step信息
//                    if (!CollectionUtils.isEmpty(devopsCiStepVOList)) {
//                        for (DevopsCiStepVO devopsCiStepVO : devopsCiStepVOList) {
//                            AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandler(devopsCiStepVO.getType());
//                            handler.save(projectId, devopsCiJobId, devopsCiStepVO);
//                        }
//                    }
//                    // 更新job信息
//                    devopsCiJobMapper.updateByPrimaryKeySelective(devopsCiJobDTO);
//                }
//            } catch (Exception e) {
//                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Fix pipeline ci job data : {} failed! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsCiJobDTO.getId());
//                errorJobIds.add(devopsCiJobDTO.getId());
//            }
//        }

    }

    private void fixCustomJob() {
        List<DevopsCiJobDTO> devopsCiJobDTOList = devopsCiJobMapper.listOldDataByType(JobTypeEnum.CUSTOM.value());
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Start fix pipeline ci custom job data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        Set<Long> errorJobIds = new HashSet<>();
        for (DevopsCiJobDTO devopsCiJobDTO : devopsCiJobDTOList) {
            try {
                if (devopsCiJobDTO != null) {
                    CiCdPipelineVO ciCdPipelineVO = devopsCiCdPipelineMapper.queryById(devopsCiJobDTO.getCiPipelineId());
                    Long devopsCiJobId = devopsCiJobDTO.getId();
                    Long projectId = ciCdPipelineVO.getProjectId();

                    // 需要修复的内容
                    // 1. job的所属分组信息
                    // 将构建任务拆分为单步骤的任务
                    if (JobTypeEnum.CUSTOM.value().equals(devopsCiJobDTO.getOldType())) {
                        devopsCiJobDTO.setScript(devopsCiJobDTO.getMetadata());
                        devopsCiJobDTO.setType(CiJobTypeEnum.CUSTOM.value());

                        devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.OTHER.value());
                    }
                    // 更新job信息
                    devopsCiJobMapper.updateByPrimaryKeySelective(devopsCiJobDTO);
                }
            } catch (Exception e) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Fix pipeline ci custom job data : {} failed! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsCiJobDTO.getId());
                errorJobIds.add(devopsCiJobDTO.getId());
            }
        }
        if (CollectionUtils.isEmpty(errorJobIds)) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline ci custom job data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline ci custom job data, but exist errors! Failed job ids is : {}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(errorJobIds));
            }
        }
    }

    private void fixChartJob() {
        List<DevopsCiJobDTO> devopsCiJobDTOList = devopsCiJobMapper.listOldDataByType(JobTypeEnum.CHART.value());
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Start fix pipeline ci chart job data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        Set<Long> errorJobIds = new HashSet<>();
        for (DevopsCiJobDTO devopsCiJobDTO : devopsCiJobDTOList) {
            try {
                if (devopsCiJobDTO != null) {
                    CiCdPipelineVO ciCdPipelineVO = devopsCiCdPipelineMapper.queryById(devopsCiJobDTO.getCiPipelineId());
                    Long devopsCiJobId = devopsCiJobDTO.getId();
                    Long projectId = ciCdPipelineVO.getProjectId();

                    // 需要修复的内容
                    // 1. job的所属分组信息
                    // 将构建任务拆分为单步骤的任务
                    List<DevopsCiStepVO> devopsCiStepVOList = new ArrayList<>();
                    if (JobTypeEnum.CHART.value().equals(devopsCiJobDTO.getOldType())) {
                        DevopsCiStepVO devopsCiStepVO = new DevopsCiStepVO();
                        devopsCiStepVO.setDevopsCiJobId(devopsCiJobId);
                        devopsCiStepVO.setName("上传Chart至猪齿鱼");
                        devopsCiStepVO.setSequence(0L);
                        devopsCiStepVO.setType(DevopsCiStepTypeEnum.UPLOAD_CHART.value());
                        devopsCiStepVOList.add(devopsCiStepVO);

                        devopsCiJobDTO.setType(CiJobTypeEnum.NORMAL.value());
                        devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.BUILD.value());
                    }
                    // 保证可重复执行，报错step信息前先删除旧数据
                    List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepService.listByJobId(devopsCiJobId);
                    if (!CollectionUtils.isEmpty(devopsCiStepDTOS)) {
                        Map<String, List<DevopsCiStepDTO>> stepMap = devopsCiStepDTOS.stream().collect(Collectors.groupingBy(DevopsCiStepDTO::getType));
                        // 按类型级联删除
                        stepMap.forEach((k, v) -> {
                            AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandler(k);
                            handler.batchDeleteCascade(v);
                        });
                    }
                    // 更新job step信息
                    if (!CollectionUtils.isEmpty(devopsCiStepVOList)) {
                        for (DevopsCiStepVO devopsCiStepVO : devopsCiStepVOList) {
                            AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandler(devopsCiStepVO.getType());
                            handler.save(projectId, devopsCiJobId, devopsCiStepVO);
                        }
                    }
                    // 更新job信息
                    devopsCiJobMapper.updateByPrimaryKeySelective(devopsCiJobDTO);
                }
            } catch (Exception e) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Fix pipeline ci chart job data : {} failed! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsCiJobDTO.getId());
                errorJobIds.add(devopsCiJobDTO.getId());
            }
        }
        if (CollectionUtils.isEmpty(errorJobIds)) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline ci chart job data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline ci chart job data, but exist errors! Failed job ids is : {}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(errorJobIds));
            }
        }
    }

    private void fixSonarJob() {
        List<DevopsCiJobDTO> devopsCiJobDTOList = devopsCiJobMapper.listOldDataByType(JobTypeEnum.SONAR.value());
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Start fix pipeline ci sonar job data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        Set<Long> errorJobIds = new HashSet<>();
        for (DevopsCiJobDTO devopsCiJobDTO : devopsCiJobDTOList) {
            try {
                if (devopsCiJobDTO != null) {
                    CiCdPipelineVO ciCdPipelineVO = devopsCiCdPipelineMapper.queryById(devopsCiJobDTO.getCiPipelineId());
                    Long devopsCiJobId = devopsCiJobDTO.getId();
                    Long projectId = ciCdPipelineVO.getProjectId();

                    // 需要修复的内容
                    // 1. job的所属分组信息
                    // 将构建任务拆分为单步骤的任务
                    List<DevopsCiStepVO> devopsCiStepVOList = new ArrayList<>();
                    if (JobTypeEnum.SONAR.value().equals(devopsCiJobDTO.getOldType())) {
                        SonarQubeConfigVO sonarQubeConfigVO = JsonHelper.unmarshalByJackson(devopsCiJobDTO.getMetadata(), SonarQubeConfigVO.class);
                        DevopsCiStepVO devopsCiStepVO = new DevopsCiStepVO();
                        devopsCiStepVO.setDevopsCiJobId(devopsCiJobId);
                        devopsCiStepVO.setName("SonarQube代码检查");
                        devopsCiStepVO.setSequence(0L);
                        devopsCiStepVO.setType(DevopsCiStepTypeEnum.SONAR.value());
                        DevopsCiSonarConfigDTO devopsCiSonarConfigDTO = ConvertUtils.convertObject(sonarQubeConfigVO, DevopsCiSonarConfigDTO.class);
                        devopsCiStepVO.setSonarConfig(devopsCiSonarConfigDTO);
                        devopsCiStepVOList.add(devopsCiStepVO);

                        devopsCiJobDTO.setType(CiJobTypeEnum.NORMAL.value());
                        devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.CODE_SCAN.value());

                    }

                    // 保证可重复执行，报错step信息前先删除旧数据
                    List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepService.listByJobId(devopsCiJobId);
                    if (!CollectionUtils.isEmpty(devopsCiStepDTOS)) {
                        Map<String, List<DevopsCiStepDTO>> stepMap = devopsCiStepDTOS.stream().collect(Collectors.groupingBy(DevopsCiStepDTO::getType));
                        // 按类型级联删除
                        stepMap.forEach((k, v) -> {
                            AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandler(k);
                            handler.batchDeleteCascade(v);
                        });
                    }

                    // 更新job step信息
                    if (!CollectionUtils.isEmpty(devopsCiStepVOList)) {
                        for (DevopsCiStepVO devopsCiStepVO : devopsCiStepVOList) {
                            AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandler(devopsCiStepVO.getType());
                            handler.save(projectId, devopsCiJobId, devopsCiStepVO);
                        }
                    }
                    // 更新job信息
                    devopsCiJobMapper.updateByPrimaryKeySelective(devopsCiJobDTO);
                }
            } catch (Exception e) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Fix pipeline ci sonar job data : {} failed! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsCiJobDTO.getId());
                errorJobIds.add(devopsCiJobDTO.getId());
            }
        }

        if (CollectionUtils.isEmpty(errorJobIds)) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline ci sonar job data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline ci sonar job data, but exist errors! Failed job ids is : {}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(errorJobIds));
            }
        }
    }

    private void fixBuildJob() {
        List<DevopsCiJobDTO> devopsCiJobDTOList = devopsCiJobMapper.listOldDataByType(JobTypeEnum.BUILD.value());
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Start fix pipeline ci build job data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        Set<Long> errorJobIds = new HashSet<>();
        for (DevopsCiJobDTO devopsCiJobDTO : devopsCiJobDTOList) {
            try {
                if (devopsCiJobDTO != null) {
                    CiCdPipelineVO ciCdPipelineVO = devopsCiCdPipelineMapper.queryById(devopsCiJobDTO.getCiPipelineId());
                    Long devopsCiJobId = devopsCiJobDTO.getId();
                    Long projectId = ciCdPipelineVO.getProjectId();

                    // 需要修复的内容
                    // 1. job的所属分组信息
                    // 将构建任务拆分为单步骤的任务
                    List<DevopsCiStepVO> devopsCiStepVOList = new ArrayList<>();
                    if (JobTypeEnum.BUILD.value().equals(devopsCiJobDTO.getOldType())) {
                        CiConfigVO ciConfigVO = JSONObject.parseObject(devopsCiJobDTO.getMetadata(), CiConfigVO.class);
                        List<CiConfigTemplateVO> config = ciConfigVO.getConfig();

                        devopsCiJobDTO.setType(CiJobTypeEnum.NORMAL.value());

                        for (CiConfigTemplateVO ciConfigTemplateVO : config) {
                            if (CiJobScriptTypeEnum.NPM.getType().equals(ciConfigTemplateVO.getType().toLowerCase())) {
                                DevopsCiStepVO devopsCiStepVO = new DevopsCiStepVO();
                                devopsCiStepVO.setDevopsCiJobId(devopsCiJobId);
                                devopsCiStepVO.setName("Npm构建");
                                devopsCiStepVO.setSequence(ciConfigTemplateVO.getSequence());
                                devopsCiStepVO.setType(DevopsCiStepTypeEnum.NPM_BUILD.value());
                                devopsCiStepVO.setScript(ciConfigTemplateVO.getScript());

                                devopsCiStepVOList.add(devopsCiStepVO);

                                devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.BUILD.value());
                            } else if (CiJobScriptTypeEnum.MAVEN.getType().equals(ciConfigTemplateVO.getType().toLowerCase())) {
                                DevopsCiStepVO mavenStep = new DevopsCiStepVO();
                                mavenStep.setDevopsCiJobId(devopsCiJobId);
                                mavenStep.setName("Maven构建");
                                mavenStep.setSequence(ciConfigTemplateVO.getSequence());
                                mavenStep.setType(DevopsCiStepTypeEnum.MAVEN_BUILD.value());
                                mavenStep.setScript(ciConfigTemplateVO.getScript());
                                DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = new DevopsCiMavenBuildConfigVO();
                                devopsCiMavenBuildConfigVO.setRepos(ciConfigTemplateVO.getRepos());
                                devopsCiMavenBuildConfigVO.setMavenSettings(ciConfigTemplateVO.getMavenSettings());
                                devopsCiMavenBuildConfigVO.setNexusMavenRepoIds(ciConfigTemplateVO.getNexusMavenRepoIds());
                                mavenStep.setMavenBuildConfig(devopsCiMavenBuildConfigVO);

                                devopsCiStepVOList.add(mavenStep);

                                devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.BUILD.value());

                            } else if (CiJobScriptTypeEnum.DOCKER.getType().equals(ciConfigTemplateVO.getType().toLowerCase())) {
                                DevopsCiStepVO dockerStep = new DevopsCiStepVO();
                                dockerStep.setDevopsCiJobId(devopsCiJobId);
                                dockerStep.setName("Docker构建");
                                dockerStep.setSequence(ciConfigTemplateVO.getSequence());
                                dockerStep.setType(DevopsCiStepTypeEnum.DOCKER_BUILD.value());
                                dockerStep.setScript(ciConfigTemplateVO.getScript());

                                DevopsCiDockerBuildConfigDTO devopsCiDockerBuildConfigDTO = ConvertUtils.convertObject(ciConfigTemplateVO, DevopsCiDockerBuildConfigDTO.class);
                                SecurityConditionConfigVO securityCondition = ciConfigTemplateVO.getSecurityCondition();
                                if (securityCondition != null) {
                                    devopsCiDockerBuildConfigDTO.setSeverity(securityCondition.getLevel());
                                    devopsCiDockerBuildConfigDTO.setSecurityControlConditions(securityCondition.getSymbol());
                                    devopsCiDockerBuildConfigDTO.setVulnerabilityCount(securityCondition.getCondition());
                                }
                                devopsCiDockerBuildConfigDTO.setSecurityControl(securityCondition != null);
                                devopsCiDockerBuildConfigDTO.setEnableDockerTlsVerify(ciConfigTemplateVO.getSkipDockerTlsVerify() == null || ciConfigTemplateVO.getSkipDockerTlsVerify());
                                devopsCiDockerBuildConfigDTO.setImageScan(ciConfigTemplateVO.getImageScan() != null ? ciConfigTemplateVO.getImageScan() : false);
                                dockerStep.setDockerBuildConfig(devopsCiDockerBuildConfigDTO);

                                devopsCiStepVOList.add(dockerStep);

                                devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.DOCKER_BUILD.value());
                            } else if (CiJobScriptTypeEnum.UPLOAD_JAR.getType().equals(ciConfigTemplateVO.getType().toLowerCase())) {
                                DevopsCiStepVO uploadJar = new DevopsCiStepVO();
                                uploadJar.setDevopsCiJobId(devopsCiJobId);
                                uploadJar.setName("上传Jar包至制品库");
                                uploadJar.setSequence(ciConfigTemplateVO.getSequence());
                                uploadJar.setType(DevopsCiStepTypeEnum.UPLOAD_JAR.value());
                                uploadJar.setScript(ciConfigTemplateVO.getScript());

                                DevopsCiMavenPublishConfigVO uploadJarConfig = new DevopsCiMavenPublishConfigVO();
                                uploadJarConfig.setRepos(ciConfigTemplateVO.getRepos());
                                uploadJarConfig.setMavenSettings(ciConfigTemplateVO.getMavenSettings());
                                uploadJarConfig.setNexusMavenRepoIds(ciConfigTemplateVO.getNexusMavenRepoIds());
                                uploadJarConfig.setNexusRepoId(ciConfigTemplateVO.getMavenDeployRepoSettings().getNexusRepoIds());
                                uploadJar.setMavenPublishConfig(uploadJarConfig);

                                devopsCiStepVOList.add(uploadJar);

                                devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.BUILD.value());
                            } else if (CiJobScriptTypeEnum.MAVEN_DEPLOY.getType().equals(ciConfigTemplateVO.getType().toLowerCase())) {
                                DevopsCiStepVO mavenDeploy = new DevopsCiStepVO();
                                mavenDeploy.setDevopsCiJobId(devopsCiJobId);
                                mavenDeploy.setName("Maven发布");
                                mavenDeploy.setSequence(ciConfigTemplateVO.getSequence());
                                mavenDeploy.setType(DevopsCiStepTypeEnum.MAVEN_BUILD.value());
                                mavenDeploy.setScript(ciConfigTemplateVO.getScript());

                                DevopsCiMavenPublishConfigVO devopsCiMavenPublishConfigVO = new DevopsCiMavenPublishConfigVO();
                                devopsCiMavenPublishConfigVO.setRepos(ciConfigTemplateVO.getRepos());
                                devopsCiMavenPublishConfigVO.setMavenSettings(ciConfigTemplateVO.getMavenSettings());
                                devopsCiMavenPublishConfigVO.setNexusMavenRepoIds(ciConfigTemplateVO.getNexusMavenRepoIds());
                                devopsCiMavenPublishConfigVO.setNexusRepoId(ciConfigTemplateVO.getMavenDeployRepoSettings().getNexusRepoIds());
                                mavenDeploy.setMavenPublishConfig(devopsCiMavenPublishConfigVO);

                                devopsCiStepVOList.add(mavenDeploy);

                                devopsCiJobDTO.setGroupType(CiTemplateJobGroupTypeEnum.BUILD.value());
                            }

                        }

                    }

                    // 保证可重复执行，报错step信息前先删除旧数据
                    List<DevopsCiStepDTO> devopsCiStepDTOS = devopsCiStepService.listByJobId(devopsCiJobId);
                    if (!CollectionUtils.isEmpty(devopsCiStepDTOS)) {
                        Map<String, List<DevopsCiStepDTO>> stepMap = devopsCiStepDTOS.stream().collect(Collectors.groupingBy(DevopsCiStepDTO::getType));
                        // 按类型级联删除
                        stepMap.forEach((k, v) -> {
                            AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandler(k);
                            handler.batchDeleteCascade(v);
                        });
                    }

                    // 更新job step信息
                    if (!CollectionUtils.isEmpty(devopsCiStepVOList)) {
                        for (DevopsCiStepVO devopsCiStepVO : devopsCiStepVOList) {
                            AbstractDevopsCiStepHandler handler = devopsCiStepOperator.getHandler(devopsCiStepVO.getType());
                            handler.save(projectId, devopsCiJobId, devopsCiStepVO);
                        }
                    }
                    // 更新job信息
                    devopsCiJobMapper.updateByPrimaryKeySelective(devopsCiJobDTO);
                }
            } catch (Exception e) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Fix pipeline ci build job data : {} failed! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsCiJobDTO.getId());
                errorJobIds.add(devopsCiJobDTO.getId());
            }
        }
        if (CollectionUtils.isEmpty(errorJobIds)) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline ci build job data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline ci build job data, but exist errors! Failed job ids is : {}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(errorJobIds));
            }
        }

    }

    private void pipelineDataFix() {
        List<DevopsCdJobDTO> devopsCdJobDTOS = devopsCdJobService.listByType(JobTypeEnum.CD_API_TEST);
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Start fix pipeline api test data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        Set<Long> errorJobIds = new HashSet<>();
        for (DevopsCdJobDTO devopsCdJobDTO : devopsCdJobDTOS) {
            try {
                if (devopsCdJobDTO != null) {
                    CdApiTestConfigVO cdApiTestConfigVO = JsonHelper.unmarshalByJackson(devopsCdJobDTO.getMetadata(), CdApiTestConfigVO.class);
                    DevopsCdApiTestInfoDTO devopsCdApiTestInfoDTO = ConvertUtils.convertObject(cdApiTestConfigVO, DevopsCdApiTestInfoDTO.class);
                    devopsCdApiTestInfoDTO.setTaskType(ApiTestTaskType.TASK.getValue());

                    WarningSettingVO warningSettingVO = cdApiTestConfigVO.getWarningSettingVO();
                    if (warningSettingVO != null) {
                        devopsCdApiTestInfoDTO.setEnableWarningSetting(warningSettingVO.getEnableWarningSetting());
                        devopsCdApiTestInfoDTO.setBlockAfterJob(warningSettingVO.getBlockAfterJob());
                        devopsCdApiTestInfoDTO.setSendEmail(warningSettingVO.getSendEmail());
                        devopsCdApiTestInfoDTO.setPerformThreshold(warningSettingVO.getPerformThreshold());
                        if (!CollectionUtils.isEmpty(warningSettingVO.getNotifyUserIds())) {
                            devopsCdApiTestInfoDTO.setNotifyUserIds(JsonHelper.marshalByJackson(warningSettingVO.getNotifyUserIds()));
                        }
                    }
                    devopsCdApiTestInfoService.baseCreate(devopsCdApiTestInfoDTO);
                    devopsCdJobDTO.setDeployInfoId(devopsCdApiTestInfoDTO.getId());
                    devopsCdJobService.baseUpdate(devopsCdJobDTO);
                }
            } catch (Exception e) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Fix pipeline api test data : {} failed! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsCdJobDTO.getId());
                errorJobIds.add(devopsCdJobDTO.getId());
            }

        }
        if (CollectionUtils.isEmpty(errorJobIds)) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline api test data! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline api test data, but exist errors! Failed job ids is : {}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(errorJobIds));
            }
        }
    }

    private void pipelineContentFix() {
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>start fix pipeline content >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
        //1.找出所有的注册组织，Saas组织
        //查询默认的nexus仓库id
        Long configId = null;
        ResponseEntity<NexusServerConfig> defaultMavenRepo = rdupmClient.getDefaultMavenRepo(BaseConstants.DEFAULT_TENANT_ID);
        if (defaultMavenRepo != null && defaultMavenRepo.getBody() != null) {
            NexusServerConfig defaultMavenRepoBody = defaultMavenRepo.getBody();
            configId = defaultMavenRepoBody.getConfigId();
        } else {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>nexus service config is null >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
        }
        if (configId == null) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>nexus service config is null >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
            return;
        }

        List<ExternalTenantVO> resultTenants = queryExternalTenantVOS();

        if (!CollectionUtils.isEmpty(resultTenants)) {
            Long finalConfigId = configId;
            resultTenants.forEach(externalTenantVO -> {
                List<ProjectDTO> projects = baseServiceClientOperator.listIamProjectByOrgId(externalTenantVO.getTenantId());
                if (!CollectionUtils.isEmpty(projects)) {
                    projects.forEach(projectDTO -> {
                        CiCdPipelineDTO ciCdPipelineDTO = new CiCdPipelineDTO();
                        ciCdPipelineDTO.setProjectId(projectDTO.getId());
                        List<CiCdPipelineDTO> ciCdPipelineDTOS = devopsCiCdPipelineMapper.select(ciCdPipelineDTO);
                        if (!CollectionUtils.isEmpty(ciCdPipelineDTOS)) {
                            Set<Long> ids = ciCdPipelineDTOS.stream().map(CiCdPipelineDTO::getId).collect(Collectors.toSet());
                            if (CollectionUtils.isEmpty(ids)) {
                                return;
                            }
                            devopsCiContentMapper.updateCiContent(ids, nexusDefaultUrl.trim(), nexusProxyUrl.trim() + "/v1/nexus/proxy/" + finalConfigId);
                        }
                    });
                }
            });
        }

        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>end fix pipeline content >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!");
    }


    private List<ExternalTenantVO> queryExternalTenantVOS() {
        List<String> saasLevels = Arrays.asList(SaasLevelEnum.FREE.name(), SaasLevelEnum.STANDARD.name(), SaasLevelEnum.SENIOR.name());
        List<ExternalTenantVO> saasTenants = baseServiceClientOperator.querySaasTenants(saasLevels);
        List<ExternalTenantVO> registerTenants = baseServiceClientOperator.queryRegisterTenant();
        List<ExternalTenantVO> resultTenants = new ArrayList<>();
        if (!CollectionUtils.isEmpty(saasTenants)) {
            resultTenants.addAll(saasTenants);
        }
        if (!CollectionUtils.isEmpty(registerTenants)) {
            resultTenants.addAll(registerTenants);
        }
        return resultTenants;
    }

    private void fixPipelineCdDeployData() {
        List<DevopsCdEnvDeployInfoDTO> devopsCdEnvDeployInfoDTOS = devopsCdEnvDeployInfoService.listAll();
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Start fix pipeline devopsCdEnvDeployInfoDTO! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        Set<Long> errorJobIds = new HashSet<>();
        for (DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO : devopsCdEnvDeployInfoDTOS) {
            try {
                if (devopsCdEnvDeployInfoDTO != null) {

                    DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = null;
                    // 实例id不为空就通过实例id查询应用
                    if (devopsCdEnvDeployInfoDTO.getInstanceId() != null) {
                        devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, devopsCdEnvDeployInfoDTO.getInstanceId());
                    } else {
                        // 实例id为空就通过环境id和实例名称查询应用
                        if (devopsCdEnvDeployInfoDTO.getEnvId() != null
                                && devopsCdEnvDeployInfoDTO.getInstanceName() != null) {
                            devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(devopsCdEnvDeployInfoDTO.getEnvId(), devopsCdEnvDeployInfoDTO.getInstanceName());
                        }
                    }
                    // 找到了关联的应用，设置关联应用id，流水线执行时走更新实例逻辑
                    if (DeployTypeEnum.UPDATE.value().equals(devopsCdEnvDeployInfoDTO.getDeployType())
                            && devopsDeployAppCenterEnvDTO != null) {
                        devopsCdEnvDeployInfoDTO.setAppCode(devopsDeployAppCenterEnvDTO.getCode());
                        devopsCdEnvDeployInfoDTO.setAppName(devopsDeployAppCenterEnvDTO.getName());
                        devopsCdEnvDeployInfoDTO.setAppId(devopsDeployAppCenterEnvDTO.getId());
                        devopsCdEnvDeployInfoDTO.setSkipCheckPermission(!devopsCdEnvDeployInfoDTO.getCheckEnvPermissionFlag());
                        devopsCdEnvDeployInfoService.update(devopsCdEnvDeployInfoDTO);
                    } else {
                        devopsCdEnvDeployInfoDTO.setAppName(devopsCdEnvDeployInfoDTO.getInstanceName());
                        devopsCdEnvDeployInfoDTO.setAppCode(devopsCdEnvDeployInfoDTO.getInstanceName());
                        devopsCdEnvDeployInfoDTO.setSkipCheckPermission(!devopsCdEnvDeployInfoDTO.getCheckEnvPermissionFlag());
                        devopsCdEnvDeployInfoService.update(devopsCdEnvDeployInfoDTO);
                    }
                }
            } catch (Exception e) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Fix pipeline devopsCdEnvDeployInfoDTO : {} failed! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsCdEnvDeployInfoDTO.getId());
                errorJobIds.add(devopsCdEnvDeployInfoDTO.getId());
            }

        }
        if (CollectionUtils.isEmpty(errorJobIds)) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline devopsCdEnvDeployInfoDTO! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline devopsCdEnvDeployInfoDTO, but exist errors! Failed devopsCdEnvDeployInfo ids is : {}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(errorJobIds));
            }
        }


    }
}
