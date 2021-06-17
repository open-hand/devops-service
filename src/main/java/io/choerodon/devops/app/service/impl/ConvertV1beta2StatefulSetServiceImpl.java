package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsStatefulSetService;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsStatefulSetDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Component
public class ConvertV1beta2StatefulSetServiceImpl extends ConvertK8sObjectService<DevopsStatefulSetDTO> {

    public ConvertV1beta2StatefulSetServiceImpl() {
        super(DevopsStatefulSetDTO.class);
    }

    @Autowired
    private DevopsStatefulSetService devopsStatefulSetService;

    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Override
    public ResourceType getType() {
        return ResourceType.STATEFULSET;
    }

    @Override
    public void checkIfExist(List<DevopsStatefulSetDTO> devopsStatefulSetDTOS, Long
            envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, DevopsStatefulSetDTO devopsStatefulSetDTO) {
        String filePath = objectPath.get(TypeUtil.objToString(devopsStatefulSetDTO.hashCode()));
        String statefulSetName = devopsStatefulSetDTO.getName();
        DevopsStatefulSetDTO oldDevopsStatefulSetDTO = devopsStatefulSetService.baseQueryByEnvIdAndName(envId, statefulSetName);
        if (oldDevopsStatefulSetDTO != null) {
            Long statefulSetDTOId = oldDevopsStatefulSetDTO.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType()
                            .equals(this.getType().getType()))
                    .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(statefulSetDTOId))) {
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, statefulSetDTOId, this.getType().getType());
                if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath()
                        .equals(objectPath.get(TypeUtil.objToString(devopsStatefulSetDTO.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, devopsStatefulSetDTO);
                }
            }
        }

        if (devopsStatefulSetDTOS.stream().anyMatch(e -> e.getName().equals(statefulSetName))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, statefulSetName);
        } else {
            devopsStatefulSetDTOS.add(devopsStatefulSetDTO);
        }
    }

    @Override
    public DevopsStatefulSetDTO serializableObject(String jsonString, String filePath, Map<String, String> objectPath, Long envId) {
        DevopsStatefulSetDTO result = getDevopsStatefulSetDTO(envId, filePath, JSONObject.parseObject(jsonString));
        objectPath.put(TypeUtil.objToString(result.hashCode()), filePath);
        return result;
    }

    private DevopsStatefulSetDTO getDevopsStatefulSetDTO(Long envId, String
            filePath, Map<String, Object> data) {
        DevopsStatefulSetDTO devopsStatefulSetDTO = new DevopsStatefulSetDTO();

        devopsStatefulSetDTO.setEnvId(envId);
        if (data.get(KubernetesConstants.KIND) == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_KIND_NOT_FOUND.getError(), filePath);
        }
        JSONObject metadata = (JSONObject) data.get(KubernetesConstants.METADATA);

        if (metadata == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_METADATA_NOT_FOUND.getError(), filePath);
        }
        if (metadata.get(KubernetesConstants.NAME) == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_NAME_NOT_FOUND.getError(), filePath);
        }
        devopsStatefulSetDTO.setName(metadata.get(KubernetesConstants.NAME).toString());

        devopsStatefulSetDTO.setContent(FileUtil.getYaml().dump(data));
        return devopsStatefulSetDTO;
    }
}
