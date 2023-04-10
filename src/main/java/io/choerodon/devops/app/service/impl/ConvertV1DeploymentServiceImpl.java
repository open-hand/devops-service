package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsDeploymentService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsDeploymentDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Component
public class ConvertV1DeploymentServiceImpl extends ConvertK8sObjectService<DevopsDeploymentDTO> {

    public ConvertV1DeploymentServiceImpl() {
        super(DevopsDeploymentDTO.class);
    }

    @Autowired
    private DevopsDeploymentService devopsDeploymentService;

    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Override
    public ResourceType getType() {
        return ResourceType.DEPLOYMENT;
    }

    @Override
    public void checkIfExist(List<DevopsDeploymentDTO> devopsDeploymentDTOS, Long
            envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, DevopsDeploymentDTO devopsDeploymentDTO) {
        String filePath = objectPath.get(TypeUtil.objToString(devopsDeploymentDTO.hashCode()));
        String deployName = devopsDeploymentDTO.getName();
        DevopsDeploymentDTO oldDevopsDeploymentDTO = devopsDeploymentService.baseQueryByEnvIdAndName(envId, deployName);
        if (oldDevopsDeploymentDTO != null) {
            Long deploymentDTOId = oldDevopsDeploymentDTO.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType()
                            .equals(this.getType().getType()))
                    .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(deploymentDTOId))) {
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, deploymentDTOId, this.getType().getType());
                if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath()
                        .equals(objectPath.get(TypeUtil.objToString(devopsDeploymentDTO.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, devopsDeploymentDTO);
                }
            }
        }

        if (devopsDeploymentDTOS.stream().anyMatch(e -> e.getName().equals(deployName))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, deployName);
        } else {
            devopsDeploymentDTOS.add(devopsDeploymentDTO);
        }
    }

    @Override
    public DevopsDeploymentDTO serializableObject(String jsonString, String filePath, Map<String, String> objectPath, Long envId) {
        DevopsDeploymentDTO result = getDevopsDeploymentDTO(envId, filePath, JSONObject.parseObject(jsonString));
        objectPath.put(TypeUtil.objToString(result.hashCode()), filePath);
        return result;
    }

    private DevopsDeploymentDTO getDevopsDeploymentDTO(Long envId, String
            filePath, Map<String, Object> data) {
        DevopsDeploymentDTO devopsDeploymentDTO = new DevopsDeploymentDTO();

        devopsDeploymentDTO.setEnvId(envId);
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
        devopsDeploymentDTO.setName(metadata.get(KubernetesConstants.NAME).toString());

        devopsDeploymentDTO.setContent(FileUtil.getYaml().dump(data));
        return devopsDeploymentDTO;
    }
}
