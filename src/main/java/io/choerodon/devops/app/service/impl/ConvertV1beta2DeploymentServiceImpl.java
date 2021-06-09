package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1beta2Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.DevopsDeploymentService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.infra.dto.DevopsDeploymentDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;

@Component
public class ConvertV1beta2DeploymentServiceImpl extends ConvertK8sObjectService<V1beta2Deployment> {

    public ConvertV1beta2DeploymentServiceImpl() {
        super(V1beta2Deployment.class);
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
    public void checkParameters(V1beta2Deployment v1beta2Deployment, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(v1beta2Deployment.hashCode()));
        if (v1beta2Deployment.getMetadata() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.RELEASE_META_DATA_NOT_FOUND.getError(), filePath);
        } else {
            if (v1beta2Deployment.getMetadata().getName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.RELEASE_NAME_NOT_FOUND.getError(), filePath);
            }
        }
        if (v1beta2Deployment.getSpec() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.RELEASE_SPEC_NOT_FOUND.getError(), filePath);
        }
        if (v1beta2Deployment.getApiVersion() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.RELEASE_API_VERSION_NOT_FOUND.getError(), filePath);
        }
    }

    @Override
    public void checkIfExist(List<V1beta2Deployment> v1beta2Deployments, Long
            envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, V1beta2Deployment v1beta2Deployment) {
        String filePath = objectPath.get(TypeUtil.objToString(v1beta2Deployment.hashCode()));
        String deployName = v1beta2Deployment.getMetadata().getName();
        DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.baseQueryByEnvIdAndName(envId, deployName);
        if (devopsDeploymentDTO != null) {
            Long deploymentDTOId = devopsDeploymentDTO.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType()
                            .equals(v1beta2Deployment.getKind()))
                    .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(deploymentDTOId))) {
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, deploymentDTOId, v1beta2Deployment.getKind());
                if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath()
                        .equals(objectPath.get(TypeUtil.objToString(v1beta2Deployment.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, v1beta2Deployment);
                }
            }
        }

        if (v1beta2Deployments.stream().anyMatch(e -> e.getMetadata().getName().equals(deployName))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, deployName);
        } else {
            v1beta2Deployments.add(v1beta2Deployment);
        }
    }
}
