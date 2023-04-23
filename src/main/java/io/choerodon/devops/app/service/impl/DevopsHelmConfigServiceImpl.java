package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.DevopsHelmConfigVO;
import io.choerodon.devops.api.vo.iam.ImmutableProjectInfoVO;
import io.choerodon.devops.app.service.AppServiceHelmRelService;
import io.choerodon.devops.app.service.DevopsHelmConfigService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceHelmRelDTO;
import io.choerodon.devops.infra.dto.DevopsHelmConfigDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsHelmConfigMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsHelmConfigServiceImpl implements DevopsHelmConfigService {

    @Autowired
    private DevopsHelmConfigMapper devopsHelmConfigMapper;

    @Autowired
    private AppServiceHelmRelService appServiceHelmRelService;

    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Autowired
    @Qualifier(value = "restTemplateForIp")
    private RestTemplate restTemplate;

    @Override
    public List<DevopsHelmConfigVO> listHelmConfig(Long projectId) {
        List<DevopsHelmConfigDTO> devopsHelmConfigDTOS = new ArrayList<>();

        // 查询项目层设置helm仓库
        DevopsHelmConfigDTO helmConfigSearchDTOOnProject = new DevopsHelmConfigDTO();
        helmConfigSearchDTOOnProject.setResourceId(projectId);
        helmConfigSearchDTOOnProject.setResourceType(ResourceLevel.PROJECT.value());
        helmConfigSearchDTOOnProject.setDeleted(false);
        List<DevopsHelmConfigDTO> devopsHelmConfigDTOListOnProject = devopsHelmConfigMapper.select(helmConfigSearchDTOOnProject);
        devopsHelmConfigDTOS.addAll(devopsHelmConfigDTOListOnProject);
        DevopsHelmConfigDTO defaultDevopsHelmConfigDTOOnProject = null;
        if (devopsHelmConfigDTOS.size() != 0) {
            for (DevopsHelmConfigDTO devopsHelmConfigDTO : devopsHelmConfigDTOS) {
                if (Boolean.TRUE.equals(devopsHelmConfigDTO.getRepoDefault())) {
                    defaultDevopsHelmConfigDTOOnProject = devopsHelmConfigDTO;
                }
            }
        }
        devopsHelmConfigDTOS.remove(defaultDevopsHelmConfigDTOOnProject);
        devopsHelmConfigDTOS = devopsHelmConfigDTOS.stream().sorted(Comparator.comparing(DevopsHelmConfigDTO::getCreationDate, (i, j) -> {
            if (i.before(j)) {
                return -1;
            } else if (i.equals(j)) {
                return 0;
            }
            return 1;
        }).reversed()).collect(Collectors.toList());

        // 查询组织层helm仓库
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Long organizationId = projectDTO.getOrganizationId();
        String code = projectDTO.getDevopsComponentCode();
        DevopsHelmConfigDTO helmConfigSearchDTOOnOrganization = new DevopsHelmConfigDTO();
        helmConfigSearchDTOOnOrganization.setResourceId(organizationId);
        helmConfigSearchDTOOnOrganization.setResourceType(ResourceLevel.ORGANIZATION.value());
        helmConfigSearchDTOOnOrganization.setRepoDefault(true);
        DevopsHelmConfigDTO devopsHelmConfigDTOtOnOrganization = devopsHelmConfigMapper.selectOne(helmConfigSearchDTOOnOrganization);
        if (devopsHelmConfigDTOtOnOrganization != null) {
            Tenant tenant = baseServiceClientOperator.queryOrganizationById(organizationId);
            devopsHelmConfigDTOtOnOrganization.setName(tenant.getTenantNum() + "-" + code);
            if (defaultDevopsHelmConfigDTOOnProject != null) {
                devopsHelmConfigDTOtOnOrganization.setRepoDefault(false);
            }
            devopsHelmConfigDTOS.add(0, devopsHelmConfigDTOtOnOrganization);
        } else {
            // 如果组织层的仓库为空，查询平台默认
            DevopsHelmConfigDTO helmConfigSearchDTOOnSite = new DevopsHelmConfigDTO();
            helmConfigSearchDTOOnSite.setResourceType(ResourceLevel.SITE.value());
            helmConfigSearchDTOOnSite.setRepoDefault(true);
            DevopsHelmConfigDTO devopsHelmConfigDTOOnSite = devopsHelmConfigMapper.selectOne(helmConfigSearchDTOOnSite);
            if (devopsHelmConfigDTOOnSite == null) {
                throw new CommonException("devops.helm.config.site.exist");
            }
            Tenant tenant = baseServiceClientOperator.queryOrganizationById(organizationId);
            devopsHelmConfigDTOOnSite.setName(tenant.getTenantNum() + "-" + code);
            if (defaultDevopsHelmConfigDTOOnProject != null) {
                devopsHelmConfigDTOOnSite.setRepoDefault(false);
            }
            devopsHelmConfigDTOS.add(0, devopsHelmConfigDTOOnSite);
        }

        if (defaultDevopsHelmConfigDTOOnProject != null) {
            devopsHelmConfigDTOS.add(0, defaultDevopsHelmConfigDTOOnProject);
        }

        List<DevopsHelmConfigVO> devopsHelmConfigVOS = ConvertUtils.convertList(devopsHelmConfigDTOS, DevopsHelmConfigVO.class);

        Set<Long> creatorIds = devopsHelmConfigDTOS.stream().map(DevopsHelmConfigDTO::getCreatedBy).collect(Collectors.toSet());
        List<IamUserDTO> iamUserDTOList = baseServiceClientOperator.listUsersByIds(new ArrayList<>(creatorIds));
        Map<Long, IamUserDTO> iamUserDTOMap = iamUserDTOList.stream().collect(Collectors.toMap(IamUserDTO::getId, Function.identity()));

        devopsHelmConfigVOS.forEach(c -> {
            c.setPassword(null);
            IamUserDTO creator = iamUserDTOMap.get(c.getCreatedBy());
            if (creator != null) {
                c.setCreatorImageUrl(creator.getImageUrl());
                c.setCreatorLoginName(creator.getLoginName());
                c.setCreatorRealName(creator.getRealName());
            }
        });
        return devopsHelmConfigVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DevopsHelmConfigVO createDevopsHelmConfigOnProjectLevel(Long projectId, DevopsHelmConfigVO devopsHelmConfigVO) {
        checkNameExistsThrowEx(projectId, null, devopsHelmConfigVO.getName());
        DevopsHelmConfigDTO devopsHelmConfigDTO = ConvertUtils.convertObject(devopsHelmConfigVO, DevopsHelmConfigDTO.class);
        devopsHelmConfigDTO.setResourceType(ResourceLevel.PROJECT.value());
        devopsHelmConfigDTO.setResourceId(projectId);
        devopsHelmConfigDTO.setRepoPrivate(!ObjectUtils.isEmpty(devopsHelmConfigDTO.getUsername()) && !ObjectUtils.isEmpty(devopsHelmConfigDTO.getPassword()));

        DevopsHelmConfigDTO result = MapperUtil.resultJudgedInsertSelective(devopsHelmConfigMapper, devopsHelmConfigDTO, "devops.helm.config.insert");
        return ConvertUtils.convertObject(result, DevopsHelmConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DevopsHelmConfigVO updateDevopsHelmConfigOnProjectLevel(Long projectId, DevopsHelmConfigVO devopsHelmConfigVO) {
        checkNameExistsThrowEx(projectId, devopsHelmConfigVO.getId(), devopsHelmConfigVO.getName());
        DevopsHelmConfigDTO devopsHelmConfigDTO = ConvertUtils.convertObject(devopsHelmConfigVO, DevopsHelmConfigDTO.class);
        devopsHelmConfigDTO.setResourceType(ResourceLevel.PROJECT.value());
        devopsHelmConfigDTO.setResourceId(projectId);

        devopsHelmConfigDTO.setRepoPrivate(!ObjectUtils.isEmpty(devopsHelmConfigDTO.getUsername()) && !ObjectUtils.isEmpty(devopsHelmConfigDTO.getPassword()));
        devopsHelmConfigDTO.setRepoDefault(null);
        devopsHelmConfigDTO.setDeleted(null);

        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHelmConfigMapper, devopsHelmConfigDTO, "devops.helm.config.update");

        if (devopsHelmConfigDTO.getUsername() == null || devopsHelmConfigDTO.getPassword() == null) {
            devopsHelmConfigMapper.updateUsernameAndPasswordToNull(devopsHelmConfigDTO.getId());
        }

        return ConvertUtils.convertObject(devopsHelmConfigDTO, DevopsHelmConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevopsHelmConfig(Long projectId, Long helmConfigId) {
        DevopsHelmConfigDTO oldDevopsHelmConfigDTO = devopsHelmConfigMapper.listObjectVersionNumberById(helmConfigId);
        if (oldDevopsHelmConfigDTO == null) {
            return;
        }

        DevopsHelmConfigDTO devopsHelmConfigDTO = new DevopsHelmConfigDTO();
        devopsHelmConfigDTO.setResourceId(projectId);
        devopsHelmConfigDTO.setResourceType(ResourceLevel.PROJECT.value());
        devopsHelmConfigDTO.setId(helmConfigId);
        devopsHelmConfigDTO.setDeleted(true);
        devopsHelmConfigDTO.setObjectVersionNumber(oldDevopsHelmConfigDTO.getObjectVersionNumber());
        devopsHelmConfigDTO.setName(UUID.randomUUID().toString());

        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHelmConfigMapper, devopsHelmConfigDTO, "devops.helm.config.delete");
    }

    @Override
    public DevopsHelmConfigVO queryDevopsHelmConfig(Long projectId, Long helmConfigId) {
        DevopsHelmConfigDTO devopsHelmConfigSearchDTO = new DevopsHelmConfigDTO();
        devopsHelmConfigSearchDTO.setResourceType(ResourceLevel.PROJECT.value());
        devopsHelmConfigSearchDTO.setResourceId(projectId);
        devopsHelmConfigSearchDTO.setId(helmConfigId);

        DevopsHelmConfigDTO devopsHelmConfigDTO = devopsHelmConfigMapper.selectOne(devopsHelmConfigSearchDTO);
        devopsHelmConfigDTO.setPassword(null);

        return ConvertUtils.convertObject(devopsHelmConfigDTO, DevopsHelmConfigVO.class);
    }

    @Override
    public DevopsHelmConfigDTO queryById(Long id) {
        return devopsHelmConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultDevopsHelmConfig(Long projectId, Long helmConfigId) {
        DevopsHelmConfigDTO devopsHelmConfigDTO = devopsHelmConfigMapper.selectByPrimaryKey(helmConfigId);

        // 先将项目层的所有仓库是否为默认置为false
        devopsHelmConfigMapper.updateAllHelmConfigRepoDefaultToFalse(projectId);

        // 如果helm默认仓库仍是项目层，那么将指定的仓库设置为默认仓库
        if (!ResourceLevel.SITE.value().equals(devopsHelmConfigDTO.getResourceType()) && !ResourceLevel.ORGANIZATION.value().equals(devopsHelmConfigDTO.getResourceType())) {
            devopsHelmConfigMapper.updateHelmConfigRepoDefaultToTrue(projectId, helmConfigId);
        }
    }

    @Override
    public DevopsHelmConfigDTO queryDefaultDevopsHelmConfigByLevel(String resourceType, Long resourceId) {
        Assert.notNull(resourceType, ResourceCheckConstant.DEVOPS_RESOURCE_TYPE_IS_NULL);
        Assert.notNull(resourceId, ResourceCheckConstant.DEVOPS_RESOURCE_ID_IS_NULL);

        DevopsHelmConfigDTO devopsHelmConfigDTO = new DevopsHelmConfigDTO();
        devopsHelmConfigDTO.setResourceType(resourceType);
        devopsHelmConfigDTO.setResourceId(resourceId);
        devopsHelmConfigDTO.setRepoDefault(true);
        devopsHelmConfigDTO.setDeleted(false);

        return devopsHelmConfigMapper.selectOne(devopsHelmConfigDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDevopsHelmConfig(DevopsHelmConfigDTO devopsHelmConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsHelmConfigMapper, devopsHelmConfigDTO, "devops.helm.config.insert");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDevopsHelmConfig(DevopsHelmConfigDTO devopsHelmConfigDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHelmConfigMapper, devopsHelmConfigDTO, "devops.helm.config.update");
    }

    @Override
    public void updateDevopsHelmConfigToNonDefaultRepoOnOrganization(Long resourceId) {
        devopsHelmConfigMapper.updateDevopsHelmConfigToNonDefaultRepoOnOrganization(resourceId);
    }

    @Override
    public DevopsHelmConfigDTO queryAppConfig(Long appServiceId, Long projectId, Long tenantId) {
        DevopsHelmConfigDTO devopsHelmConfigDTO;
        AppServiceHelmRelDTO appServiceHelmRelDTO = appServiceHelmRelService.queryByAppServiceId(appServiceId);
        if (appServiceHelmRelDTO != null) {
            return queryById(appServiceHelmRelDTO.getHelmConfigId());
        }
        devopsHelmConfigDTO = queryDefaultDevopsHelmConfigByLevel(ResourceLevel.PROJECT.value(), projectId);
        if (devopsHelmConfigDTO != null) {
            return devopsHelmConfigDTO;
        }

        devopsHelmConfigDTO = queryDefaultDevopsHelmConfigByLevel(ResourceLevel.ORGANIZATION.value(), tenantId);
        if (devopsHelmConfigDTO != null) {
            return devopsHelmConfigDTO;
        }

        devopsHelmConfigDTO = queryDefaultDevopsHelmConfigByLevel(ResourceLevel.SITE.value(), 0L);
        return devopsHelmConfigDTO;
    }

    @Override
    public boolean checkNameExists(Long projectId, Long helmConfigId, String name) {
        return devopsHelmConfigMapper.checkNameExists(projectId, helmConfigId, name);
    }

    @Override
    public void checkNameExistsThrowEx(Long projectId, Long helmConfigId, String name) {
        if (devopsHelmConfigMapper.checkNameExists(projectId, helmConfigId, name)) {
            throw new CommonException("devops.helm.config.name.exists");
        }
    }

    @Override
    public String getIndexContent(Long projectId, Long helmConfigId) {
        DevopsHelmConfigDTO devopsHelmConfigDTO = devopsHelmConfigMapper.selectByPrimaryKey(helmConfigId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (devopsHelmConfigDTO.getRepoPrivate()) {
            String credentials = devopsHelmConfigDTO.getUsername() + ":"
                    + devopsHelmConfigDTO.getPassword();
            headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()));
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String helmRepoUrl;
        if (ResourceLevel.PROJECT.value().equals(devopsHelmConfigDTO.getResourceType())) {
            helmRepoUrl = devopsHelmConfigDTO.getUrl().endsWith("/") ? devopsHelmConfigDTO.getUrl().substring(0, devopsHelmConfigDTO.getUrl().length() - 1) : devopsHelmConfigDTO.getUrl();
        } else {
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
            Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

            helmRepoUrl = devopsHelmConfigDTO.getUrl().endsWith("/") ? devopsHelmConfigDTO.getUrl() + organization.getTenantNum() + "/" + projectDTO.getDevopsComponentCode() + "/" : devopsHelmConfigDTO.getUrl() + "/" + organization.getTenantNum() + "/" + projectDTO.getDevopsComponentCode() + "/";
        }
        ResponseEntity<String> exchange = restTemplate.exchange(helmRepoUrl + "/index.yaml", HttpMethod.GET, requestEntity, String.class);

        if (!HttpStatus.OK.equals(exchange.getStatusCode())) {
            throw new CommonException("devops.get.helm.chart");
        }
        return exchange.getBody();
    }


    @Override
    public List<DevopsHelmConfigVO> listHelmConfigOnApp(Long projectId, Long appServiceId) {
        List<DevopsHelmConfigDTO> devopsHelmConfigDTOS = new ArrayList<>();

        Long effectiveRepoId;
        // 查询项目层设置helm仓库
        List<DevopsHelmConfigDTO> devopsHelmConfigDTOListOnProject = devopsHelmConfigMapper.listHelmConfigWithIdAndName(projectId, ResourceLevel.PROJECT.value());
        devopsHelmConfigDTOS.addAll(devopsHelmConfigDTOListOnProject);
        DevopsHelmConfigDTO defaultDevopsHelmConfigDTOOnProject = null;
        if (devopsHelmConfigDTOS.size() != 0) {
            for (DevopsHelmConfigDTO devopsHelmConfigDTO : devopsHelmConfigDTOS) {
                if (Boolean.TRUE.equals(devopsHelmConfigDTO.getRepoDefault())) {
                    defaultDevopsHelmConfigDTOOnProject = devopsHelmConfigDTO;
                }
            }
        }
        devopsHelmConfigDTOS.remove(defaultDevopsHelmConfigDTOOnProject);
        devopsHelmConfigDTOS = devopsHelmConfigDTOS.stream().sorted(Comparator.comparing(DevopsHelmConfigDTO::getCreationDate, (i, j) -> {
            if (i.before(j)) {
                return -1;
            } else if (i.equals(j)) {
                return 0;
            }
            return 1;
        }).reversed()).collect(Collectors.toList());

        // 查询组织层helm仓库
        ImmutableProjectInfoVO immutableProjectInfoVO = baseServiceClientOperator.queryImmutableProjectInfo(projectId);
        Long tenantId = immutableProjectInfoVO.getTenantId();
        String projCode = immutableProjectInfoVO.getProjCode();
        DevopsHelmConfigDTO devopsHelmConfigDTOtOnOrganization = devopsHelmConfigMapper.selectOneWithIdAndName(tenantId, ResourceLevel.ORGANIZATION.value(), true);
        DevopsHelmConfigDTO devopsHelmConfigDTOOnSite = null;
        if (devopsHelmConfigDTOtOnOrganization != null) {
            Tenant tenant = baseServiceClientOperator.queryOrganizationById(tenantId);
            devopsHelmConfigDTOtOnOrganization.setName(tenant.getTenantNum() + "-" + projCode);
            devopsHelmConfigDTOS.add(0, devopsHelmConfigDTOtOnOrganization);
        } else {
            // 如果组织层的仓库为空，查询平台默认
            devopsHelmConfigDTOOnSite = devopsHelmConfigMapper.selectOneWithIdAndName(0L, ResourceLevel.SITE.value(), true);
            if (devopsHelmConfigDTOOnSite == null) {
                throw new CommonException("devops.helm.config.site.exist");
            }
            Tenant tenant = baseServiceClientOperator.queryOrganizationById(tenantId);
            devopsHelmConfigDTOOnSite.setName(tenant.getTenantNum() + "-" + projCode);
            devopsHelmConfigDTOS.add(0, devopsHelmConfigDTOOnSite);
        }

        if (defaultDevopsHelmConfigDTOOnProject != null) {
            devopsHelmConfigDTOS.add(0, defaultDevopsHelmConfigDTOOnProject);
        }
        DevopsHelmConfigDTO devopsHelmConfigDTORelatedWithAppService = null;
        if (appServiceId != null) {
            devopsHelmConfigDTORelatedWithAppService = devopsHelmConfigMapper.selectWithIdAndNameByAppServiceId(appServiceId);
        }
        if (devopsHelmConfigDTORelatedWithAppService != null) {
            effectiveRepoId = devopsHelmConfigDTORelatedWithAppService.getId();
        } else if (defaultDevopsHelmConfigDTOOnProject != null) {
            effectiveRepoId = defaultDevopsHelmConfigDTOOnProject.getId();
        } else if (devopsHelmConfigDTOtOnOrganization != null) {
            effectiveRepoId = devopsHelmConfigDTOtOnOrganization.getId();
        } else {
            effectiveRepoId = devopsHelmConfigDTOOnSite.getId();
        }

        List<DevopsHelmConfigVO> devopsHelmConfigVOS = ConvertUtils.convertList(devopsHelmConfigDTOS, DevopsHelmConfigVO.class);
        devopsHelmConfigVOS.forEach(v -> {
            if (v.getId().equals(effectiveRepoId)) {
                v.setRepoEffective(true);
            }
        });

        return devopsHelmConfigVOS;
    }

    @Override
    public void batchInsertInNewTrans(List<DevopsHelmConfigDTO> devopsHelmConfigDTOS) {
        devopsHelmConfigMapper.batchInsert(devopsHelmConfigDTOS);
    }

    @Override
    public byte[] downloadChart(Long helmConfigId, String chartUrl) {
        DevopsHelmConfigDTO devopsHelmConfigDTO = devopsHelmConfigMapper.selectByPrimaryKey(helmConfigId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (devopsHelmConfigDTO.getRepoPrivate()) {
            String credentials = devopsHelmConfigDTO.getUsername() + ":"
                    + devopsHelmConfigDTO.getPassword();
            headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()));
        }

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String helmRepoUrl = devopsHelmConfigDTO.getUrl();
        helmRepoUrl = helmRepoUrl.endsWith("/") ? helmRepoUrl.substring(0, devopsHelmConfigDTO.getUrl().length() - 1) : helmRepoUrl;

        ResponseEntity<byte[]> exchange;
        try {
            exchange = restTemplate.exchange(helmRepoUrl + "/" + chartUrl, HttpMethod.GET, requestEntity, byte[].class);
        } catch (Exception e) {
            throw new CommonException("devops.helm.chart.download", e.getMessage());
        }

        return exchange.getBody();
    }
}
