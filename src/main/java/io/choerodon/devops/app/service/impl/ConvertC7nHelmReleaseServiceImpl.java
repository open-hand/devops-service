package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.api.vo.iam.entity.ApplicationInstanceE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.GitOpsObjectError;

public class ConvertC7nHelmReleaseServiceImpl extends ConvertK8sObjectService<C7nHelmRelease> {

    private ApplicationInstanceRepository applicationInstanceRepository;
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    public ConvertC7nHelmReleaseServiceImpl() {
        this.applicationInstanceRepository = ApplicationContextHelper.getSpringFactory().getBean(ApplicationInstanceRepository.class);
        this.devopsEnvFileResourceRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceRepository.class);
    }

    @Override
    public void checkParameters(C7nHelmRelease c7nHelmRelease, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode()));
        if (c7nHelmRelease.getMetadata() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.RELEASE_META_DATA_NOT_FOUND.getError(), filePath);
        } else {
            if (c7nHelmRelease.getMetadata().getName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.RELEASE_NAME_NOT_FOUND.getError(), filePath);
            }
        }
        if (c7nHelmRelease.getSpec() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.RELEASE_SPEC_NOT_FOUND.getError(), filePath);
        } else {
            if (c7nHelmRelease.getSpec().getChartName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.RELEASE_CHART_NAME_NOT_FOUND.getError(), filePath);
            }
            if (c7nHelmRelease.getSpec().getChartVersion() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.RELEASE_CHART_VERSION_NOT_FOUND.getError(), filePath);
            }
            if (c7nHelmRelease.getSpec().getRepoUrl() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.RELEASE_REPO_URL_NOT_FOUND.getError(), filePath);
            }
        }
        if (c7nHelmRelease.getApiVersion() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.RELEASE_API_VERSION_NOT_FOUND.getError(), filePath);
        }

    }

    @Override
    public void checkIfExist(List<C7nHelmRelease> c7nHelmReleases, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, C7nHelmRelease c7nHelmRelease) {
        String filePath = objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode()));
        String instanceCode = c7nHelmRelease.getMetadata().getName();
        ApplicationInstanceE applicationInstanceE = applicationInstanceRepository.selectByCode(instanceCode, envId);
        if (applicationInstanceE != null) {
            Long instanceId = applicationInstanceE.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType()
                            .equals(c7nHelmRelease.getKind()))
                    .noneMatch(devopsEnvFileResourceE ->
                            devopsEnvFileResourceE.getResourceId()
                                    .equals(instanceId))) {
                DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, instanceId, c7nHelmRelease.getKind());
                if (devopsEnvFileResourceE != null && !devopsEnvFileResourceE.getFilePath().equals(objectPath.get(TypeUtil.objToString(c7nHelmRelease.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, instanceCode);
                }
            }
        }
        if (c7nHelmReleases.stream()
                .anyMatch(c7nHelmRelease1 -> c7nHelmRelease1.getMetadata().getName()
                        .equals(instanceCode))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, instanceCode);
        } else {
            c7nHelmReleases.add(c7nHelmRelease);
        }
    }
}
