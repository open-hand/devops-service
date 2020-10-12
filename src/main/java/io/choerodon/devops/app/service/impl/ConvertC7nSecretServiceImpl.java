package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1Secret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsSecretService;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsSecretDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 下午7:31
 * Description:
 */
@Component
public class ConvertC7nSecretServiceImpl extends ConvertK8sObjectService<V1Secret> {
    @Autowired
    private DevopsSecretService devopsSecretService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    public ConvertC7nSecretServiceImpl() {
        super(V1Secret.class);
    }

    @Override
    public void checkParameters(V1Secret v1Secret, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(v1Secret.hashCode()));
        if (v1Secret.getApiVersion() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.SECRET_API_VERSION_NOT_FOUND.getError(), filePath);
        } else {
            if (v1Secret.getType() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.SECRET_TYPE_NOT_FOUND.getError(), filePath);
            }
            if (v1Secret.getMetadata().getName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.SECRET_NAME_NOT_FOUND.getError(), filePath);
            }
            if (v1Secret.getType().equals("Opaque")) {
                // 两种数据都没有, 那说明没给数据
                if (CollectionUtils.isEmpty(v1Secret.getStringData())
                        && CollectionUtils.isEmpty(v1Secret.getData())) {
                    throw new GitOpsExplainException(GitOpsObjectError.SECRET_DATA_NOT_FOUND.getError(), filePath);
                }
            } else {
                if (v1Secret.getData() == null || v1Secret.getData().size() == 0) {
                    throw new GitOpsExplainException(GitOpsObjectError.SECRET_DATA_NOT_FOUND.getError(), filePath);
                }
            }
        }
    }

    @Override
    public void checkIfExist(List<V1Secret> c7nSecrets, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete,
                             Map<String, String> objectPath, V1Secret v1Secret) {
        String filePath = objectPath.get(TypeUtil.objToString(v1Secret.hashCode()));
        String secretName = v1Secret.getMetadata().getName();
        DevopsSecretDTO devopsSecretDTO = devopsSecretService.baseQueryByEnvIdAndName(envId, secretName);
        if (devopsSecretDTO != null) {
            Long secretId = devopsSecretDTO.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType()
                            .equals(v1Secret.getKind()))
                    .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(secretId))) {
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, secretId, v1Secret.getKind());
                if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath()
                        .equals(objectPath.get(TypeUtil.objToString(v1Secret.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, secretName);
                }
            }
        }
        if (c7nSecrets.stream().anyMatch(e -> e.getMetadata().getName().equals(secretName))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, secretName);
        } else {
            c7nSecrets.add(v1Secret);
        }
    }

    @Override
    public ResourceType getType() {
        return ResourceType.SECRET;
    }
}
