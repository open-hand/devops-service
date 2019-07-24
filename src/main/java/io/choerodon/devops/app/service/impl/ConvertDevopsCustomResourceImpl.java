package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.app.service.DevopsCustomizeResourceService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by Sheep on 2019/7/1.
 */
public class ConvertDevopsCustomResourceImpl extends ConvertK8sObjectService<DevopsCustomizeResourceDTO> {

    private DevopsCustomizeResourceService devopsCustomizeResourceService;
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    public ConvertDevopsCustomResourceImpl() {
        this.devopsCustomizeResourceService = ApplicationContextHelper.getSpringFactory().getBean(DevopsCustomizeResourceService.class);
        this.devopsEnvFileResourceService = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceService.class);
    }


    @Override
    public void checkIfExist(List<DevopsCustomizeResourceDTO> devopsCustomizeResourceDTOS, Long
            envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, DevopsCustomizeResourceDTO
                                     devopsCustomizeResourceDTO) {
        String filePath = objectPath.get(TypeUtil.objToString(devopsCustomizeResourceDTO.hashCode()));
        DevopsCustomizeResourceDTO oldDevopsCustomizeResourceDTO = devopsCustomizeResourceService.queryByEnvIdAndKindAndName(envId, devopsCustomizeResourceDTO.getK8sKind(), devopsCustomizeResourceDTO.getName());
        if (oldDevopsCustomizeResourceDTO != null
                && beforeSyncDelete.stream()
                .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType().equals(ResourceType.CUSTOM.getType()))
                .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(oldDevopsCustomizeResourceDTO.getId()))) {
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService.baseQueryByEnvIdAndResourceId(envId, oldDevopsCustomizeResourceDTO.getId(), ResourceType.CUSTOM.getType());
            if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath().equals(objectPath.get(TypeUtil.objToString(devopsCustomizeResourceDTO.hashCode())))) {
                throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, devopsCustomizeResourceDTO.getName());
            }
        }
        if (devopsCustomizeResourceDTOS.stream().anyMatch(resourceDTO -> resourceDTO.getName().equals(devopsCustomizeResourceDTO.getName()) && resourceDTO.getK8sKind().equals(devopsCustomizeResourceDTO.getK8sKind()))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, devopsCustomizeResourceDTO.getName());
        } else {
            devopsCustomizeResourceDTOS.add(devopsCustomizeResourceDTO);
        }
    }

}
