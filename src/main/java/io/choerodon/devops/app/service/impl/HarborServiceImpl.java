package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceShareRuleDTO;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.harbor.HarborAllRepoDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.harbor.User;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.RdupmClient;
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
    private static final Gson gson = new Gson();

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
            if (!Objects.isNull(harborDefaultRepoConfig) && !Objects.isNull(harborDefaultRepoConfig.getRepoId())) {
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
    public DevopsConfigDTO queryRepoConfigByIdToDevopsConfig(Long appServiceId, Long projectId, Long harborConfigId, String repoType, String operateType) {
        //查询应用服务是否为共享，如果是共享则从原来的的项目下拿仓库
        HarborRepoDTO harborRepoDTO;
        AppServiceShareRuleDTO appServiceShareRuleDTO = null;
        if (appServiceId != null) {
            appServiceShareRuleDTO = queryShareAppService(appServiceId);
        }
        if (!Objects.isNull(appServiceShareRuleDTO)) {
            AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
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
        if (AUTH_TYPE_PULL.equals(operateType)) {
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
        devopsHarborConfig.setType(harborRepoDTO.getRepoType());
        devopsHarborConfig.setId(harborRepoConfig.getRepoId());
        return devopsHarborConfig;
    }
}


