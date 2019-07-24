package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ApplicationDeployVO;
import io.choerodon.devops.api.vo.ApplicationInstanceVO;
import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.api.vo.kubernetes.InstanceValueVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.feign.operator.IamServiceClientOperator;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.models.V1Endpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class HandlerC7nReleaseRelationsServiceImpl implements HandlerObjectFileRelationsService<C7nHelmRelease> {

    private static final String C7N_HELM_RELEASE = "C7NHelmRelease";
    private static final String GIT_SUFFIX = "/.git";
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private IamServiceClientOperator iamServiceClientOperator;
    @Autowired
    private AgentMsgHandlerService agentMsgHandlerService;
    @Autowired
    private ApplicationVersionService applicationVersionService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsServiceInstanceService devopsServiceInstanceService;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceDTO> beforeSync, List<C7nHelmRelease> c7nHelmReleases, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeC7nRelease = beforeSync.stream()
                .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType().equals(C7N_HELM_RELEASE))
                .map(devopsEnvFileResourceDTO -> {
                    ApplicationInstanceDTO applicationInstanceDTO = applicationInstanceService
                            .baseQuery(devopsEnvFileResourceDTO.getResourceId());
                    if (applicationInstanceDTO == null) {
                        devopsEnvFileResourceService
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceDTO.getResourceId(), C7N_HELM_RELEASE);
                        return null;
                    }
                    return applicationInstanceDTO.getCode();
                }).collect(Collectors.toList());

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

        //新增instance
        addC7nHelmRelease(objectPath, envId, projectId, addC7nHelmRelease, path, userId);
        //更新instance
        updateC7nHelmRelease(objectPath, envId, projectId, updateC7nHelmRelease, path, userId);
        //删除instance,和文件对象关联关系
        beforeC7nRelease.forEach(releaseName -> {
            ApplicationInstanceDTO applicationInstanceDTO = applicationInstanceService.baseQueryByCodeAndEnv(releaseName, envId);
            if (applicationInstanceDTO != null) {
                applicationInstanceService.instanceDeleteByGitOps(applicationInstanceDTO.getId());
                devopsEnvFileResourceService
                        .baseDeleteByEnvIdAndResourceId(envId, applicationInstanceDTO.getId(), C7N_HELM_RELEASE);
            }
        });
    }


    private void updateC7nHelmRelease(Map<String, String> objectPath, Long envId, Long projectId, List<C7nHelmRelease> updateC7nHelmRelease, String path, Long userId) {
        updateC7nHelmRelease.forEach(c7nHelmRelease -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode()));
                        //初始化实例参数,更新时判断实例是否真的修改，没有修改则直接更新文件关联关系
                        ApplicationDeployVO applicationDeployVO = getApplicationDeployDTO(
                                c7nHelmRelease,
                                projectId,
                                envId,
                                filePath,
                                "update");
                        if (applicationDeployVO == null) {
                            return;
                        }
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationDeployVO.getCommandId());
                        if (!applicationDeployVO.getIsNotChange()) {
                            ApplicationInstanceVO applicationInstanceVO = applicationInstanceService
                                    .createOrUpdateByGitOps(applicationDeployVO, userId);
                            devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceVO.getCommandId());
                        }

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);
                        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                                .baseQueryByEnvIdAndResourceId(envId, applicationDeployVO.getAppInstanceId(), c7nHelmRelease.getKind());
                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId,
                                devopsEnvFileResourceDTO,
                                c7nHelmRelease.hashCode(), applicationDeployVO.getAppInstanceId(),
                                c7nHelmRelease.getKind());
                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                }
        );
    }

    private void addC7nHelmRelease(Map<String, String> objectPath, Long envId, Long projectId, List<C7nHelmRelease> addC7nHelmRelease, String path, Long userId) {
        addC7nHelmRelease.stream()
                .forEach(c7nHelmRelease -> {
                    String filePath = "";
                    try {
                        filePath = objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode()));
                        ApplicationInstanceDTO applicationInstanceDTO = applicationInstanceService
                                .baseQueryByCodeAndEnv(c7nHelmRelease.getMetadata().getName(), envId);
                        ApplicationDeployVO applicationDeployVO;

                        ApplicationInstanceVO applicationInstanceVO = new ApplicationInstanceVO();
                        //初始化实例参数,创建时判断实例是否存在，存在则直接创建文件对象关联关系
                        if (applicationInstanceDTO == null) {
                            applicationDeployVO = getApplicationDeployDTO(
                                    c7nHelmRelease,
                                    projectId,
                                    envId,
                                    filePath,
                                    "create");
                            applicationInstanceVO = applicationInstanceService.createOrUpdateByGitOps(applicationDeployVO, userId);
                        } else {
                            applicationInstanceVO.setId(applicationInstanceDTO.getId());
                            applicationInstanceVO.setCommandId(applicationInstanceDTO.getCommandId());
                        }
                        DevopsEnvCommandDTO devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceVO.getCommandId());


                        List<DevopsServiceAppInstanceDTO> devopsServiceAppInstanceDTOS = devopsServiceInstanceService.baseListByEnvIdAndInstanceCode(envId, c7nHelmRelease.getMetadata().getName());

                        //删除实例之后，重新创建同名的实例，如果之前的实例关联的网络，此时需要把网络关联上新的实例
                        Long instanceId = applicationInstanceVO.getId();
                        if (devopsServiceAppInstanceDTOS != null && !devopsServiceAppInstanceDTOS.isEmpty()) {
                            devopsServiceAppInstanceDTOS.stream().filter(devopsServiceAppInstanceE -> !devopsServiceAppInstanceE.getAppInstanceId().equals(instanceId)).forEach(devopsServiceAppInstanceE -> {
                                devopsServiceInstanceService.baseUpdateInstanceId(devopsServiceAppInstanceE.getId(), instanceId);
                            });
                        }

                        devopsEnvCommandDTO.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandService.baseUpdate(devopsEnvCommandDTO);

                        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId, null, c7nHelmRelease.hashCode(), instanceId,
                                c7nHelmRelease.getKind());

                    } catch (CommonException e) {
                        String errorCode = "";
                        if (e instanceof GitOpsExplainException) {
                            errorCode = ((GitOpsExplainException) e).getErrorCode() == null ? "" : ((GitOpsExplainException) e).getErrorCode();
                        }
                        throw new GitOpsExplainException(e.getMessage(), filePath, errorCode, e);
                    }
                });
    }


    private ApplicationDeployVO getApplicationDeployDTO(C7nHelmRelease c7nHelmRelease,
                                                        Long projectId, Long envId, String filePath, String type) {
        ProjectDTO projectDTO = iamServiceClientOperator.queryIamProjectById(projectId);
        OrganizationDTO organization = iamServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());

        List<ApplicationDTO> applications = agentMsgHandlerService.getApplication(c7nHelmRelease.getSpec().getChartName(), projectId, organization.getId());

        if (applications.isEmpty()) {
            throw new GitOpsExplainException("app.not.exist.in.database", filePath, c7nHelmRelease.getSpec().getChartName());
        }
        ApplicationVersionDTO applicationVersionDTO = null;
        ApplicationDTO applicationDTO = null;
        for (ApplicationDTO application : applications) {
            applicationVersionDTO = applicationVersionService
                    .baseQueryByAppIdAndVersion(application.getId(), c7nHelmRelease.getSpec().getChartVersion());
            if (applicationVersionDTO != null) {
                applicationDTO = application;
                break;
            }
        }

        if (applicationVersionDTO == null) {
            throw new GitOpsExplainException("appversion.not.exist.in.database", filePath, c7nHelmRelease.getSpec().getChartVersion());
        }

        String versionValue = applicationVersionService.baseQueryValue(applicationVersionDTO.getId());
        ApplicationDeployVO applicationDeployVO = new ApplicationDeployVO();
        applicationDeployVO.setEnvironmentId(envId);
        applicationDeployVO.setType(type);
        applicationDeployVO.setValues(applicationInstanceService.getReplaceResult(versionValue, c7nHelmRelease.getSpec().getValues()).getYaml());
        applicationDeployVO.setAppId(applicationDTO.getId());
        applicationDeployVO.setAppVersionId(applicationVersionDTO.getId());
        applicationDeployVO.setInstanceName(c7nHelmRelease.getMetadata().getName());
        if (type.equals("update")) {
            DevopsEnvCommandDTO devopsEnvCommandDTO;
            ApplicationInstanceDTO applicationInstanceDTO = applicationInstanceService
                    .baseQueryByCodeAndEnv(c7nHelmRelease.getMetadata().getName(), envId);
            if (applicationInstanceDTO.getCommandId() == null) {
                devopsEnvCommandDTO = devopsEnvCommandService.baseQueryByObject(ObjectType.INSTANCE.getType(), applicationInstanceDTO.getId());
            } else {
                devopsEnvCommandDTO = devopsEnvCommandService.baseQuery(applicationInstanceDTO.getCommandId());
            }
            String deployValue = applicationInstanceService.baseQueryValueByInstanceId(applicationInstanceDTO.getId());
            InstanceValueVO instanceValueVO = applicationInstanceService.getReplaceResult(deployValue, applicationDeployVO.getValues());
            if (deployValue != null && instanceValueVO.getNewLines().isEmpty() && applicationVersionDTO.getId().equals(devopsEnvCommandDTO.getObjectVersionId())) {
                applicationDeployVO.setIsNotChange(true);
            }
            applicationDeployVO.setAppInstanceId(applicationInstanceDTO.getId());
            applicationDeployVO.setCommandId(applicationInstanceDTO.getCommandId());
        }
        return applicationDeployVO;
    }
}
