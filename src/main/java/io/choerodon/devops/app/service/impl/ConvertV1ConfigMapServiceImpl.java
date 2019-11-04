package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1ConfigMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsConfigMapService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.infra.dto.DevopsConfigMapDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;

@Component
public class ConvertV1ConfigMapServiceImpl extends ConvertK8sObjectService<V1ConfigMap> {

    @Autowired
    private DevopsConfigMapService devopsConfigMapService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    public ConvertV1ConfigMapServiceImpl() {
        super(V1ConfigMap.class);
    }

    @Override
    public void checkIfExist(List<V1ConfigMap> v1ConfigMaps, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, V1ConfigMap v1ConfigMap) {
        String filePath = objectPath.get(TypeUtil.objToString(v1ConfigMap.hashCode()));
        DevopsConfigMapDTO devopsConfigMapDTO = devopsConfigMapService.baseQueryByEnvIdAndName(envId, v1ConfigMap.getMetadata().getName());
        if (devopsConfigMapDTO != null &&
                beforeSyncDelete.stream()
                        .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType().equals(v1ConfigMap.getKind()))
                        .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(devopsConfigMapDTO.getId()))) {
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService.baseQueryByEnvIdAndResourceId(envId, devopsConfigMapDTO.getId(), v1ConfigMap.getKind());
            if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath().equals(objectPath.get(TypeUtil.objToString(v1ConfigMap.hashCode())))) {
                throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, v1ConfigMap.getMetadata().getName());
            }
        }
        if (v1ConfigMaps.stream().anyMatch(v1Service1 -> v1Service1.getMetadata().getName().equals(v1ConfigMap.getMetadata().getName()))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, v1ConfigMap.getMetadata().getName());
        } else {
            v1ConfigMaps.add(v1ConfigMap);
        }
    }

    @Override
    public ResourceType getType() {
        return ResourceType.CONFIGMAP;
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
