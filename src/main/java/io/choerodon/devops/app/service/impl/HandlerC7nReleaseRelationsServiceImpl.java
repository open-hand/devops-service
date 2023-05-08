package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AppServiceDeployVO;
import io.choerodon.devops.api.vo.AppServiceInstanceVO;
import io.choerodon.devops.api.vo.MarketInstanceCreationRequestVO;
import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.api.vo.market.MarketServiceDeployObjectVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.enums.AppSourceType;
import io.choerodon.devops.infra.enums.CommandType;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.MarketServiceClientOperator;
import io.choerodon.devops.infra.mapper.AppServiceMapper;
import io.choerodon.devops.infra.util.ComponentVersionUtil;
import io.choerodon.devops.infra.util.GitOpsUtil;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.openapi.models.V1Endpoints;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class HandlerC7nReleaseRelationsServiceImpl implements HandlerObjectFileRelationsService<C7nHelmRelease> {
    private static final String C7N_HELM_RELEASE = "C7NHelmRelease";
    private static final String GIT_SUFFIX = "/.git";
    private static final String COMPARE_VALUES = "{}";
    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerC7nReleaseRelationsServiceImpl.class);
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    @Lazy
    private AppServiceInstanceService appServiceInstanceService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private AppServiceVersionService appServiceVersionService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsServiceInstanceService devopsServiceInstanceService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private MarketServiceClientOperator marketServiceClientOperator;
    @Autowired
    private AppServiceMapper appServiceMapper;
    @Autowired
    private AppServiceShareRuleService appServiceShareRuleService;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync, List<C7nHelmRelease> c7nHelmReleases, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeC7nRelease = beforeSync.stream()
                .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType().equals(C7N_HELM_RELEASE))
                .map(devopsEnvFileResourceDTO -> {
                    AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService
                            .baseQuery(devopsEnvFileResourceDTO.getResourceId());
                    if (appServiceInstanceDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceDTO.getResourceId(), C7N_HELM_RELEASE);
                        return null;
                    }
                    return appServiceInstanceDTO.getCode();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        //比较已存在实例和新增要处理的实例,获取新增实例，更新实例，删除实例
        List<C7nHelmRelease> addC7nHelmRelease = new ArrayList<>();
        List<C7nHelmRelease> updateC7nHelmRelease = new ArrayList<>();
        c7nHelmReleases.forEach(c7nHelmRelease -> {
            if (beforeC7nRelease.contains(c7nHelmRelease.getMetadata().getName())) {
                updateC7nHelmRelease.add(c7nHelmRelease);
                beforeC7nRelease.remove(c7nHelmRelease.getMetadata().getName());
            } else {
                addC7nHelmRelease.add(c7nHelmRelease);
            }
        });

        // 新增instance
        addC7nHelmRelease(objectPath, envId, projectId, addC7nHelmRelease.stream().filter(f -> !AppSourceType.MARKET.getValue().equals(f.getSpec().getSource()) && !AppSourceType.MIDDLEWARE.getValue().equals(f.getSpec().getSource())).collect(Collectors.toList()), path, userId);

        // 新增市场实例
        addMarketInstance(objectPath, envId, projectId, addC7nHelmRelease.stream().filter(f -> AppSourceType.MARKET.getValue().equals(f.getSpec().getSource()) || AppSourceType.MIDDLEWARE.getValue().equals(f.getSpec().getSource())).collect(Collectors.toList()), path, userId);

        // 更新instance
        updateC7nHelmRelease(objectPath, envId, projectId, updateC7nHelmRelease.stream().filter(f -> !AppSourceType.MARKET.getValue().equals(f.getSpec().getSource()) && !AppSourceType.MIDDLEWARE.getValue().equals(f.getSpec().getSource())).collect(Collectors.toList()), path, userId);

        // 更新市场实例
        updateMarketInstance(objectPath, envId, projectId, updateC7nHelmRelease.stream().filter(f -> AppSourceType.MARKET.getValue().equals(f.getSpec().getSource()) || AppSourceType.MIDDLEWARE.getValue().equals(f.getSpec().getSource())).collect(Collectors.toList()), path, userId);

        //删除instance,和文件对象关联关系
        beforeC7nRelease.forEach(releaseName -> {
            AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService.baseQueryByCodeAndEnv(releaseName, envId);
            if (appServiceInstanceDTO != null) {
                appServiceInstanceService.instanceDeleteByGitOps(appServiceInstanceDTO.getId());
                devopsEnvFileResourceService
                        .baseDeleteByEnvIdAndResourceId(envId, appServiceInstanceDTO.getId(), C7N_HELM_RELEASE);
            }
        });
    }

    @Override
    public Class<C7nHelmRelease> getTarget() {
        return C7nHelmRelease.class;
    }


    private void updateC7nHelmRelease(Map<String, String> objectPath, Long envId, Long projectId, List<C7nHelmRelease> updateC7nHelmRelease, String path, Long userId) {
        updateC7nHelmRelease.forEach(c7nHelmRelease -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode()));
                        //初始化实例参数,更新时判断实例是否真的修改，没有修改则直接更新文件关联关系
                        AppServiceDeployVO appServiceDeployVO = getApplicationDeployDTO(
                                c7nHelmRelease,
                                projectId,
                                envId,
                                filePath,
                                "update");
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceDeployVO.getCommandId());
                        if (!appServiceDeployVO.getIsNotChange()) {
                            AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService
                                    .createOrUpdateByGitOps(appServiceDeployVO, userId);
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceVO.getCommandId());
                        }

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, appServiceDeployVO.getInstanceId(), c7nHelmRelease.getKind());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId,
                                devopsEnvFileResourceDTO,
                                c7nHelmRelease.hashCode(), appServiceDeployVO.getInstanceId(),
                                c7nHelmRelease.getKind());
                    } catch (GitOpsExplainException ex) {
                        throw ex;
                    } catch (CommonException e) {
                        throw new GitOpsExplainException(e.getMessage(), filePath, "", e.getParameters());
                    }
                }
        );
    }

    private void updateMarketInstance(Map<String, String> objectPath, Long envId, Long projectId, List<C7nHelmRelease> updateC7nHelmRelease, String path, Long userId) {
        updateC7nHelmRelease.forEach(c7nHelmRelease -> {
                    String filePath = StringUtils.EMPTY;
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode()));
                        //初始化实例参数,更新时判断实例是否真的修改，没有修改则直接更新文件关联关系
                        MarketInstanceCreationRequestVO appServiceDeployVO = getMarketInstanceCreationRequestVO(
                                c7nHelmRelease,
                                projectId,
                                envId,
                                filePath,
                                CommandType.UPDATE.getType());
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceDeployVO.getCommandId());
                        if (!appServiceDeployVO.getNotChanged()) {
                            AppServiceInstanceVO appServiceInstanceVO = appServiceInstanceService
                                    .createOrUpdateMarketInstanceByGitOps(appServiceDeployVO, userId);
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceVO.getCommandId());
                        }

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, appServiceDeployVO.getInstanceId(), c7nHelmRelease.getKind());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId,
                                devopsEnvFileResourceDTO,
                                c7nHelmRelease.hashCode(), appServiceDeployVO.getInstanceId(),
                                c7nHelmRelease.getKind());
                    } catch (GitOpsExplainException ex) {
                        throw ex;
                    } catch (CommonException e) {
                        throw new GitOpsExplainException(e.getMessage(), filePath, "", e.getParameters());
                    }
                }
        );
    }

    private void addC7nHelmRelease(Map<String, String> objectPath, Long envId, Long projectId, List<C7nHelmRelease> addC7nHelmRelease, String path, Long userId) {
        addC7nHelmRelease.forEach(c7nHelmRelease -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode()));
                AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService
                        .baseQueryByCodeAndEnv(c7nHelmRelease.getMetadata().getName(), envId);
                AppServiceDeployVO appServiceDeployVO;

                AppServiceInstanceVO appServiceInstanceVO = new AppServiceInstanceVO();
                //初始化实例参数,创建时判断实例是否存在，存在则直接创建文件对象关联关系
                if (appServiceInstanceDTO == null) {
                    appServiceDeployVO = getApplicationDeployDTO(
                            c7nHelmRelease,
                            projectId,
                            envId,
                            filePath,
                            "create");
                    appServiceInstanceVO = appServiceInstanceService.createOrUpdateByGitOps(appServiceDeployVO, userId);
                } else {
                    appServiceInstanceVO.setId(appServiceInstanceDTO.getId());
                    appServiceInstanceVO.setCommandId(appServiceInstanceDTO.getCommandId());
                }
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceVO.getCommandId());


                List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS = devopsServiceInstanceService.baseListByEnvIdAndInstanceCode(envId, c7nHelmRelease.getMetadata().getName());

                //删除实例之后，重新创建同名的实例，如果之前的实例关联的网络，此时需要把网络关联上新的实例
                Long instanceId = appServiceInstanceVO.getId();
                if (devopsServiceInstanceDTOS != null && !devopsServiceInstanceDTOS.isEmpty()) {
                    devopsServiceInstanceDTOS.stream().filter(devopsServiceAppInstanceDTO -> !devopsServiceAppInstanceDTO.getInstanceId().equals(instanceId)).forEach(devopsServiceAppInstanceDTO -> devopsServiceInstanceService.baseUpdateInstanceId(devopsServiceAppInstanceDTO.getId(), instanceId));
                }

                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, c7nHelmRelease.hashCode(), instanceId,
                        c7nHelmRelease.getKind());

            } catch (GitOpsExplainException ex) {
                throw ex;
            } catch (CommonException e) {
                throw new GitOpsExplainException(e.getMessage(), filePath, "", e.getParameters());
            }
        });
    }

    private void addMarketInstance(Map<String, String> objectPath, Long envId, Long projectId, List<C7nHelmRelease> addC7nHelmRelease, String path, Long userId) {
        addC7nHelmRelease.forEach(c7nHelmRelease -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode()));
                AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService
                        .baseQueryByCodeAndEnv(c7nHelmRelease.getMetadata().getName(), envId);
                MarketInstanceCreationRequestVO appServiceDeployVO;

                AppServiceInstanceVO appServiceInstanceVO = new AppServiceInstanceVO();
                //初始化实例参数,创建时判断实例是否存在，存在则直接创建文件对象关联关系
                if (appServiceInstanceDTO == null) {
                    appServiceDeployVO = getMarketInstanceCreationRequestVO(
                            c7nHelmRelease,
                            projectId,
                            envId,
                            filePath,
                            CommandType.CREATE.getType());
                    appServiceInstanceVO = appServiceInstanceService.createOrUpdateMarketInstanceByGitOps(appServiceDeployVO, userId);
                } else {
                    appServiceInstanceVO.setId(appServiceInstanceDTO.getId());
                    appServiceInstanceVO.setCommandId(appServiceInstanceDTO.getCommandId());
                }
                DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceVO.getCommandId());


                List<DevopsServiceInstanceDTO> devopsServiceInstanceDTOS = devopsServiceInstanceService.baseListByEnvIdAndInstanceCode(envId, c7nHelmRelease.getMetadata().getName());

                //删除实例之后，重新创建同名的实例，如果之前的实例关联的网络，此时需要把网络关联上新的实例
                Long instanceId = appServiceInstanceVO.getId();
                if (devopsServiceInstanceDTOS != null && !devopsServiceInstanceDTOS.isEmpty()) {
                    devopsServiceInstanceDTOS.stream().filter(devopsServiceAppInstanceDTO -> !devopsServiceAppInstanceDTO.getInstanceId().equals(instanceId)).forEach(devopsServiceAppInstanceDTO -> devopsServiceInstanceService.baseUpdateInstanceId(devopsServiceAppInstanceDTO.getId(), instanceId));
                }

                devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                devopsEnvCommandService.baseUpdateSha(devopsEnvCommandDTO.getId(), devopsEnvCommandDTO.getSha());

                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, c7nHelmRelease.hashCode(), instanceId,
                        c7nHelmRelease.getKind());

            } catch (GitOpsExplainException ex) {
                throw ex;
            } catch (CommonException e) {
                throw new GitOpsExplainException(e.getMessage(), filePath, "", e.getParameters());
            }
        });
    }


    /**
     * 校验版本是否为空
     *
     * @param appServiceVersionDTO 应用服务版本
     * @param filePath             文件路径
     * @param c7nHelmRelease       release信息
     */
    private void validateVersion(AppServiceVersionDTO appServiceVersionDTO, String filePath, C7nHelmRelease c7nHelmRelease) {
        if (appServiceVersionDTO == null) {
            throw new GitOpsExplainException("devops.appversion.not.exist.in.database", filePath, c7nHelmRelease.getSpec().getChartVersion());
        }
    }

    /**
     * 校验版本是否为空
     *
     * @param appServiceVersionDTO 应用服务版本
     * @param filePath             文件路径
     * @param c7nHelmRelease       release信息
     */
    private void validateVersion(MarketServiceDeployObjectVO appServiceVersionDTO, String filePath, C7nHelmRelease c7nHelmRelease) {
        if (appServiceVersionDTO == null || appServiceVersionDTO.getMarketServiceId() == null) {
            throw new GitOpsExplainException("devops.appversion.not.exist.in.database", filePath, c7nHelmRelease.getSpec().getChartVersion());
        }
    }

    private AppServiceDeployVO getApplicationDeployDTO(C7nHelmRelease c7nHelmRelease,
                                                       Long projectId, Long envId, String filePath, String type) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Tenant organization = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        boolean isClusterComponent = GitOpsUtil.isClusterComponent(devopsEnvironmentDTO.getType(), c7nHelmRelease);

        AppServiceVersionDTO appServiceVersionDTO;
        String versionValue;
        // 根据不同的release情况处理release所属应用服务及其版本
        if (!isClusterComponent) {
            // 尝试找到部署的版本
            appServiceVersionDTO = findVersion(c7nHelmRelease, projectId, organization.getTenantId(), filePath, type, envId);
            versionValue = appServiceVersionService.baseQueryValue(appServiceVersionDTO.getId());
        } else {
            appServiceVersionDTO = ComponentVersionUtil.getComponentVersion(c7nHelmRelease.getSpec().getChartName());
            validateVersion(appServiceVersionDTO, filePath, c7nHelmRelease);
            versionValue = appServiceVersionDTO.getValues();
        }

        AppServiceDeployVO appServiceDeployVO = new AppServiceDeployVO();
        appServiceDeployVO.setEnvironmentId(envId);
        appServiceDeployVO.setType(type);
        // 设置values为配置库的values和版本的values合并值
        appServiceDeployVO.setValues(appServiceInstanceService.getReplaceResult(versionValue, c7nHelmRelease.getSpec().getValues()).getYaml());
        appServiceDeployVO.setAppServiceId(appServiceVersionDTO.getAppServiceId());
        appServiceDeployVO.setAppServiceVersionId(appServiceVersionDTO.getId());
        appServiceDeployVO.setInstanceName(c7nHelmRelease.getMetadata().getName());
        if (type.equals("update")) {
            DevopsEnvCommandDTO devopsEnvCommandDTO;
            AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService
                    .baseQueryByCodeAndEnv(c7nHelmRelease.getMetadata().getName(), envId);
            if (appServiceInstanceDTO.getCommandId() == null) {
                devopsEnvCommandDTO = devopsEnvCommandService.baseQueryByObject(ObjectType.INSTANCE.getType(), appServiceInstanceDTO.getId());
            } else {
                devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
            }
            // 上次部署的传入values
            String lastDeployRawValue = appServiceInstanceService.baseQueryValueByInstanceId(appServiceInstanceDTO.getId());
            // 上次部署values和版本values的合并值
            String lastDeployMergedValues = appServiceInstanceService.getReplaceResult(versionValue, lastDeployRawValue).getYaml();
            // (上次部署和版本的合并值)，和 (这次部署和版本values的合并值) 的对比结果
            InstanceValueVO compareResult = appServiceInstanceService.getReplaceResult(lastDeployMergedValues, appServiceDeployVO.getValues());
            if (lastDeployRawValue != null
                    && (compareResult.getDeltaYaml() == null || compareResult.getDeltaYaml().equals("") || compareResult.getDeltaYaml().trim().equals(COMPARE_VALUES))
                    && Objects.equals(appServiceVersionDTO.getId(), devopsEnvCommandDTO.getObjectVersionId())) {
                appServiceDeployVO.setIsNotChange(true);
            }
            appServiceDeployVO.setInstanceId(appServiceInstanceDTO.getId());
            appServiceDeployVO.setCommandId(appServiceInstanceDTO.getCommandId());
        } else {
            appServiceDeployVO.setAppName(c7nHelmRelease.getMetadata().getName());
            appServiceDeployVO.setAppCode(c7nHelmRelease.getMetadata().getName());
        }
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceVersionDTO.getAppServiceId());
        appServiceInstanceService.getSecret(appServiceDTO, appServiceVersionDTO.getId(), devopsEnvironmentDTO);
        return appServiceDeployVO;
    }

    private MarketInstanceCreationRequestVO getMarketInstanceCreationRequestVO(C7nHelmRelease c7nHelmRelease,
                                                                               Long projectId, Long envId, String filePath, String type) {
        // 查询版本
        MarketServiceDeployObjectVO appServiceVersion;
        if (c7nHelmRelease.getSpec().getMarketDeployObjectId() != null) {
            // 如果有填，从指定id查
            appServiceVersion = marketServiceClientOperator.queryDeployObjectWithValues(projectId, c7nHelmRelease.getSpec().getMarketDeployObjectId());
        } else {
            // 如果没填，从市场服务根据code和版本号查询市场服务版本
            appServiceVersion = marketServiceClientOperator.queryDeployObjectByCodeAndVersion(projectId, c7nHelmRelease.getSpec().getChartName(), c7nHelmRelease.getSpec().getChartVersion());
        }

        validateVersion(appServiceVersion, filePath, c7nHelmRelease);
        String versionValue = appServiceVersion.getValue();
        validateValues(versionValue, filePath);

        // 校验应用服务id是实例的实际应用服务id
        if (c7nHelmRelease.getSpec().getAppServiceId() != null
                && !Objects.equals(appServiceVersion.getMarketServiceId(), c7nHelmRelease.getSpec().getAppServiceId())) {
            throw new GitOpsExplainException(GitOpsObjectError.RELEASE_APP_SERVICE_ID_MISMATCH.getError(), filePath);
        }

        MarketInstanceCreationRequestVO marketInstanceCreationRequestVO = new MarketInstanceCreationRequestVO();
        marketInstanceCreationRequestVO.setNotChanged(false);
        marketInstanceCreationRequestVO.setEnvironmentId(envId);
        marketInstanceCreationRequestVO.setCommandType(type);
        // 设置values为配置库的values和版本的values合并值
        marketInstanceCreationRequestVO.setValues(appServiceInstanceService.getReplaceResult(versionValue, c7nHelmRelease.getSpec().getValues()).getYaml());
        marketInstanceCreationRequestVO.setMarketDeployObjectId(appServiceVersion.getId());
        marketInstanceCreationRequestVO.setMarketAppServiceId(appServiceVersion.getMarketServiceId());
        marketInstanceCreationRequestVO.setInstanceName(c7nHelmRelease.getMetadata().getName());
        marketInstanceCreationRequestVO.setSource(c7nHelmRelease.getSpec().getSource());
        if (AppSourceType.MIDDLEWARE.getValue().equals(c7nHelmRelease.getSpec().getSource())) {
            marketInstanceCreationRequestVO.setChartVersion(appServiceVersion.getMarketServiceVersion());
        } else {
            marketInstanceCreationRequestVO.setChartVersion(appServiceVersion.getDevopsAppServiceVersion());
        }
        if (type.equals(CommandType.UPDATE.getType())) {
            DevopsEnvCommandDTO devopsEnvCommandDTO;
            AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService
                    .baseQueryByCodeAndEnv(c7nHelmRelease.getMetadata().getName(), envId);
            if (appServiceInstanceDTO.getCommandId() == null) {
                devopsEnvCommandDTO = devopsEnvCommandService.baseQueryByObject(ObjectType.INSTANCE.getType(), appServiceInstanceDTO.getId());
            } else {
                devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(appServiceInstanceDTO.getCommandId());
            }
            // 上次部署的传入values
            String lastDeployRawValue = appServiceInstanceService.baseQueryValueByInstanceId(appServiceInstanceDTO.getId());
            // 上次部署values和版本values的合并值
            String lastDeployMergedValues = appServiceInstanceService.getReplaceResult(versionValue, lastDeployRawValue).getYaml();
            // (上次部署和版本的合并值)，和 (这次部署和版本values的合并值) 的对比结果
            InstanceValueVO compareResult = appServiceInstanceService.getReplaceResult(lastDeployMergedValues, marketInstanceCreationRequestVO.getValues());
            if (lastDeployRawValue != null
                    && (compareResult.getDeltaYaml() == null || compareResult.getDeltaYaml().equals("") || compareResult.getDeltaYaml().trim().equals(COMPARE_VALUES))
                    && Objects.equals(appServiceVersion.getId(), devopsEnvCommandDTO.getObjectVersionId())) {
                marketInstanceCreationRequestVO.setNotChanged(true);
            }
            marketInstanceCreationRequestVO.setInstanceId(appServiceInstanceDTO.getId());
            marketInstanceCreationRequestVO.setCommandId(appServiceInstanceDTO.getCommandId());
        } else {
            marketInstanceCreationRequestVO.setAppName(c7nHelmRelease.getMetadata().getName());
            marketInstanceCreationRequestVO.setAppCode(c7nHelmRelease.getMetadata().getName());
        }
        return marketInstanceCreationRequestVO;
    }

    private void validateValues(String values, String filePath) {
        if (values == null) {
            throw new GitOpsExplainException(GitOpsObjectError.MARKET_APP_SERVICE_VERSION_VALUES_NULL.getError(), filePath);
        }
    }

    /**
     * 根据配置文件，尝试找到要使用的应用服务的版本
     *
     * @param c7nHelmRelease 配置文件内容
     * @param projectId      环境所属的项目id
     * @param tenantId       环境所属的组织id
     * @param filePath       配置文件所在的文件路径
     * @param commandType    操作类型
     * @param envId          环境id
     * @return 找到的版本
     * @throws GitOpsExplainException 如果找不到版本或者配置文件中指定的应用服务id不正确
     */
    private AppServiceVersionDTO findVersion(C7nHelmRelease c7nHelmRelease, Long projectId, Long tenantId, String filePath, String commandType, Long envId) {
        AppServiceVersionDTO result;
        String chartName = c7nHelmRelease.getSpec().getChartName();
        String chartVersion = c7nHelmRelease.getSpec().getChartVersion();
        Long appServiceId = c7nHelmRelease.getSpec().getAppServiceId();

        if (appServiceId != null) {
            // 如果配置库传入了应用服务id，首先尝试使用这个id去查是否有符合条件的版本
            result = tryFindVersionWithAppServiceId(appServiceId, chartVersion, projectId, tenantId, filePath);
        } else {
            result = tryFindVersionWithoutAppServiceId(chartName, chartVersion, projectId, tenantId);
        }

        // 如果本身是使用共享规则共享的版本部署的实例，后续共享规则删除后，也允许实例修改values更新实例时,继续使用这个版本
        if (result == null && CommandType.UPDATE.getType().equals(commandType)) {
            AppServiceInstanceDTO appServiceInstanceDTO = appServiceInstanceService
                    .baseQueryByCodeAndEnv(c7nHelmRelease.getMetadata().getName(), envId);
            // 查出上次部署的版本
            AppServiceVersionDTO lastVersion = appServiceInstanceService.queryVersion(appServiceInstanceDTO.getId());
            // 如果上次版本和这次版本一致, 允许这次部署, 用的是上次部署的版本
            if (Objects.equals(lastVersion.getVersion(), c7nHelmRelease.getSpec().getChartVersion())) {
                result = lastVersion;
            }
        }

        // 校验版本不为空
        validateVersion(result, filePath, c7nHelmRelease);

        // 校验应用服务id是实例的实际应用服务id
        if (c7nHelmRelease.getSpec().getAppServiceId() != null
                && !Objects.equals(result.getAppServiceId(), c7nHelmRelease.getSpec().getAppServiceId())) {
            throw new GitOpsExplainException(GitOpsObjectError.RELEASE_APP_SERVICE_ID_MISMATCH.getError(), filePath);
        }
        return result;
    }

    /**
     * 尝试通过配置文件指定的应用服务id找到要部署的版本
     *
     * @param appServiceId 配置文件中指定的应用服务id
     * @param version      配置文件中指定的版本
     * @param projectId    这个环境所属的项目id
     * @param tenantId     环境所属的组织id
     * @param filePath     配置文件路径
     * @return 可能找到的版本
     */
    @Nullable
    private AppServiceVersionDTO tryFindVersionWithAppServiceId(Long appServiceId, String version, Long projectId, Long tenantId, String filePath) {
        LOGGER.debug("Try to find version with app service id specified. appServiceId: {}, chartVersion: {}, projectId: {}, tenantId: {}", appServiceId, version, projectId, tenantId);
        AppServiceDTO appServiceDTO = appServiceMapper.selectByPrimaryKey(appServiceId);
        // 应用服务不存在时报错
        if (appServiceDTO == null) {
            throw new GitOpsExplainException(GitOpsObjectError.RELEASE_APP_SERVICE_ID_NOT_EXIST.getError(), filePath);
        }

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(appServiceDTO.getProjectId());
        // if 找到的应用服务所属的组织id equals 要使用版本的项目的组织id，才进一步寻找，否则返回null
        if (tenantId.equals(projectDTO.getOrganizationId())) {
            return tryFindVersionByAppService(appServiceDTO, version, projectId);
        } else {
            return null;
        }
    }

    /**
     * 从指定的应用服务下找到可以使用的版本
     *
     * @param appServiceDTO 应用服务信息
     * @param version       指定的版本
     * @param projectId     环境所属的项目id
     * @return 可能找到的版本
     */
    @Nullable
    private AppServiceVersionDTO tryFindVersionByAppService(AppServiceDTO appServiceDTO, String version, Long projectId) {
        LOGGER.debug("try find version by app service... appServiceId {} version {}", appServiceDTO.getId(), version);
        AppServiceVersionDTO appServiceVersionDTO = appServiceVersionService.baseQueryByAppServiceIdAndVersion(appServiceDTO.getId(), version);
        if (appServiceVersionDTO != null) {
            LOGGER.debug("Found version by app service id {} and version version {}", appServiceDTO.getId(), version);
            // 如果不等于空，校验下是否有这个版本的权限
            if (projectId.equals(appServiceDTO.getProjectId())) {
                LOGGER.debug("The version is in this project, so return");
                return appServiceVersionDTO;
                // 查询有没有共享规则，有权限的话，就返回这个版本
            } else {
                if (appServiceShareRuleService.hasAccessByShareRule(appServiceVersionDTO, projectId)) {
                    LOGGER.debug("The version is shared by other project, so return");
                    return appServiceVersionDTO;
                }
            }
        }
        return null;
    }

    /**
     * 在没有在配置文件中指定应用服务id的前提下，尝试找到要使用的版本
     *
     * @param chartName    配置文件中指定的chartName
     * @param chartVersion 配置文件中指定的chartVersion
     * @param projectId    环境所属的项目id
     * @param tenantId     环境所属的组织id
     * @return 可能找到的版本
     */
    @Nullable
    private AppServiceVersionDTO tryFindVersionWithoutAppServiceId(String chartName, String chartVersion, Long projectId, Long tenantId) {
        LOGGER.debug("Try to find version without app service id specified. chartName: {}, chartVersion: {}, projectId: {}, tenantId: {}", chartName, chartVersion, projectId, tenantId);
        // 尝试从项目下找
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByCode(chartName, projectId);
        AppServiceVersionDTO result = null;
        // 如果项目下有这个服务
        if (appServiceDTO != null) {
            result = tryFindVersionByAppService(appServiceDTO, chartVersion, projectId);
        }

        // 如果项目下没有这个服务或者，项目下的服务没有这个版本
        if (result == null) {
            // 如果项目下没有这个服务
            // 查询组织下的所有项目中有这个版本名的应用服务
            // 先查询所有的项目id
            List<ProjectDTO> projects = baseServiceClientOperator.listIamProjectByOrgId(tenantId);
            if (!CollectionUtils.isEmpty(projects)) {
                Set<Long> projectIds = projects.stream().map(ProjectDTO::getId).collect(Collectors.toSet());
                // 查询出有这个版本的这个组织下的名为chartName的应用服务
                List<AppServiceDTO> appServices = appServiceMapper.inProjectsAndHavingVersion(projectIds, chartName, chartVersion);
                if (!CollectionUtils.isEmpty(appServices)) {
                    // 尝试从中找出一个具有权限的版本
                    for (AppServiceDTO app : appServices) {
                        result = tryFindVersionByAppService(app, chartVersion, projectId);
                        if (result != null) {
                            return result;
                        }
                    }
                } else {
                    LOGGER.info("There isn't an app service that has matched version");
                }
            } else {
                LOGGER.warn("The projects is empty in org with id {}, which is impossible", tenantId);
            }
        }
        LOGGER.info("Without app service id specified. version found or not: {}, chartName: {}, chartVersion: {}, projectId: {}, tenantId: {}", result != null, chartName, chartVersion, projectId, tenantId);
        return result;
    }
}
