package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsJobService;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsJobDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.TypeUtil;

@Component
public class ConvertV1JobServiceImpl extends ConvertK8sObjectService<DevopsJobDTO> {

    public ConvertV1JobServiceImpl() {
        super(DevopsJobDTO.class);
    }

    @Autowired
    private DevopsJobService devopsJobService;

    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Override
    public ResourceType getType() {
        return ResourceType.JOB;
    }

    @Override
    public void checkIfExist(List<DevopsJobDTO> devopsJobDTOS, Long
            envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, DevopsJobDTO devopsJobDTO) {
        String filePath = objectPath.get(TypeUtil.objToString(devopsJobDTO.hashCode()));
        String jobDTOName = devopsJobDTO.getName();
        DevopsJobDTO oldDevopsJobDTO = devopsJobService.baseQueryByEnvIdAndName(envId, jobDTOName);
        if (oldDevopsJobDTO != null) {
            Long devopsJobDTOId = oldDevopsJobDTO.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType()
                            .equals(this.getType().getType()))
                    .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(devopsJobDTOId))) {
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, devopsJobDTOId, this.getType().getType());
                if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath()
                        .equals(objectPath.get(TypeUtil.objToString(devopsJobDTO.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, devopsJobDTO);
                }
            }
        }

        if (devopsJobDTOS.stream().anyMatch(e -> e.getName().equals(jobDTOName))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, jobDTOName);
        } else {
            devopsJobDTOS.add(devopsJobDTO);
        }
    }

    @Override
    public DevopsJobDTO serializableObject(String jsonString, String filePath, Map<String, String> objectPath, Long envId) {
        DevopsJobDTO result = getDevopsJobDTO(envId, filePath, JSONObject.parseObject(jsonString));
        objectPath.put(TypeUtil.objToString(result.hashCode()), filePath);
        return result;
    }

    private DevopsJobDTO getDevopsJobDTO(Long envId, String
            filePath, Map<String, Object> data) {
        DevopsJobDTO devopsJobDTO = new DevopsJobDTO();

        devopsJobDTO.setEnvId(envId);
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
        devopsJobDTO.setName(metadata.get(KubernetesConstants.NAME).toString());

        devopsJobDTO.setContent(FileUtil.getYaml().dump(data));
        return devopsJobDTO;
    }
}
