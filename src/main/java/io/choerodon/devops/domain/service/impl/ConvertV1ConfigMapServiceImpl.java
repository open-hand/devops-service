package io.choerodon.devops.domain.service.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1ConfigMap;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsConfigMapE;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsConfigMapRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.service.ConvertK8sObjectService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;

public class ConvertV1ConfigMapServiceImpl extends ConvertK8sObjectService<V1ConfigMap> {

    private DevopsConfigMapRepository devopsConfigMapRepository;
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    public ConvertV1ConfigMapServiceImpl() {
        this.devopsConfigMapRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsConfigMapRepository.class);
        this.devopsEnvFileResourceRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceRepository.class);
    }

    @Override
    public void checkIfExist(List<V1ConfigMap> v1ConfigMaps, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, V1ConfigMap v1ConfigMap) {
        String filePath = objectPath.get(TypeUtil.objToString(v1ConfigMap.hashCode()));
        DevopsConfigMapE devopsConfigMapE = devopsConfigMapRepository.queryByEnvIdAndName(envId, v1ConfigMap.getMetadata().getName());
        if (devopsConfigMapE != null &&
                beforeSyncDelete.stream()
                        .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(v1ConfigMap.getKind()))
                        .noneMatch(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceId().equals(devopsConfigMapE.getId()))) {
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, devopsConfigMapE.getId(), v1ConfigMap.getKind());
            if (devopsEnvFileResourceE != null && !devopsEnvFileResourceE.getFilePath().equals(objectPath.get(TypeUtil.objToString(v1ConfigMap.hashCode())))) {
                throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, v1ConfigMap.getMetadata().getName(), null);
            }
        }
        if (v1ConfigMaps.stream().anyMatch(v1Service1 -> v1Service1.getMetadata().getName().equals(v1ConfigMap.getMetadata().getName()))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, v1ConfigMap.getMetadata().getName(), null);
        } else {
            v1ConfigMaps.add(v1ConfigMap);
        }
    }

    @Override
    public void checkParameters(V1ConfigMap v1ConfigMap, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(v1ConfigMap.hashCode()));
        if (v1ConfigMap.getMetadata() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CONFIG_MAP_METADATA_NOT_FOUND.getError(), filePath);
        } else {
            if (v1ConfigMap.getMetadata().getName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.CONFIG_MAP_NAME_NOT_FOUND.getError(), filePath);
            }
        }
        if (v1ConfigMap.getData() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CONFIG_MAP_DATA_NOT_FOUND.getError(), filePath);
        }
    }

}
