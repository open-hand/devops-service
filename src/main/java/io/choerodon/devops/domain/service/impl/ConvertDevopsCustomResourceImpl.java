package io.choerodon.devops.domain.service.impl;

import java.util.List;
import java.util.Map;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsCustomizeResourceE;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsCustomizeResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.service.ConvertK8sObjectService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;
import io.choerodon.devops.infra.common.util.enums.ResourceType;

/**
 * Created by Sheep on 2019/7/1.
 */
public class ConvertDevopsCustomResourceImpl extends ConvertK8sObjectService<DevopsCustomizeResourceE> {

    private DevopsCustomizeResourceRepository devopsCustomizeResourceRepository;
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    public ConvertDevopsCustomResourceImpl() {
        this.devopsCustomizeResourceRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsCustomizeResourceRepository.class);
        this.devopsEnvFileResourceRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceRepository.class);
    }


    @Override
    public void checkIfExist(List<DevopsCustomizeResourceE> devopsCustomizeResourceES, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, DevopsCustomizeResourceE devopsCustomizeResourceE) {
        String filePath = objectPath.get(TypeUtil.objToString(devopsCustomizeResourceE.hashCode()));
        DevopsCustomizeResourceE oldDevopsCustomizeResourceE = devopsCustomizeResourceRepository.queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceE.getK8sKind(), devopsCustomizeResourceE.getName());
        if (oldDevopsCustomizeResourceE != null
                && beforeSyncDelete.stream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(ResourceType.CUSTOM.getType()))
                .noneMatch(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceId().equals(oldDevopsCustomizeResourceE.getId()))) {
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, oldDevopsCustomizeResourceE.getId(), ResourceType.CUSTOM.getType());
            if (devopsEnvFileResourceE != null && !devopsEnvFileResourceE.getFilePath().equals(objectPath.get(TypeUtil.objToString(devopsCustomizeResourceE.hashCode())))) {
                throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, devopsCustomizeResourceE.getName());
            }
        }
        if (devopsCustomizeResourceES.stream().anyMatch(resourceE -> resourceE.getName().equals(devopsCustomizeResourceE.getName()) && resourceE.getK8sKind().equals(devopsCustomizeResourceE.getK8sKind()))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, devopsCustomizeResourceE.getName());
        } else {
            devopsCustomizeResourceES.add(devopsCustomizeResourceE);
        }
    }

    @Override
    public void checkParameters(DevopsCustomizeResourceE devopsCustomizeResourceE, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(devopsCustomizeResourceE.hashCode()));
        if (devopsCustomizeResourceE.getK8sKind() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_KIND_NOT_FOUND.getError(), filePath);
        } else if (devopsCustomizeResourceE.getName() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_NAME_NOT_FOUND.getError(), filePath);
        }

    }
}
