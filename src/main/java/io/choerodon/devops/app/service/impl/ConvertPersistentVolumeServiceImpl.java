package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1PersistentVolume;
import org.springframework.stereotype.Component;

import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * @author zmf
 * @since 11/6/19
 */
@Component
public class ConvertPersistentVolumeServiceImpl extends ConvertK8sObjectService<V1PersistentVolume> {
    public ConvertPersistentVolumeServiceImpl() {
        super(V1PersistentVolume.class);
    }

    @Override
    public void checkParameters(V1PersistentVolume pv, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(pv.hashCode()));
        if (pv.getMetadata() == null) {
            throw new GitOpsExplainException(
                    GitOpsObjectError.PERSISTENT_VOLUME_METADATA_NOT_FOUND.getError(), filePath);
        }
        if (pv.getMetadata().getName() == null) {
            throw new GitOpsExplainException(
                    GitOpsObjectError.PERSISTENT_VOLUME_NAME_NOT_FOUND.getError(), filePath);
        }
    }

    @Override
    public void checkIfExist(List<V1PersistentVolume> v1PersistentVolumes, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, V1PersistentVolume v1PersistentVolume) {
        super.checkIfExist(v1PersistentVolumes, envId, beforeSyncDelete, objectPath, v1PersistentVolume);
    }

    @Override
    public ResourceType getType() {
        return ResourceType.PERSISTENT_VOLUME;
    }
}
