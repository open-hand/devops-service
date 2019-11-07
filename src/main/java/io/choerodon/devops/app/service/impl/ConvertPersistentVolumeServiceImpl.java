package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.util.GitOpsUtil.*;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1PersistentVolume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsPvServcie;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
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
    @Autowired
    private DevopsPvServcie devopsPvServcie;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

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
        if (pv.getSpec() == null || pv.getSpec().getCapacity() == null) {
            throw new GitOpsExplainException(
                    GitOpsObjectError.PERSISTENT_VOLUME_CAPACITY_NOT_FOUND.getError(), filePath);
        } else if (pv.getSpec().getCapacity().get(KubernetesConstants.STORAGE) == null) {
            throw new GitOpsExplainException(
                    GitOpsObjectError.PERSISTENT_VOLUME_STORAGE_NOT_FOUND.getError(), filePath);
        }
        if (pv.getSpec().getAccessModes() == null) {
            throw new CommonException(
                    GitOpsObjectError.PERSISTENT_VOLUME_ACCESS_MODE_NOT_FOUND.getError(), filePath);
        }
        if (pv.getSpec().getAccessModes().size() > 1) {
            throw new CommonException(
                    GitOpsObjectError.PERSISTENT_VOLUME_ACCESS_MODE_SIZE_NOT_SUPPORTED.getError(), filePath);
        }
    }

    @Override
    public void checkIfExist(List<V1PersistentVolume> v1PersistentVolumes, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, V1PersistentVolume pv) {
        String filePath = objectPath.get(TypeUtil.objToString(pv.hashCode()));
        DevopsPvDTO devopsPvDTO = devopsPvServcie.queryByEnvIdAndName(envId, pv.getMetadata().getName());
        if (devopsPvDTO != null
                && !isDeletedByCurrentOperation(beforeSyncDelete, devopsPvDTO.getId(), ResourceType.PERSISTENT_VOLUME)) {
            checkNotExistInDb(devopsEnvFileResourceService.baseQueryByEnvIdAndResourceId(envId, devopsPvDTO.getId(), pv.getKind()), filePath, pv.getMetadata().getName());
        }

        if (isContainedByList(v1PersistentVolumes, pv, obj -> obj.getMetadata().getName())) {
            throwExistEx(filePath, pv.getMetadata().getName());
        }

        v1PersistentVolumes.add(pv);
    }

    @Override
    public ResourceType getType() {
        return ResourceType.PERSISTENT_VOLUME;
    }
}
