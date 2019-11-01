package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsCustomizeResourceService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by Sheep on 2019/7/1.
 */
@Component
public class ConvertDevopsCustomResourceImpl extends ConvertK8sObjectService<DevopsCustomizeResourceDTO> {
    @Autowired
    private DevopsCustomizeResourceService devopsCustomizeResourceService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    public ConvertDevopsCustomResourceImpl() {
        super(DevopsCustomizeResourceDTO.class);
    }


    @Override
    public DevopsCustomizeResourceDTO serializableObject(String jsonString, String filePath, Map<String, String> objectPath, Long envId) {
        DevopsCustomizeResourceDTO result = getDevopsCustomizeResourceDTO(envId, filePath, JSONObject.parseObject(jsonString));
        objectPath.put(TypeUtil.objToString(result.hashCode()), filePath);
        return result;
    }

    private DevopsCustomizeResourceDTO getDevopsCustomizeResourceDTO(Long envId, String
            filePath, Map<String, Object> data) {
        DevopsCustomizeResourceDTO devopsCustomizeResourceDTO = new DevopsCustomizeResourceDTO();

        devopsCustomizeResourceDTO.setEnvId(envId);
        devopsCustomizeResourceDTO.setFilePath(filePath);
        if (data.get(KubernetesConstants.KIND) == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_KIND_NOT_FOUND.getError(), filePath);
        }
        devopsCustomizeResourceDTO.setK8sKind(data.get(KubernetesConstants.KIND).toString());
        JSONObject metadata = (JSONObject) data.get(KubernetesConstants.METADATA);

        if (metadata == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_METADATA_NOT_FOUND.getError(), filePath);
        }
        if (metadata.get(KubernetesConstants.NAME) == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CUSTOM_RESOURCE_NAME_NOT_FOUND.getError(), filePath);
        }
        devopsCustomizeResourceDTO.setName(metadata.get(KubernetesConstants.NAME).toString());

        //添加自定义资源标签
        JSONObject labels = (JSONObject) metadata.get(KubernetesConstants.LABELS);

        if (labels == null) {
            labels = new JSONObject();
        }
        labels.put(KubernetesConstants.CHOERODON_IO_RESOURCE, KubernetesConstants.CUSTOM);
        metadata.put(KubernetesConstants.LABELS, labels);
        data.put(KubernetesConstants.METADATA, metadata);

        devopsCustomizeResourceDTO.setResourceContent(FileUtil.getYaml().dump(data));
        return devopsCustomizeResourceDTO;
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

    @Override
    public ResourceType getType() {
        return ResourceType.CUSTOM;
    }

}
