package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.app.service.DevopsCronJobService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsCronJobDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.TypeUtil;

public class ConvertV1Alpha1CronJobServiceImpl extends ConvertK8sObjectService<DevopsCronJobDTO> {

    public ConvertV1Alpha1CronJobServiceImpl() {
        super(DevopsCronJobDTO.class);
    }

    @Autowired
    private DevopsCronJobService devopsCronJobService;

    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Override
    public ResourceType getType() {
        return ResourceType.CRON_JOB;
    }

    @Override
    public void checkIfExist(List<DevopsCronJobDTO> devopsCronJobDTOS, Long
            envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, DevopsCronJobDTO devopsCronJobDTO) {
        String filePath = objectPath.get(TypeUtil.objToString(devopsCronJobDTO.hashCode()));
        String cronJobDTOName = devopsCronJobDTO.getName();
        DevopsCronJobDTO oldDevopsCronJobDTO = devopsCronJobService.baseQueryByEnvIdAndName(envId, cronJobDTOName);
        if (oldDevopsCronJobDTO != null) {
            Long devopsCronJobDTOId = devopsCronJobDTO.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType()
                            .equals(this.getType().getType()))
                    .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(devopsCronJobDTOId))) {
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, devopsCronJobDTOId, this.getType().getType());
                if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath()
                        .equals(objectPath.get(TypeUtil.objToString(devopsCronJobDTO.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, devopsCronJobDTO);
                }
            }
        }

        if (devopsCronJobDTOS.stream().anyMatch(e -> e.getName().equals(cronJobDTOName))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, cronJobDTOName);
        } else {
            devopsCronJobDTOS.add(devopsCronJobDTO);
        }
    }

    @Override
    public DevopsCronJobDTO serializableObject(String jsonString, String filePath, Map<String, String> objectPath, Long envId) {
        DevopsCronJobDTO result = getDevopsCronJobDTO(envId, filePath, JSONObject.parseObject(jsonString));
        objectPath.put(TypeUtil.objToString(result.hashCode()), filePath);
        return result;
    }

    private DevopsCronJobDTO getDevopsCronJobDTO(Long envId, String
            filePath, Map<String, Object> data) {
        DevopsCronJobDTO devopsCronJobDTO = new DevopsCronJobDTO();

        devopsCronJobDTO.setEnvId(envId);
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
        devopsCronJobDTO.setName(metadata.get(KubernetesConstants.NAME).toString());

        devopsCronJobDTO.setContent(FileUtil.getYaml().dump(data));
        return devopsCronJobDTO;
    }
}
