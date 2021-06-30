package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1beta2Deployment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.DevopsEnvMessageVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.choerodon.devops.app.service.PermissionHelper;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.deploy.ApplicationCenterEnum;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceInstanceMapper;
import io.choerodon.devops.infra.mapper.AppServiceShareRuleMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvAppServiceMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Service
public class DevopsEnvApplicationServiceImpl implements DevopsEnvApplicationService {

    private final JSON json = new JSON();

    @Autowired
    private AppServiceService applicationService;
    @Autowired
    private DevopsEnvAppServiceMapper devopsEnvAppServiceMapper;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private AppServiceShareRuleMapper appServiceShareRuleMapper;
    @Autowired
    private AppServiceInstanceMapper appServiceInstanceMapper;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<DevopsEnvApplicationVO> batchCreate(Long projectId, DevopsEnvAppServiceVO devopsEnvAppServiceVO) {
        permissionHelper.checkEnvBelongToProject(projectId, devopsEnvAppServiceVO.getEnvId());
        permissionHelper.checkAppServicesBelongToProject(projectId, devopsEnvAppServiceVO.getAppServiceIds());

        return devopsEnvAppServiceVO.getAppServiceIds().stream()
                .map(appServiceId -> new DevopsEnvAppServiceDTO(appServiceId, devopsEnvAppServiceVO.getEnvId()))
                .peek(e -> {
                    AppServiceDTO appServiceDTO = applicationService.baseQuery(e.getAppServiceId());
                    if (!Objects.isNull(appServiceDTO)) {
                        boolean isProjectAppService = projectId.equals(appServiceDTO.getProjectId());
                        ApplicationCenterEnum appSourceType = isProjectAppService ? ApplicationCenterEnum.PROJECT : ApplicationCenterEnum.SHARE;
                        createEnvAppRelationShipIfNon(e.getAppServiceId(), e.getEnvId(), appSourceType.value, appServiceDTO.getCode(), appServiceDTO.getName());
                    }
                })
                .map(e -> ConvertUtils.convertObject(e, DevopsEnvApplicationVO.class))
                .collect(Collectors.toList());
    }

    /**
     * 为环境和应用创建关联关系如果不存在
     *
     * @param appServiceId 应用id
     * @param envId        环境id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createEnvAppRelationShipIfNon(Long appServiceId, Long envId, String source, String serviceCode, String serviceName) {
        DevopsEnvAppServiceDTO devopsEnvAppServiceDTO = new DevopsEnvAppServiceDTO();
        devopsEnvAppServiceDTO.setAppServiceId(Objects.requireNonNull(appServiceId));
        devopsEnvAppServiceDTO.setEnvId(Objects.requireNonNull(envId));
        // 如果没有，插入
        if (devopsEnvAppServiceMapper.selectCount(devopsEnvAppServiceDTO) == 0) {
            devopsEnvAppServiceDTO.setSource(source);
            devopsEnvAppServiceDTO.setServiceName(serviceName);
            devopsEnvAppServiceDTO.setServiceCode(serviceCode);
            devopsEnvAppServiceMapper.insertSelective(devopsEnvAppServiceDTO);
        } else {
            //旧的关联关系可以跟新
            devopsEnvAppServiceDTO.setSource(source);
            devopsEnvAppServiceDTO.setServiceName(serviceName);
            devopsEnvAppServiceDTO.setServiceCode(serviceCode);
            devopsEnvAppServiceMapper.updateByPrimaryKeySelective(devopsEnvAppServiceDTO);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long envId, Long appServiceId) {
        DevopsEnvAppServiceDTO searchCondition = new DevopsEnvAppServiceDTO(appServiceId, envId);
        DevopsEnvAppServiceDTO recordInDb = devopsEnvAppServiceMapper.selectOne(searchCondition);
        if (recordInDb == null) {
            return;
        }

        if (!checkCanDelete(envId, appServiceId)) {
            throw new CommonException("error.delete.env.app-service.relationship");
        }

        devopsEnvAppServiceMapper.delete(searchCondition);
    }

    /**
     * 删除环境和应用服务之间的关联关系时，应用服务在该环境下不能有实例和相关资源
     *
     * @param envId        环境id
     * @param appServiceId 应用服务id
     * @return true 可以删除
     */
    private boolean checkCanDelete(Long envId, Long appServiceId) {
        return devopsEnvAppServiceMapper.countInstances(appServiceId, envId, null) == 0 &&
                devopsEnvAppServiceMapper.countRelatedConfigMap(appServiceId, envId, null) == 0 &&
                devopsEnvAppServiceMapper.countRelatedSecret(appServiceId, envId, null) == 0 &&
                devopsEnvAppServiceMapper.countRelatedService(appServiceId, envId, null) == 0;
    }

    @Override
    public List<AppServiceRepVO> listAppByEnvId(Long envId) {
        List<Long> appServiceIds = baseListAppByEnvId(envId);
        List<AppServiceRepVO> applicationRepVOS = new ArrayList<>();
        appServiceIds.forEach(v ->
                applicationRepVOS.add(ConvertUtils.convertObject(applicationService.baseQuery(v), AppServiceRepVO.class))
        );
        return applicationRepVOS;
    }

    @Override
    public List<Map<String, String>> listLabelByAppAndEnvId(Long envId, Long appServiceId) {
        List<DevopsEnvMessageVO> devopsEnvMessageVOS = baseListResourceByEnvAndApp(envId, appServiceId);
        List<Map<String, String>> listLabel = new ArrayList<>();
        devopsEnvMessageVOS.forEach(devopsEnvMessageVO -> {
            V1beta2Deployment v1beta2Deployment = json.deserialize(
                    devopsEnvMessageVO.getDetail(), V1beta2Deployment.class);
            listLabel.add(v1beta2Deployment.getMetadata().getLabels());
        });
        return listLabel;
    }

    @Override
    public List<DevopsEnvPortVO> listPortByAppAndEnvId(Long envId, Long appServiceId) {
        List<DevopsEnvMessageVO> devopsEnvMessageVOS = baseListResourceByEnvAndApp(envId, appServiceId);
        List<DevopsEnvPortVO> devopsEnvPortVOS = new ArrayList<>();
        devopsEnvMessageVOS.forEach(devopsEnvMessageVO -> {
            V1beta2Deployment v1beta2Deployment = json.deserialize(
                    devopsEnvMessageVO.getDetail(), V1beta2Deployment.class);
            List<V1Container> containers = v1beta2Deployment.getSpec().getTemplate().getSpec().getContainers();
            for (V1Container container : containers) {
                List<V1ContainerPort> ports = container.getPorts();

                Optional.ofNullable(ports).ifPresent(portList -> {
                    for (V1ContainerPort port : portList) {
                        DevopsEnvPortVO devopsEnvPortVO = new DevopsEnvPortVO();
                        devopsEnvPortVO.setResourceName(devopsEnvMessageVO.getResourceName());
                        devopsEnvPortVO.setPortName(port.getName());
                        devopsEnvPortVO.setPortValue(port.getContainerPort());
                        devopsEnvPortVOS.add(devopsEnvPortVO);
                    }
                });
            }
        });
        return devopsEnvPortVOS;
    }

    @Override
    public DevopsEnvAppServiceDTO baseCreate(DevopsEnvAppServiceDTO devopsEnvAppServiceDTO) {
        if (devopsEnvAppServiceMapper.insert(devopsEnvAppServiceDTO) != 1) {
            throw new CommonException("error.insert.env.app");
        }
        return devopsEnvAppServiceDTO;
    }

    @Override
    public List<Long> baseListAppByEnvId(Long envId) {
        return devopsEnvAppServiceMapper.queryAppByEnvId(envId);
    }

    @Override
    public List<DevopsEnvMessageVO> baseListResourceByEnvAndApp(Long envId, Long appServiceId) {
        return devopsEnvAppServiceMapper.listResourceByEnvAndApp(envId, appServiceId);
    }

    @Override
    public List<BaseApplicationServiceVO> listNonRelatedAppService(Long projectId, Long envId) {
        return devopsEnvAppServiceMapper.listNonRelatedApplications(projectId, envId);
    }

    @Override
    public void fixData() {
        List<DevopsEnvAppServiceDTO> devopsEnvAppServiceDTOS = devopsEnvAppServiceMapper.selectAll();
        if (CollectionUtils.isEmpty(devopsEnvAppServiceDTOS)) {
            return;
        }


        devopsEnvAppServiceDTOS.forEach(devopsEnvAppServiceDTO -> {
            //根据部署的实例来修复
            AppServiceInstanceDTO appServiceInstanceDTO = new AppServiceInstanceDTO();
            appServiceInstanceDTO.setEnvId(devopsEnvAppServiceDTO.getEnvId());
            appServiceInstanceDTO.setAppServiceId(devopsEnvAppServiceDTO.getAppServiceId());
            List<AppServiceInstanceDTO> appServiceInstanceDTOS = appServiceInstanceMapper.select(appServiceInstanceDTO);
            if (!CollectionUtils.isEmpty(appServiceInstanceDTOS)) {
                AppServiceInstanceDTO instanceDTO = appServiceInstanceDTOS.stream().sorted(Comparator.comparing(AppServiceInstanceDTO::getId).reversed()).collect(Collectors.toList()).get(0);
                if (StringUtils.equalsIgnoreCase(instanceDTO.getSource(), AppSourceType.MARKET.getValue())) {
                    //查询市场服务,
                    devopsEnvAppServiceDTO.setSource(ApplicationCenterEnum.MARKET.value);
                    List<MarketServiceDeployObjectVO> marketServiceDeployObjectVOS = marketServiceClientOperator.queryDeployObjectByMarketServiceId(0l, devopsEnvAppServiceDTO.getAppServiceId());
                    if (!CollectionUtils.isEmpty(marketServiceDeployObjectVOS)) {
                        MarketServiceDeployObjectVO marketServiceDeployObjectVO = marketServiceDeployObjectVOS.get(0);
                        devopsEnvAppServiceDTO.setServiceCode(marketServiceDeployObjectVO.getDevopsAppServiceCode());
                        devopsEnvAppServiceDTO.setServiceName(marketServiceDeployObjectVO.getDevopsAppServiceName());
                    }
                    devopsEnvAppServiceMapper.updateByPrimaryKeySelective(devopsEnvAppServiceDTO);

                    return;
                } else {
                    //共享的或本项目的
                    handDevopsAppservice(devopsEnvAppServiceDTO);
                }
            } else {
                //有关联 没有实例
                handDevopsAppservice(devopsEnvAppServiceDTO);
            }

        });
    }

    private void handDevopsAppservice(DevopsEnvAppServiceDTO devopsEnvAppServiceDTO) {
        //1.应用服务是不是在本项目下，如果在，则来源为本项目
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvAppServiceDTO.getEnvId());
        if (Objects.isNull(devopsEnvironmentDTO)) {
            //环境删除
            return;
        }
        AppServiceDTO appServiceDTO = applicationService.baseQuery(devopsEnvAppServiceDTO.getAppServiceId());
        if (Objects.isNull(appServiceDTO)) {
            //应用服务被删除
            return;
        }
        //环境与应用服务在一个项目下，则认为是本项目部署
        if (devopsEnvironmentDTO.getProjectId().equals(appServiceDTO.getProjectId())) {
            devopsEnvAppServiceDTO.setSource(ApplicationCenterEnum.PROJECT.value);
            devopsEnvAppServiceDTO.setServiceCode(appServiceDTO.getCode());
            devopsEnvAppServiceDTO.setServiceName(appServiceDTO.getName());
            devopsEnvAppServiceMapper.updateByPrimaryKeySelective(devopsEnvAppServiceDTO);
            return;
        }
        //2.不在本项目的应用服务是不是来源于共享规则，如果是来源为共享应用（不考虑部署后删除共享规则的）
        AppServiceShareRuleDTO appServiceShareRuleDTO = new AppServiceShareRuleDTO();
        appServiceShareRuleDTO.setAppServiceId(devopsEnvAppServiceDTO.getAppServiceId());
        List<AppServiceShareRuleDTO> appServiceShareRuleDTOS = appServiceShareRuleMapper.select(appServiceShareRuleDTO);
        if (!CollectionUtils.isEmpty(appServiceShareRuleDTOS)) {
            devopsEnvAppServiceDTO.setSource(ApplicationCenterEnum.SHARE.value);
            devopsEnvAppServiceDTO.setServiceCode(appServiceDTO.getCode());
            devopsEnvAppServiceDTO.setServiceName(appServiceDTO.getName());
            devopsEnvAppServiceMapper.updateByPrimaryKeySelective(devopsEnvAppServiceDTO);
            return;
        }
    }
}
