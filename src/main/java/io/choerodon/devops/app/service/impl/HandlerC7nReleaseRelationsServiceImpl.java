package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ApplicationDeployVO;
import io.choerodon.devops.api.vo.ApplicationInstanceVO;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.app.service.ApplicationInstanceService;
import io.choerodon.devops.app.service.DeployMsgHandlerService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.domain.application.valueobject.InstanceValueVO;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.enums.ObjectType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.models.V1Endpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class HandlerC7nReleaseRelationsServiceImpl implements HandlerObjectFileRelationsService<C7nHelmRelease> {

    private static final String C7NHELM_RELEASE = "C7NHelmRelease";
    private static final String GIT_SUFFIX = "/.git";
    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DeployMsgHandlerService deployMsgHandlerService;
    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private DevopsServiceInstanceRepository devopsServiceInstanceRepository;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceVO> beforeSync, List<C7nHelmRelease> c7nHelmReleases, List<V1Endpoints> v1Endpoints, Long envId, Long projectId, String path, Long userId) {
        List<String> beforeC7nRelease = beforeSync.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(C7NHELM_RELEASE))
                .map(devopsEnvFileResourceE -> {
                    ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                            .selectById(devopsEnvFileResourceE.getResourceId());
                    if (applicationInstanceE == null) {
                        devopsEnvFileResourceRepository
                                .baseDeleteByEnvIdAndResourceId(envId, devopsEnvFileResourceE.getResourceId(), C7NHELM_RELEASE);
                        return null;
                    }
                    return applicationInstanceE.getCode();
                }).collect(Collectors.toList());

        //比较已存在实例和新增要处理的实例,获取新增实例，更新实例，删除实例
        List<C7nHelmRelease> addC7nHelmRelease = new ArrayList<>();
        List<C7nHelmRelease> updateC7nHelmRelease = new ArrayList<>();
        c7nHelmReleases.stream().forEach(c7nHelmRelease -> {
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
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectByCode(releaseName, envId);
            if (applicationInstanceE != null) {
                applicationInstanceService.instanceDeleteByGitOps(applicationInstanceE.getId());
                devopsEnvFileResourceRepository
                        .baseDeleteByEnvIdAndResourceId(envId, applicationInstanceE.getId(), C7NHELM_RELEASE);
            }
        });
    }


    private void updateC7nHelmRelease(Map<String, String> objectPath, Long envId, Long projectId, List<C7nHelmRelease> updateC7nHelmRelease, String path, Long userId) {
        updateC7nHelmRelease.stream()
                .forEach(c7nHelmRelease -> {
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
                                DevopsEnvCommandVO devopsEnvCommandE = devopsEnvCommandRepository.query(applicationDeployVO.getCommandId());
                                if (!applicationDeployVO.getIsNotChange()) {
                                    ApplicationInstanceVO applicationInstanceVO = applicationInstanceService
                                            .createOrUpdateByGitOps(applicationDeployVO, userId);
                                    devopsEnvCommandE = devopsEnvCommandRepository.query(applicationInstanceVO.getCommandId());
                                }

                                devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                                devopsEnvCommandRepository.update(devopsEnvCommandE);
                                DevopsEnvFileResourceVO devopsEnvFileResourceE = devopsEnvFileResourceRepository
                                        .baseQueryByEnvIdAndResourceId(envId, applicationDeployVO.getAppInstanceId(), c7nHelmRelease.getKind());
                                devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId,
                                        devopsEnvFileResourceE,
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
                        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                                .selectByCode(c7nHelmRelease.getMetadata().getName(), envId);
                        ApplicationDeployVO applicationDeployVO;

                        ApplicationInstanceVO applicationInstanceVO = new ApplicationInstanceVO();
                        //初始化实例参数,创建时判断实例是否存在，存在则直接创建文件对象关联关系
                        if (applicationInstanceE == null) {
                            applicationDeployVO = getApplicationDeployDTO(
                                    c7nHelmRelease,
                                    projectId,
                                    envId,
                                    filePath,
                                    "create");
                            applicationInstanceVO = applicationInstanceService.createOrUpdateByGitOps(applicationDeployVO, userId);
                        } else {
                            applicationInstanceVO.setId(applicationInstanceE.getId());
                            applicationInstanceVO.setCommandId(applicationInstanceE.getCommandId());
                        }
                        DevopsEnvCommandVO devopsEnvCommandE = devopsEnvCommandRepository.query(applicationInstanceVO.getCommandId());


                        List<DevopsServiceAppInstanceE> devopsServiceAppInstanceES = devopsServiceInstanceRepository.baseListByEnvIdAndInstanceCode(envId, c7nHelmRelease.getMetadata().getName());

                        //删除实例之后，重新创建同名的实例，如果之前的实例关联的网络，此时需要把网络关联上新的实例
                        Long instanceId = applicationInstanceVO.getId();
                        if (devopsServiceAppInstanceES != null && !devopsServiceAppInstanceES.isEmpty()) {
                            devopsServiceAppInstanceES.stream().filter(devopsServiceAppInstanceE -> !devopsServiceAppInstanceE.getAppInstanceId().equals(instanceId)).forEach(devopsServiceAppInstanceE -> {
                                devopsServiceInstanceRepository.baseUpdateInstanceId(devopsServiceAppInstanceE.getId(), instanceId);
                            });
                        }

                        devopsEnvCommandE.setSha(GitUtil.getFileLatestCommit(path + GIT_SUFFIX, filePath));
                        devopsEnvCommandRepository.update(devopsEnvCommandE);

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
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        OrganizationVO organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        List<ApplicationE> applications = deployMsgHandlerService.getApplication(c7nHelmRelease.getSpec().getChartName(), projectId, organization.getId());
        if (applications.isEmpty()) {
            throw new GitOpsExplainException("app.not.exist.in.database", filePath, c7nHelmRelease.getSpec().getChartName());
        }
        ApplicationVersionE applicationVersionE = null;
        ApplicationE applicationE = null;
        for (ApplicationE application : applications) {
            applicationVersionE = applicationVersionRepository
                    .baseQueryByAppIdAndVersion(application.getId(), c7nHelmRelease.getSpec().getChartVersion());
            if (applicationVersionE != null) {
                applicationE = application;
                break;
            }
        }

        if (applicationVersionE == null) {
            throw new GitOpsExplainException("appversion.not.exist.in.database", filePath, c7nHelmRelease.getSpec().getChartVersion());
        }

        String versionValue = applicationVersionRepository.baseQueryValue(applicationVersionE.getId());
        ApplicationDeployVO applicationDeployVO = new ApplicationDeployVO();
        applicationDeployVO.setEnvironmentId(envId);
        applicationDeployVO.setType(type);
        applicationDeployVO.setValues(applicationInstanceService.getReplaceResult(versionValue, c7nHelmRelease.getSpec().getValues()).getYaml());
        applicationDeployVO.setAppId(applicationE.getId());
        applicationDeployVO.setAppVersionId(applicationVersionE.getId());
        applicationDeployVO.setInstanceName(c7nHelmRelease.getMetadata().getName());
        if (type.equals("update")) {
            DevopsEnvCommandVO devopsEnvCommandE;
            ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                    .selectByCode(c7nHelmRelease.getMetadata().getName(), envId);
            if (applicationInstanceE.getCommandId() == null) {
                devopsEnvCommandE = devopsEnvCommandRepository.baseQueryByObject(ObjectType.INSTANCE.getType(), applicationInstanceE.getId());
            } else {
                devopsEnvCommandE = devopsEnvCommandRepository.query(applicationInstanceE.getCommandId());
            }
            String deployValue = applicationInstanceRepository.queryValueByInstanceId(applicationInstanceE.getId());
            InstanceValueVO instanceValueVO = applicationInstanceService.getReplaceResult(deployValue, applicationDeployVO.getValues());
            if (deployValue != null && instanceValueVO.getNewLines().isEmpty() && applicationVersionE.getId().equals(devopsEnvCommandE.getObjectVersionId())) {
                applicationDeployVO.setIsNotChange(true);
            }
            applicationDeployVO.setAppInstanceId(applicationInstanceE.getId());
            applicationDeployVO.setCommandId(applicationInstanceE.getCommandId());
        }
        return applicationDeployVO;
    }
}
