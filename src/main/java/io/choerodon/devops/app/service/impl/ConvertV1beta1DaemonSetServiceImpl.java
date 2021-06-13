package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.devops.app.service.DevopsDaemonSetService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.infra.constant.KubernetesConstants;
import io.choerodon.devops.infra.dto.DevopsDaemonSetDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.TypeUtil;

public class ConvertV1beta1DaemonSetServiceImpl extends ConvertK8sObjectService<DevopsDaemonSetDTO> {

    public ConvertV1beta1DaemonSetServiceImpl() {
        super(DevopsDaemonSetDTO.class);
    }

    @Autowired
    private DevopsDaemonSetService daemonSetService;

    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Override
    public ResourceType getType() {
        return ResourceType.DAEMONSET;
    }

    @Override
    public void checkIfExist(List<DevopsDaemonSetDTO> devopsDaemonSetDTOS, Long
            envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, DevopsDaemonSetDTO devopsDaemonSetDTO) {
        String filePath = objectPath.get(TypeUtil.objToString(devopsDaemonSetDTO.hashCode()));
        String daemonSetDTOName = devopsDaemonSetDTO.getName();
        DevopsDaemonSetDTO oldDevopsDaemonSetDTO = daemonSetService.baseQueryByEnvIdAndName(envId, daemonSetDTOName);
        if (oldDevopsDaemonSetDTO != null) {
            Long devopsDaemonSetDTOId = devopsDaemonSetDTO.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType()
                            .equals(this.getType().getType()))
                    .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(devopsDaemonSetDTOId))) {
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, devopsDaemonSetDTOId, this.getType().getType());
                if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath()
                        .equals(objectPath.get(TypeUtil.objToString(devopsDaemonSetDTO.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, devopsDaemonSetDTO);
                }
            }
        }

        if (devopsDaemonSetDTOS.stream().anyMatch(e -> e.getName().equals(daemonSetDTOName))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, daemonSetDTOName);
        } else {
            devopsDaemonSetDTOS.add(devopsDaemonSetDTO);
        }
    }

    @Override
    public DevopsDaemonSetDTO serializableObject(String jsonString, String filePath, Map<String, String> objectPath, Long envId) {
        DevopsDaemonSetDTO result = getDevopsDaemonSetDTO(envId, filePath, JSONObject.parseObject(jsonString));
        objectPath.put(TypeUtil.objToString(result.hashCode()), filePath);
        return result;
    }

    private DevopsDaemonSetDTO getDevopsDaemonSetDTO(Long envId, String
            filePath, Map<String, Object> data) {
        DevopsDaemonSetDTO devopsDaemonSetDTO = new DevopsDaemonSetDTO();

        devopsDaemonSetDTO.setEnvId(envId);
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
        devopsDaemonSetDTO.setName(metadata.get(KubernetesConstants.NAME).toString());

        devopsDaemonSetDTO.setContent(FileUtil.getYaml().dump(data));
        return devopsDaemonSetDTO;
    }
}
