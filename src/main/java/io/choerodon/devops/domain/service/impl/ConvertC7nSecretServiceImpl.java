package io.choerodon.devops.domain.service.impl;

import java.util.List;
import java.util.Map;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.entity.DevopsSecretE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsSecretRepository;
import io.choerodon.devops.domain.application.valueobject.C7nSecret;
import io.choerodon.devops.domain.service.ConvertK8sObjectService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 下午7:31
 * Description:
 */
public class ConvertC7nSecretServiceImpl extends ConvertK8sObjectService<C7nSecret> {

    private DevopsSecretRepository devopsSecretRepository;
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    public ConvertC7nSecretServiceImpl() {
        this.devopsSecretRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsSecretRepository.class);
        this.devopsEnvFileResourceRepository = ApplicationContextHelper.getSpringFactory()
                .getBean(DevopsEnvFileResourceRepository.class);
    }

    @Override
    public void checkParameters(C7nSecret c7nSecret, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(c7nSecret.hashCode()));
        if (c7nSecret.getApiVersion() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.SECRET_API_VERSION_NOT_FOUND.getError(), filePath);
        } else {
            if (c7nSecret.getMetadata().getName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.SECRET_NAME_NOT_FOUND.getError(), filePath);
            }
        }
        if (c7nSecret.getStringData() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.SECRET_DATA_NOT_FOUND.getError(), filePath);
        }
    }

    @Override
    public void checkIfExist(List<C7nSecret> c7nSecrets, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete,
                             Map<String, String> objectPath, C7nSecret c7nSecret) {
        String filePath = objectPath.get(TypeUtil.objToString(c7nSecret.hashCode()));
        String secretName = c7nSecret.getMetadata().getName();
        DevopsSecretE devopsSecretE = devopsSecretRepository.selectByEnvIdAndName(envId, secretName);
        if (devopsSecretE != null) {
            Long secretId = devopsSecretE.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType()
                            .equals(c7nSecret.getKind()))
                    .noneMatch(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceId().equals(secretId))) {
                DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                        .queryByEnvIdAndResource(envId, secretId, c7nSecret.getKind());
                if (devopsEnvFileResourceE != null && !devopsEnvFileResourceE.getFilePath()
                        .equals(objectPath.get(TypeUtil.objToString(c7nSecret.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, secretName);
                }
            }
        }
        if (c7nSecrets.stream().anyMatch(e -> e.getMetadata().getName().equals(secretName))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, secretName);
        } else {
            c7nSecrets.add(c7nSecret);
        }
    }
}
