package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.MiscConstants.DEFAULT_CHART_NAME;

import java.util.*;

import com.google.gson.Gson;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.ConfigVO;
import io.choerodon.devops.api.vo.DefaultConfigVO;
import io.choerodon.devops.api.vo.DevopsConfigRepVO;
import io.choerodon.devops.api.vo.DevopsConfigVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.devops.infra.dto.DevopsHelmConfigDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsConfigMapper;
import io.choerodon.devops.infra.util.CommonExAssertUtil;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
@Service
public class DevopsConfigServiceImpl implements DevopsConfigService {
    private static final String HARBOR = "harbor";
    private static final String CHART = "chart";
    private static final Gson gson = new Gson();

    @Autowired
    private DevopsConfigMapper devopsConfigMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private DevopsHelmConfigService devopsHelmConfigService;

    @Override
    public void operate(Long resourceId, String resourceType, List<DevopsConfigVO> devopsConfigVOS) {
        devopsConfigVOS.forEach(devopsConfigVO -> {
            if (devopsConfigVO.getType().equals(CHART)) {
                DevopsHelmConfigDTO devopsHelmConfigDTO = new DevopsHelmConfigDTO();
                devopsHelmConfigDTO.setUrl(devopsConfigVO.getConfig().getUrl());
                devopsHelmConfigDTO.setName(UUID.randomUUID().toString());
                devopsHelmConfigDTO.setUsername(devopsConfigVO.getConfig().getUserName());
                devopsHelmConfigDTO.setPassword(devopsConfigVO.getConfig().getPassword());
                if (!ObjectUtils.isEmpty(devopsHelmConfigDTO.getUsername()) && !ObjectUtils.isEmpty(devopsHelmConfigDTO.getPassword())) {
                    devopsHelmConfigDTO.setRepoPrivate(true);
                } else {
                    devopsHelmConfigDTO.setRepoPrivate(false);
                }

                devopsHelmConfigDTO.setResourceType(ResourceLevel.ORGANIZATION.value());
                devopsHelmConfigDTO.setResourceId(resourceId);
                devopsHelmConfigDTO.setRepoDefault(true);
                DevopsHelmConfigDTO oldConfigDTO = devopsHelmConfigService.queryDefaultDevopsHelmConfigByLevel(ResourceLevel.ORGANIZATION.value(), resourceId);
                if (oldConfigDTO == null) {
                    devopsHelmConfigService.createDevopsHelmConfig(devopsHelmConfigDTO);
                } else if (oldConfigDTO.getUrl().equals(devopsHelmConfigDTO.getUrl())) {
                    if (!oldConfigDTO.getUsername().equals(devopsHelmConfigDTO.getUsername())
                            || !oldConfigDTO.getPassword().equals(devopsHelmConfigDTO.getPassword())) {
                        devopsHelmConfigDTO.setId(oldConfigDTO.getId());
                        devopsHelmConfigDTO.setObjectVersionNumber(oldConfigDTO.getObjectVersionNumber());
                        devopsHelmConfigService.updateDevopsHelmConfig(devopsHelmConfigDTO);
                    }
                } else {
                    oldConfigDTO.setRepoDefault(false);
                    devopsHelmConfigService.updateDevopsHelmConfig(oldConfigDTO);
                    devopsHelmConfigService.createDevopsHelmConfig(devopsHelmConfigDTO);
                }
            } else {
                //根据每个配置的默认还是自定义执行不同逻辑
                DevopsConfigDTO devopsConfigDTO = baseQueryByResourceAndType(resourceId, resourceType, devopsConfigVO.getType());
                if (devopsConfigVO.getCustom()) {
                    //根据配置所在的资源层级，查询出数据库中是否存在
                    DevopsConfigDTO newDevopsConfigDTO = voToDto(devopsConfigVO);
                    if (devopsConfigDTO != null) {
                        // 存在判断是否已经生成服务版本，无服务版本，直接覆盖更新；有服务版本，将原config对应的resourceId设置为null,新建config
                        if (appServiceVersionService.isVersionUseConfig(devopsConfigDTO.getId(), devopsConfigVO.getType())) {
                            updateResourceId(devopsConfigDTO.getId());
                            setResourceId(resourceId, resourceType, newDevopsConfigDTO);
                            newDevopsConfigDTO.setId(null);
                            baseCreate(newDevopsConfigDTO);
                        } else {
                            newDevopsConfigDTO.setId(devopsConfigDTO.getId());
                            setResourceId(resourceId, resourceType, newDevopsConfigDTO);
                            baseUpdate(newDevopsConfigDTO);
                        }
                    } else {
                        setResourceId(resourceId, resourceType, newDevopsConfigDTO);
                        newDevopsConfigDTO.setId(null);
                        baseCreate(newDevopsConfigDTO);
                    }
                } else {
                    //根据配置所在的资源层级，查询出数据库中是否存在，存在则删除
                    if (devopsConfigDTO != null) {
                        if (appServiceVersionService.isVersionUseConfig(devopsConfigDTO.getId(), devopsConfigVO.getType())) {
                            updateResourceId(devopsConfigDTO.getId());
                        } else {
                            baseDelete(devopsConfigDTO.getId());
                        }
                    }
                }
            }
        });
    }


    @Override
    public List<DevopsConfigVO> queryByResourceId(Long resourceId, String resourceType) {

        List<DevopsConfigVO> devopsConfigVOS = new ArrayList<>();

        List<DevopsConfigDTO> devopsConfigDTOS = baseListByResource(resourceId, resourceType);
        devopsConfigDTOS.forEach(devopsConfigDTO -> {
            DevopsConfigVO devopsConfigVO = dtoToVo(devopsConfigDTO);
            //如果是项目层级下的harbor类型，需返回是否私有
            if (devopsConfigVO.getProjectId() != null && devopsConfigVO.getType().equals(HARBOR)) {
                DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(devopsConfigVO.getProjectId());
                devopsConfigVO.setHarborPrivate(devopsProjectDTO.getHarborProjectIsPrivate());
            }
            devopsConfigVOS.add(devopsConfigVO);
        });
        return devopsConfigVOS;
    }

    @Override
    public DefaultConfigVO queryDefaultConfig(Long resourceId, String resourceType) {
        DefaultConfigVO defaultConfigVO = new DefaultConfigVO();

        //查询当前资源层级数据库中是否有对应的组件设置，有则返回url,无返回空，代表使用默认
        DevopsConfigDTO harborConfig = baseQueryByResourceAndType(resourceId, resourceType, HARBOR);
        if (harborConfig != null) {
            defaultConfigVO.setHarborConfigUrl(gson.fromJson(harborConfig.getConfig(), ConfigVO.class).getUrl());
        }
        DevopsConfigDTO chartConfig = baseQueryByResourceAndType(resourceId, resourceType, CHART);
        if (chartConfig != null) {
            defaultConfigVO.setChartConfigUrl(gson.fromJson(chartConfig.getConfig(), ConfigVO.class).getUrl());
        }
        return defaultConfigVO;
    }

    @Override
    public DevopsConfigDTO queryRealConfig(Long resourceId, String resourceType, String configType, String operateType) {
        //应用服务层次，先找应用配置，在找项目配置,最后找组织配置,项目和组织层次同理
        DevopsConfigDTO defaultConfig = baseQueryDefaultConfig(configType);
        if (resourceType.equals(MiscConstants.APP_SERVICE)) {
            DevopsConfigDTO appServiceConfig = baseQueryByResourceAndType(resourceId, resourceType, configType);
            if (appServiceConfig != null) {
                return appServiceConfig;
            }
            AppServiceDTO appServiceDTO = appServiceService.baseQuery(resourceId);
            if (appServiceDTO.getProjectId() == null) {
                return defaultConfig;
            }
            DevopsConfigDTO projectConfig = baseQueryByResourceAndType(appServiceDTO.getProjectId(), ResourceLevel.PROJECT.value(), configType);
            if (projectConfig != null) {
                return projectConfig;
            }
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
            Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            DevopsConfigDTO organizationConfig = baseQueryByResourceAndType(organizationDTO.getTenantId(), ResourceLevel.ORGANIZATION.value(), configType);
            if (organizationConfig != null) {
                return organizationConfig;
            }
            return defaultConfig;
        } else if (resourceType.equals(ResourceLevel.PROJECT.value())) {
            DevopsConfigDTO projectConfig = baseQueryByResourceAndType(resourceId, ResourceLevel.PROJECT.value(), configType);
            if (projectConfig != null) {
                return projectConfig;
            }
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(resourceId);
            Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
            DevopsConfigDTO organizationConfig = baseQueryByResourceAndType(organizationDTO.getTenantId(), ResourceLevel.ORGANIZATION.value(), configType);
            if (organizationConfig != null) {
                return organizationConfig;
            }
            return defaultConfig;
        } else {
            DevopsConfigDTO organizationConfig = baseQueryByResourceAndType(resourceId, ResourceLevel.ORGANIZATION.value(), configType);
            if (organizationConfig != null) {
                return organizationConfig;
            }
            return defaultConfig;
        }
    }

    @Override
    public DevopsConfigDTO baseCreate(DevopsConfigDTO devopsConfigDTO) {
        if (devopsConfigMapper.insert(devopsConfigDTO) != 1) {
            throw new CommonException("devops.project.config.create");
        }
        return devopsConfigDTO;
    }

    @Override
    public DevopsConfigDTO baseUpdate(DevopsConfigDTO devopsConfigDTO) {
        if (devopsConfigMapper.updateByPrimaryKeySelective(devopsConfigDTO) != 1) {
            throw new CommonException("devops.project.config.update");
        }
        return devopsConfigMapper.selectByPrimaryKey(devopsConfigDTO);
    }

    @Override
    public void updateResourceId(Long configId) {
        devopsConfigMapper.updateResourceId(configId);
    }

    @Override
    public DevopsConfigDTO baseQuery(Long id) {
        return devopsConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsConfigDTO baseQueryByName(Long projectId, String name) {
        DevopsConfigDTO paramDO = new DevopsConfigDTO();
        paramDO.setProjectId(projectId);
        paramDO.setName(name);
        return devopsConfigMapper.selectOne(paramDO);
    }

    @Override
    public Page<DevopsConfigDTO> basePageByOptions(Long projectId, PageRequest pageable, String params) {
        Map<String, Object> mapParams = TypeUtil.castMapParams(params);

        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable),
                () -> devopsConfigMapper.listByOptions(projectId,
                        TypeUtil.cast(mapParams.get(TypeUtil.SEARCH_PARAM)),
                        TypeUtil.cast(mapParams.get(TypeUtil.PARAMS)), PageRequestUtil.checkSortIsEmpty(pageable)));
    }

    @Override
    public void baseDelete(Long id) {
        if (devopsConfigMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("devops.project.config.delete");
        }
    }

    @Override
    public DevopsConfigDTO baseQueryByResourceAndType(Long resourceId, String resourceType, String type) {
        DevopsConfigDTO devopsConfigDTO = new DevopsConfigDTO();
        setResourceId(resourceId, resourceType, devopsConfigDTO);
        devopsConfigDTO.setType(type);
        return devopsConfigMapper.selectOne(devopsConfigDTO);
    }


    private DevopsConfigDTO baseQueryDefaultConfig(String type) {
        DevopsConfigDTO devopsConfigDTO = new DevopsConfigDTO();
        if (type.equals(HARBOR)) {
            devopsConfigDTO.setName(MiscConstants.DEFAULT_HARBOR_NAME);
        } else {
            devopsConfigDTO.setName(DEFAULT_CHART_NAME);
        }
        return devopsConfigMapper.selectOne(devopsConfigDTO);
    }

    private void setResourceId(Long resourceId, String resourceType, DevopsConfigDTO devopsConfigDTO) {
        if (ResourceLevel.ORGANIZATION.value().equals(resourceType)) {
            devopsConfigDTO.setOrganizationId(resourceId);
        } else if (ResourceLevel.PROJECT.value().equals(resourceType)) {
            devopsConfigDTO.setProjectId(resourceId);
        } else {
            devopsConfigDTO.setAppServiceId(resourceId);
        }
    }

    private List<DevopsConfigDTO> baseListByResource(Long resourceId, String resourceType) {
        DevopsConfigDTO devopsConfigDTO = new DevopsConfigDTO();
        setResourceId(resourceId, resourceType, devopsConfigDTO);
        return devopsConfigMapper.select(devopsConfigDTO);
    }

    @Override
    public DevopsConfigVO dtoToVo(DevopsConfigDTO devopsConfigDTO) {
        DevopsConfigVO devopsConfigVO = new DevopsConfigVO();
        BeanUtils.copyProperties(devopsConfigDTO, devopsConfigVO);
        ConfigVO configVO = gson.fromJson(devopsConfigDTO.getConfig(), ConfigVO.class);
        devopsConfigVO.setConfig(configVO);
        return devopsConfigVO;
    }

    @Override
    public DevopsConfigDTO voToDto(DevopsConfigVO devopsConfigVO) {
        DevopsConfigDTO devopsConfigDTO = new DevopsConfigDTO();
        BeanUtils.copyProperties(devopsConfigVO, devopsConfigDTO);
        String configJson = gson.toJson(devopsConfigVO.getConfig());
        devopsConfigDTO.setConfig(configJson);
        return devopsConfigDTO;
    }

    private void configVOInToRepVO(DevopsConfigRepVO devopsConfigRepVO, DevopsConfigVO devopsConfigVO) {
        if (devopsConfigVO.getType().equals(HARBOR)) {
            devopsConfigRepVO.setHarbor(devopsConfigVO);
            devopsConfigRepVO.setHarborPrivate(devopsConfigVO.getHarborPrivate());
        }
    }

    @Override
    public DevopsConfigRepVO queryConfig(Long resourceId, String resourceType) {
        DevopsConfigRepVO devopsConfigRepVO = new DevopsConfigRepVO();
        List<DevopsConfigVO> configVOS = queryByResourceId(resourceId, resourceType);
        configVOS.forEach(devopsConfigVO -> configVOInToRepVO(devopsConfigRepVO, devopsConfigVO));
        if (resourceType.equals(ResourceLevel.PROJECT.value())) {
            DevopsProjectDTO devopsProjectDTO = devopsProjectService.baseQueryByProjectId(resourceId);
            devopsConfigRepVO.setHarborPrivate(devopsProjectDTO.getHarborProjectIsPrivate());
        }
        // 设置chart仓库
        DevopsHelmConfigDTO devopsHelmConfigDTO = devopsHelmConfigService.queryDefaultDevopsHelmConfigByLevel(resourceType, resourceId);
        if (devopsHelmConfigDTO != null) {
            ConfigVO configVO = new ConfigVO();
            configVO.setUrl(devopsHelmConfigDTO.getUrl());
            configVO.setUserName(devopsHelmConfigDTO.getUsername());
            configVO.setPassword(devopsHelmConfigDTO.getPassword());
            configVO.setIsPrivate(devopsHelmConfigDTO.getRepoPrivate());

            DevopsConfigVO chart = new DevopsConfigVO();
            chart.setType(CHART);
            chart.setCustom(true);
            chart.setConfig(configVO);
            devopsConfigRepVO.setChart(chart);
        }
        return devopsConfigRepVO;
    }

    @Override
    public void operateConfig(Long resourceId, String resourceType, DevopsConfigRepVO devopsConfigRepVO) {
        List<DevopsConfigVO> configVOS = new ArrayList<>();
        DevopsConfigVO chart = null;
        if (ObjectUtils.isEmpty(devopsConfigRepVO.getChart())) {
            if (ResourceLevel.ORGANIZATION.value().equals(resourceType)) {
                devopsHelmConfigService.updateDevopsHelmConfigToNonDefaultRepoOnOrganization(resourceId);
                return;
            }
        } else {
            chart = devopsConfigRepVO.getChart();
            chart.setCustom(Boolean.TRUE);
            ConfigVO configVO = chart.getConfig();
            CommonExAssertUtil.assertNotNull(configVO, "devops.chart.config.null");
            boolean usernameEmpty = !StringUtils.hasText(configVO.getUserName());
            boolean passwordEmpty = !StringUtils.hasText(configVO.getPassword());
            if (!usernameEmpty && !passwordEmpty) {
                configVO.setUserName(configVO.getUserName());
                configVO.setPassword(configVO.getPassword());
                configVO.setIsPrivate(Boolean.TRUE);
            } else {
                configVO.setIsPrivate(Boolean.FALSE);
            }

            // 用户名和密码要么都为空, 要么都有值
            CommonExAssertUtil.assertTrue(((usernameEmpty && passwordEmpty) || (!usernameEmpty && !passwordEmpty)), "devops.chart.auth.invalid");
        }
        if (chart == null) {
            return;
        }
        chart.setType(CHART);
        configVOS.add(chart);
        operate(resourceId, resourceType, configVOS);
    }

    @Override
    public void deleteByConfigIds(Set<Long> configIds) {
        List<DevopsConfigDTO> devopsConfigDTOS = devopsConfigMapper.listByConfigs(configIds);
        devopsConfigDTOS.stream().filter(devopsConfigDTO -> devopsConfigDTO.getAppServiceId() != null)
                .forEach(devopsConfigDTO -> devopsConfigMapper.deleteByPrimaryKey(devopsConfigDTO.getId()));
    }

    @Override
    public List<DevopsConfigDTO> listAllChart() {
        return devopsConfigMapper.listAllChart();
    }
}


