package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceVO;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsSecretRepository;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.kubernetes.client.models.V1Secret;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 下午7:31
 * Description:
 */
public class ConvertC7nSecretServiceImpl extends ConvertK8sObjectService<V1Secret> {

    private DevopsSecretRepository devopsSecretRepository;
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    public ConvertC7nSecretServiceImpl() {
        this.devopsSecretRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsSecretRepository.class);
        this.devopsEnvFileResourceRepository = ApplicationContextHelper.getSpringFactory()
                .getBean(DevopsEnvFileResourceRepository.class);
    }

    @Override
    public void checkParameters(V1Secret v1Secret, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(v1Secret.hashCode()));
        if (v1Secret.getApiVersion() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.SECRET_API_VERSION_NOT_FOUND.getError(), filePath);
        } else {
            if (v1Secret.getMetadata().getName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.SECRET_NAME_NOT_FOUND.getError(), filePath);
            }
            if (v1Secret.getType().equals("Opaque")) {
                if (v1Secret.getStringData() == null || v1Secret.getStringData().size() == 0) {
                    throw new GitOpsExplainException(GitOpsObjectError.SECRET_STRING_DATA_NOT_FOUND.getError(), filePath);
                }
            } else {
                if (v1Secret.getData() == null || v1Secret.getData().size() == 0) {
                    throw new GitOpsExplainException(GitOpsObjectError.SECRET_DATA_NOT_FOUND.getError(), filePath);
                }
            }
        }
    }

    @Override
    public void checkIfExist(List<V1Secret> c7nSecrets, Long envId, List<DevopsEnvFileResourceVO> beforeSyncDelete,
                             Map<String, String> objectPath, V1Secret v1Secret) {
        String filePath = objectPath.get(TypeUtil.objToString(v1Secret.hashCode()));
        String secretName = v1Secret.getMetadata().getName();
        DevopsSecretE devopsSecretE = devopsSecretRepository.baseQueryByEnvIdAndName(envId, secretName);
        if (devopsSecretE != null) {
            Long secretId = devopsSecretE.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType()
                            .equals(v1Secret.getKind()))
                    .noneMatch(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceId().equals(secretId))) {
                DevopsEnvFileResourceVO devopsEnvFileResourceE = devopsEnvFileResourceRepository
                        .baseQueryByEnvIdAndResourceId(envId, secretId, v1Secret.getKind());
                if (devopsEnvFileResourceE != null && !devopsEnvFileResourceE.getFilePath()
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
}
