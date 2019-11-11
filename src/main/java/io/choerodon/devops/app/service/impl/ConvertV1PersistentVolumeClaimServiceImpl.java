package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.util.GitOpsUtil.*;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1PersistentVolumeClaim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsPvcService;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsPvcDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * 转化和校验{@link V1PersistentVolumeClaim}
 *
 * @author zmf
 * @since 11/4/19
 */
@Component
public class ConvertV1PersistentVolumeClaimServiceImpl extends ConvertK8sObjectService<V1PersistentVolumeClaim> {
    @Autowired
    private DevopsPvcService devopsPvcService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    public ConvertV1PersistentVolumeClaimServiceImpl() {
        super(V1PersistentVolumeClaim.class);
    }

    @Override
    public void checkIfExist(List<V1PersistentVolumeClaim> claims, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, V1PersistentVolumeClaim claim) {
        String filePath = objectPath.get(TypeUtil.objToString(claim.hashCode()));
        DevopsPvcDTO devopsPvcDTO = devopsPvcService.queryByEnvIdAndName(envId, claim.getMetadata().getName());
        if (devopsPvcDTO != null
                && !isDeletedByCurrentOperation(beforeSyncDelete, devopsPvcDTO.getId(), ResourceType.PERSISTENT_VOLUME_CLAIM)) {
            checkNotExistInDb(devopsEnvFileResourceService.baseQueryByEnvIdAndResourceId(envId, devopsPvcDTO.getId(), claim.getKind()), filePath, claim.getMetadata().getName());
        }

        if (isContainedByList(claims, claim, obj -> obj.getMetadata().getName())) {
            throwExistEx(filePath, claim.getMetadata().getName());
        }

        claims.add(claim);
    }


    @Override
    public void checkParameters(V1PersistentVolumeClaim claim, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(claim.hashCode()));
        if (claim.getMetadata() == null) {
            throw new GitOpsExplainException(
                    GitOpsObjectError.PERSISTENT_VOLUME_CLAIM_METADATA_NOT_FOUND.getError(), filePath);
        } else {
            if (claim.getMetadata().getName() == null) {
                throw new GitOpsExplainException(
                        GitOpsObjectError.PERSISTENT_VOLUME_CLAIM_NAME_NOT_FOUND.getError(), filePath);
            }
        }

        if (claim.getSpec() == null || CollectionUtils.isEmpty(claim.getSpec().getAccessModes())) {
            throw new GitOpsExplainException(
                    GitOpsObjectError.PERSISTENT_VOLUME_CLAIM_ACCESS_MODE_NOT_FOUND.getError(), filePath);
        } else if (claim.getSpec().getAccessModes().size() > 1) {
            throw new GitOpsExplainException(
                    GitOpsObjectError.PERSISTENT_VOLUME_CLAIM_ACCESS_MODE_SIZE_NOT_SUPPORTED.getError(), filePath);
        }

        if (claim.getSpec().getResources() == null || claim.getSpec().getResources().getRequests() == null) {
            throw new GitOpsExplainException(
                    GitOpsObjectError.PERSISTENT_VOLUME_CLAIM_RESOURCE_REQUEST_NOT_FOUND.getError(), filePath);
        }
    }

    @Override
    public ResourceType getType() {
        return ResourceType.PERSISTENT_VOLUME_CLAIM;
    }
}
