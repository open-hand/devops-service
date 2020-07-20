package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsPvService;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.models.V1PersistentVolume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.choerodon.devops.infra.util.GitOpsUtil.*;

/**
 * @author zmf
 * @since 11/6/19
 */
@Component
public class ConvertPersistentVolumeServiceImpl extends ConvertK8sObjectService<V1PersistentVolume> {
    /**
     * IP的正则表达式
     */
    private static final Pattern IP_PATTERN = Pattern.compile("^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$");

    @Autowired
    private DevopsPvService devopsPvService;
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

        validateType(pv, filePath);
    }

    private void validateType(V1PersistentVolume pv, String filePath) {
        // 支持NFS、HostPath、LocalPV类型的网络
        int typeCount = countTypeDefined(pv);
        if (typeCount == 0) {
            throw new CommonException(
                    GitOpsObjectError.PERSISTENT_VOLUME_TYPE_NOT_FOUND.getError(), filePath);
        }

        if (typeCount > 1) {
            throw new CommonException(
                    GitOpsObjectError.PERSISTENT_VOLUME_TYPE_MULTI.getError(), filePath);
        }

        validateNFS(pv, filePath);
        validateHostPath(pv, filePath);
        validateLocal(pv, filePath);
    }

    private int countTypeDefined(V1PersistentVolume pv) {
        int typeCount = 0;
        if (pv.getSpec().getNfs() != null) {
            typeCount++;
        }
        if (pv.getSpec().getHostPath() != null) {
            typeCount++;
        }
        if (pv.getSpec().getLocal() != null) {
            typeCount++;
        }
        return typeCount;
    }

    private void validateNFS(V1PersistentVolume pv, String filePath) {
        if (pv.getSpec().getNfs() == null) {
            return;
        }
        if (pv.getSpec().getNfs().getPath() == null) {
            throw new CommonException(
                    GitOpsObjectError.PERSISTENT_VOLUME_NFS_PATH_NOT_FOUND.getError(), filePath);
        }
        if (StringUtils.isEmpty(pv.getSpec().getNfs().getServer())) {
            throw new CommonException(
                    GitOpsObjectError.PERSISTENT_VOLUME_NFS_SERVER_NOT_FOUND.getError(), filePath);
        }
        if (!IP_PATTERN.matcher(pv.getSpec().getNfs().getServer()).matches()) {
            throw new CommonException(
                    GitOpsObjectError.PERSISTENT_VOLUME_NFS_SERVER_NOT_IP.getError(), filePath);
        }
    }

    private void validateHostPath(V1PersistentVolume pv, String filePath) {
        if (pv.getSpec().getHostPath() == null) {
            return;
        }
        if (pv.getSpec().getHostPath().getPath() == null) {
            throw new CommonException(
                    GitOpsObjectError.PERSISTENT_VOLUME_NFS_PATH_NOT_FOUND.getError(), filePath);
        }
    }

    private void validateLocal(V1PersistentVolume pv, String filePath) {
        if (pv.getSpec().getLocal() == null) {
            return;
        }
        if (pv.getSpec().getLocal().getPath() == null) {
            throw new CommonException(
                    GitOpsObjectError.PERSISTENT_VOLUME_NFS_PATH_NOT_FOUND.getError(), filePath);
        }
        try {
            if (pv.getSpec().getNodeAffinity().getRequired().getNodeSelectorTerms().get(0).getMatchExpressions().get(0).getValues().size() != 1) {
                throw new CommonException(GitOpsObjectError.PERSISTENT_VOLUME_LOCAL_PATH_NODE_NAME_MORE_THAN_ONE.getError(), filePath);
            }
        } catch (NullPointerException e) {
            throw new CommonException(GitOpsObjectError.PERSISTENT_VOLUME_LOCAL_PATH_NODE_AFFINITY_NOT_FOUND.getError(), filePath);
        }
    }

    @Override
    public void checkIfExist(List<V1PersistentVolume> v1PersistentVolumes, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, V1PersistentVolume pv) {
        String filePath = objectPath.get(TypeUtil.objToString(pv.hashCode()));
        DevopsPvDTO devopsPvDTO = devopsPvService.queryByEnvIdAndName(envId, pv.getMetadata().getName());
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
