package io.choerodon.devops.app.service.impl;

import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;

import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.feign.RdupmClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import retrofit2.Response;
import retrofit2.Retrofit;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.app.eventhandler.payload.HarborPayload;
import io.choerodon.devops.app.service.DevopsConfigService;
import io.choerodon.devops.app.service.DevopsHarborUserService;
import io.choerodon.devops.app.service.DevopsProjectService;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.dto.harbor.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.handler.RetrofitHandler;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.mapper.AppServiceShareRuleMapper;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:37
 * Description:
 */
@Component
public class HarborServiceImpl implements HarborService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HarborServiceImpl.class);
    private static final String HARBOR = "harbor";
    private static final String AUTHTYPE = "pull";
    private static final String CUSTOM_REPO = "CUSTOM_REPO";
    private static final String DEFAULT_REPO = "DEFAULT_REPO";
    private static final Gson gson = new Gson();

    @Autowired
    private HarborConfigurationProperties harborConfigurationProperties;
    @Autowired
    private DevopsConfigService devopsConfigService;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsHarborUserService devopsHarborUserService;
    @Autowired
    @Lazy
    private RdupmClient rdupmClient;
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper;
    @Autowired
    private AppServiceMapper appServiceMapper;

    @Value("${services.harbor.baseUrl}")
    private String baseUrl;
    @Value("${services.harbor.username}")
    private String username;
    @Value("${services.harbor.password}")
    private String password;

    @Override
    public User convertHarborUser(ProjectDTO projectDTO, Boolean isPush, String name) {
        String pull = "";
        if (!isPush) {
            pull = "pull";
        }
        String userName = null;
        if (ObjectUtils.isEmpty(name)) {
            userName = String.format("%sUser%s%s", pull, projectDTO.getOrganizationId(), projectDTO.getId());
        } else {
            userName = name;
        }
        String userEmail = String.format("%s@harbor.com", userName);
        String pwd = String.format("%sPWD", userName);
        return new User(userName, userEmail, pwd, userName);
    }


    @Override
    public List<HarborRepoConfigDTO> listAllCustomRepoByProject(Long projectId) {
        HarborAllRepoDTO harborAllRepoDTO = rdupmClient.queryAllHarborRepoConfig(projectId).getBody();
        List<HarborRepoConfigDTO> harborCustomRepoVOS = new ArrayList<>();
        if (!Objects.isNull(harborAllRepoDTO)) {
            List<HarborRepoConfigDTO> harborCustomRepoConfigList = harborAllRepoDTO.getHarborCustomRepoConfigList();
            HarborRepoConfigDTO harborDefaultRepoConfig = harborAllRepoDTO.getHarborDefaultRepoConfig();
            if (!CollectionUtils.isEmpty(harborCustomRepoConfigList)) {
                for (HarborRepoConfigDTO harborRepoConfigDTO : harborCustomRepoConfigList) {
                    harborRepoConfigDTO.setType(CUSTOM_REPO);
                    harborCustomRepoVOS.add(harborRepoConfigDTO);
                }
            }
            if (!Objects.isNull(harborDefaultRepoConfig)) {
                harborDefaultRepoConfig.setType(DEFAULT_REPO);
                harborCustomRepoVOS.add(harborDefaultRepoConfig);
            }
        }
        return harborCustomRepoVOS;
    }

    @Override
    public DevopsConfigDTO queryRepoConfigToDevopsConfig(Long projectId, Long appServiceId, String operateType) {
        HarborRepoDTO harborRepoDTO = new HarborRepoDTO();
        AppServiceShareRuleDTO appServiceShareRuleDTO = queryShareAppService(appServiceId);
        if (!Objects.isNull(appServiceShareRuleDTO)) {
            AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
            harborRepoDTO = rdupmClient.queryHarborRepoConfig(appServiceDTO.getProjectId(), appServiceId).getBody();
        } else {
            harborRepoDTO = rdupmClient.queryHarborRepoConfig(projectId, appServiceId).getBody();
        }
        if (Objects.isNull(harborRepoDTO) || Objects.isNull(harborRepoDTO.getHarborRepoConfig())) {
            throw new CommonException("no custom or default warehouse configuration exists");
        }
        return repoDTOToDevopsConfigDTO(harborRepoDTO, operateType);
    }

    @Override
    public DevopsConfigDTO queryRepoConfigByIdToDevopsConfig(Long appServcieId, Long projectId, Long harborConfigId, String repoType, String operateType) {
        //查询应用服务是否为共享，如果是共享则从原来的的项目下拿仓库
        HarborRepoDTO harborRepoDTO = new HarborRepoDTO();
        AppServiceShareRuleDTO appServiceShareRuleDTO = queryShareAppService(appServcieId);
        if (!Objects.isNull(appServiceShareRuleDTO)) {
            AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServcieId);
            harborRepoDTO = rdupmClient.queryHarborRepoConfigById(appServiceDTO.getProjectId(),
                    harborConfigId, repoType).getBody();
        } else {
            harborRepoDTO = rdupmClient.queryHarborRepoConfigById(projectId, harborConfigId, repoType).getBody();
        }
        if (Objects.isNull(harborRepoDTO) || Objects.isNull(harborRepoDTO.getHarborRepoConfig())) {
            throw new CommonException("query.repo.config.is null.by.configId");
        }
        return repoDTOToDevopsConfigDTO(harborRepoDTO, operateType);
    }

    private AppServiceShareRuleDTO queryShareAppService(Long appServiceId) {
        AppServiceShareRuleDTO appServiceShareRuleDTO = new AppServiceShareRuleDTO();
        appServiceShareRuleDTO.setAppServiceId(appServiceId);
        AppServiceShareRuleDTO serviceShareRuleDTO = appServiceShareRuleMapper.selectOne(appServiceShareRuleDTO);
        return serviceShareRuleDTO;
    }

    private DevopsConfigDTO repoDTOToDevopsConfigDTO(HarborRepoDTO harborRepoDTO, String operateType) {
        HarborRepoConfigDTO harborRepoConfig = harborRepoDTO.getHarborRepoConfig();
        DevopsConfigDTO devopsHarborConfig = new DevopsConfigDTO();
        ConfigVO configVO = new ConfigVO();
        configVO.setUrl(harborRepoConfig.getRepoUrl());
        //自定义仓库才有默认的邮箱
        if (AUTHTYPE.equals(operateType)) {
            if (CUSTOM_REPO.equals(harborRepoDTO.getRepoType())) {
                configVO.setUserName(harborRepoConfig.getLoginName());
                configVO.setPassword(harborRepoConfig.getPassword());
            } else {
                configVO.setUserName(harborRepoDTO.getPullRobot().getName());
                configVO.setPassword(harborRepoDTO.getPullRobot().getToken());
            }

        } else {
            if (CUSTOM_REPO.equals(harborRepoDTO.getRepoType())) {
                configVO.setUserName(harborRepoConfig.getLoginName());
                configVO.setPassword(harborRepoConfig.getPassword());
            } else {
                configVO.setUserName(harborRepoDTO.getPushRobot().getName());
                configVO.setPassword(harborRepoDTO.getPushRobot().getToken());
            }
        }
        configVO.setProject(harborRepoConfig.getRepoName());
        configVO.setPrivate(Boolean.TRUE.toString().equals(harborRepoConfig.getIsPrivate()));

        devopsHarborConfig.setConfig(gson.toJson(configVO));
        devopsHarborConfig.setType("harbor");
        devopsHarborConfig.setId(harborRepoConfig.getRepoId());
        return devopsHarborConfig;
    }
}


