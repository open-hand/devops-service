package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import feign.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.harbor.HarborImageTagVo;
import io.choerodon.devops.api.vo.rdupm.ResponseVO;
import io.choerodon.devops.app.eventhandler.constants.HarborRepoConstants;
import io.choerodon.devops.app.service.AppServiceVersionService;
import io.choerodon.devops.app.service.HarborService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.AppServiceShareRuleDTO;
import io.choerodon.devops.infra.dto.AppServiceVersionDTO;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.harbor.HarborAllRepoDTO;
import io.choerodon.devops.infra.dto.harbor.HarborImageTagDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
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
    private static final String AUTHTYPE_PULL = "pull";

    @Autowired
    @Lazy
    private RdupmClient rdupmClient;
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private AppServiceVersionService appServiceVersionService;

    @Value("${services.harbor.delete.period.seconds: 20}")
    private Long seconds;

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
        HarborRepoDTO harborRepoDTO;
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
            throw new CommonException("devops.query.repo.config.is.null.by.configId");
        }
        return repoDTOToDevopsConfigDTO(harborRepoDTO, operateType);
    }

    @Override
    public void batchDeleteImageTags(List<HarborImageTagDTO> deleteImagetags) {
        deleteImagetags.forEach(tag -> {
            ResponseEntity<Page<HarborImageTagVo>> pageResponseEntity = rdupmClient.pagingImageTag(tag.getProjectId(), tag.getRepoName(), tag.getTagName());
            Page<HarborImageTagVo> pageResponseEntityBody = pageResponseEntity.getBody();
            // 存在对应tag才删除
            if (pageResponseEntityBody != null && !CollectionUtils.isEmpty(pageResponseEntityBody.getContent())) {
                ResponseEntity<ResponseVO> responseEntity = rdupmClient.deleteImageTag(tag.getRepoName(), tag.getTagName());
                if (responseEntity.getBody() != null && responseEntity.getBody().getFailed()) {
                    throw new CommonException(responseEntity.getBody().getCode(), responseEntity.getBody().getMessage());
                }

            }
            // 删除后，睡眠20s，减小habor压力
            try {
                TimeUnit.SECONDS.sleep(seconds);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted!", e);
                Thread.currentThread().interrupt();
            }

        });
    }


    @Override
    public Map<Long, DevopsConfigDTO> listRepoConfigByAppVersionIds(List<Long> appServiceVersionIds) {
        List<AppServiceVersionDTO> serviceVersionDTOS = appServiceVersionService.baseListVersions(appServiceVersionIds);
        Map<Long, DevopsConfigDTO> configDTOMap = new HashMap<>();
        serviceVersionDTOS.forEach(versionDTO -> {
            DevopsConfigDTO devopsConfigDTO;
            if (versionDTO.getHarborConfigId() != null) {
                devopsConfigDTO = queryRepoConfigByIdToDevopsConfig(versionDTO.getAppServiceId(), versionDTO.getProjectId(),
                        versionDTO.getHarborConfigId(), versionDTO.getRepoType(), HarborRepoConstants.AUTH_TYPE_PULL);
            } else {
                devopsConfigDTO = queryRepoConfigToDevopsConfig(versionDTO.getProjectId(),
                        versionDTO.getAppServiceId(), HarborRepoConstants.AUTH_TYPE_PULL);
            }
            devopsConfigDTO.setImage(versionDTO.getImage());
            configDTOMap.put(versionDTO.getId(), devopsConfigDTO);
        });
        return configDTOMap;
    }

    @Override
    public List<DevopsConfigDTO> queryHarborConfigByHarborConfigIds(Set<Long> harborConfigIds) {
        // 镜像刷新权限的时候查询镜像配置的接口可能超时，这里调大此接口的超时时间（仅限于刷新镜像调用此接口）
        Request.Options options = new Request.Options(5L, TimeUnit.SECONDS, 30L, TimeUnit.SECONDS, true);
        ResponseEntity<List<HarborRepoDTO>> listResponseEntity = rdupmClient.queryHarborReposByIds(harborConfigIds, options);
        List<HarborRepoDTO> body = listResponseEntity.getBody();
        if (CollectionUtils.isEmpty(body)) {
            return Collections.emptyList();
        }
        List<DevopsConfigDTO> resultDevopsConfigDTOS = new ArrayList<>();
        body.forEach(harborRepoDTO -> {
            DevopsConfigDTO devopsConfigDTO = repoDTOToDevopsConfigDTO(harborRepoDTO, AUTHTYPE_PULL);
            resultDevopsConfigDTOS.add(devopsConfigDTO);

        });
        return resultDevopsConfigDTOS;
    }

    private AppServiceShareRuleDTO queryShareAppService(Long appServiceId) {
        AppServiceShareRuleDTO appServiceShareRuleDTO = new AppServiceShareRuleDTO();
        appServiceShareRuleDTO.setAppServiceId(appServiceId);
        return appServiceShareRuleMapper.selectOne(appServiceShareRuleDTO);
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
        configVO.setIsPrivate(Boolean.TRUE.toString().equals(harborRepoConfig.getIsPrivate()));

        devopsHarborConfig.setConfig(gson.toJson(configVO));
        devopsHarborConfig.setType(harborRepoDTO.getRepoType());
        devopsHarborConfig.setId(harborRepoConfig.getRepoId());
        return devopsHarborConfig;
    }
}


