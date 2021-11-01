package io.choerodon.devops.app.service.impl;

import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.ExternalTenantVO;
import io.choerodon.devops.api.vo.pipeline.DevopsDeployInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.JobTypeEnum;
import io.choerodon.devops.infra.enums.SaasLevelEnum;
import io.choerodon.devops.infra.enums.deploy.DeployTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCheckLogMapper;
import io.choerodon.devops.infra.mapper.DevopsCiCdPipelineMapper;
import io.choerodon.devops.infra.mapper.DevopsCiContentMapper;
import io.choerodon.devops.infra.util.JsonHelper;


@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);

    public static final String FIX_ENV_DATA = "fixEnvAppData";
    public static final String FIX_APP_CENTER_DATA = "fixAppCenterData";
    private static final String PIPELINE_CONTENT_FIX = "pipelineContentFix";
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Value("${nexus.proxy.url:nexus.proxy.url}")
    private String nexusProxyUrl;

    @Value("${nexus.default.url:nexus.default.url}")
    private String nexusDefaultUrl;

    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;
    @Autowired
    private DevopsEnvApplicationService devopsEnvApplicationService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;
    @Autowired
    private DevopsCdJobService devopsCdJobService;
    @Autowired
    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsCiCdPipelineMapper devopsCiCdPipelineMapper;
    @Autowired
    private DevopsCiContentMapper devopsCiContentMapper;


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
                break;
            default:
                LOGGER.info("version not matched");
                return;
        }
        devopsCheckLogDTO.setLog(task);
        devopsCheckLogDTO.setEndCheckDate(new Date());
        devopsCheckLogMapper.insert(devopsCheckLogDTO);
    }

    private void pipelineContentFix() {
        //1.找出所有的注册组织，Saas组织
        List<ExternalTenantVO> resultTenants = queryExternalTenantVOS();
        //2.找到组织下所有的项目
        List<ProjectDTO> projectDTOS = queryProjectDTOS(resultTenants);
        if (CollectionUtils.isEmpty(projectDTOS)) return;
        //3.找到项目下的所有流水线,nexus的地址替换为代理地址
        replaceCiFile(projectDTOS);

    }

    private void replaceCiFile(List<ProjectDTO> projectDTOS) {
        if (CollectionUtils.isEmpty(projectDTOS)) {
            LOGGER.info("saas and register tenant project is empty!");
            return;
        }
        projectDTOS.forEach(projectDTO -> {
            CiCdPipelineDTO ciCdPipelineDTO = new CiCdPipelineDTO();
            ciCdPipelineDTO.setProjectId(projectDTO.getId());
            List<CiCdPipelineDTO> ciCdPipelineDTOS = devopsCiCdPipelineMapper.select(ciCdPipelineDTO);
            if (!CollectionUtils.isEmpty(ciCdPipelineDTOS)) {
                Set<Long> ids = ciCdPipelineDTOS.stream().map(CiCdPipelineDTO::getId).collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(ids)) {
                    return;
                }
                devopsCiContentMapper.updateCiContent(ids, nexusDefaultUrl, nexusProxyUrl);
            }
        });
    }

    private List<ProjectDTO> queryProjectDTOS(List<ExternalTenantVO> resultTenants) {
        if (CollectionUtils.isEmpty(resultTenants)) {
            LOGGER.info("saas and register tenant is empty!");
            return Collections.EMPTY_LIST;
        }
        List<ProjectDTO> projectDTOS = new ArrayList<>();
        resultTenants.forEach(externalTenantVO -> {
            List<ProjectDTO> projects = baseServiceClientOperator.listIamProjectByOrgId(externalTenantVO.getTenantId());
            if (!CollectionUtils.isEmpty(projects)) {
                projectDTOS.addAll(projects);
            }
        });
        return projectDTOS;
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
