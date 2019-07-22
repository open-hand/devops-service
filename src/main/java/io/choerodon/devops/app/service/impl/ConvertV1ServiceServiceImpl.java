package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsServiceService;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsServiceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServicePort;


public class ConvertV1ServiceServiceImpl extends ConvertK8sObjectService<V1Service> {

    private DevopsServiceService devopsServiceService;
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    public ConvertV1ServiceServiceImpl() {
        this.devopsServiceService = ApplicationContextHelper.getSpringFactory().getBean(DevopsServiceService.class);
        this.devopsEnvFileResourceService = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceService.class);
    }

    @Override
    public void checkIfExist(List<V1Service> v1Services, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, V1Service v1Service) {
        String filePath = objectPath.get(TypeUtil.objToString(v1Service.hashCode()));
        DevopsServiceDTO devopsServiceDTO = devopsServiceService.baseQueryByNameAndEnvId(v1Service.getMetadata().getName(), envId);
        if (devopsServiceDTO != null &&
                beforeSyncDelete.stream()
                        .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType().equals(v1Service.getKind()))
                        .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(devopsServiceDTO.getId()))) {
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService.baseQueryByEnvIdAndResourceId(envId, devopsServiceDTO.getId(), v1Service.getKind());
            if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath().equals(objectPath.get(TypeUtil.objToString(v1Service.hashCode())))) {
                throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, v1Service.getMetadata().getName());
            }
        }
        if (v1Services.stream().anyMatch(v1Service1 -> v1Service1.getMetadata().getName().equals(v1Service.getMetadata().getName()))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, v1Service.getMetadata().getName());
        } else {
            v1Services.add(v1Service);
        }
    }

    @Override
    public void checkParameters(V1Service v1Service, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(v1Service.hashCode()));
        if (v1Service.getMetadata() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.SERVICE_METADATA_NOT_FOUND.getError(), filePath);
        } else {
            if (v1Service.getMetadata().getName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.SERVICE_NAME_NOT_FOUND.getError(), filePath);
            }
        }
        if (v1Service.getSpec() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.SERVICE_SPEC_NOT_FOUND.getError(), filePath);
        } else {
            checkV1ServicePorts(v1Service, filePath);
        }
        if (v1Service.getApiVersion() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.SERVICE_API_VERSION_NOT_FOUND.getError(), filePath);
        }
    }

    private void checkV1ServicePorts(V1Service v1Service, String filePath) {
        List<V1ServicePort> v1ServicePorts = v1Service.getSpec().getPorts();
        if (v1ServicePorts.isEmpty()) {
            throw new GitOpsExplainException(GitOpsObjectError.SERVICE_PORTS_NOT_FOUND.getError(), filePath);
        } else {
            for (V1ServicePort v1ServicePort : v1ServicePorts) {
                if (v1ServicePort.getName() == null) {
                    throw new GitOpsExplainException(GitOpsObjectError.SERVICE_PORTS_NAME_NOT_FOUND.getError(), filePath);
                }
                if (v1ServicePort.getPort() == null) {
                    throw new GitOpsExplainException(GitOpsObjectError.SERVICE_PORTS_PORT_NOT_FOUND.getError(), filePath);
                }
                if (v1ServicePort.getTargetPort() == null) {
                    throw new GitOpsExplainException(GitOpsObjectError.SERVICE_PORTS_TARGET_PORT.getError(), filePath);
                }
            }
        }
        if (v1Service.getSpec().getType() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.SERVICE_TYPE_NOT_FOUND.getError(), filePath);
        }
    }
}