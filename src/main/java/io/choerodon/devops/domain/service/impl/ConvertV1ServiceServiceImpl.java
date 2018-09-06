package io.choerodon.devops.domain.service.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServicePort;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.entity.DevopsServiceE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsServiceRepository;
import io.choerodon.devops.domain.service.ConvertK8sObjectService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;

public class ConvertV1ServiceServiceImpl extends ConvertK8sObjectService<V1Service> {

    private DevopsServiceRepository devopsServiceRepository;
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    public ConvertV1ServiceServiceImpl() {
        this.devopsServiceRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsServiceRepository.class);
        this.devopsEnvFileResourceRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceRepository.class);
    }


    public void checkIfexist(List<V1Service> v1Services, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, V1Service v1Service) {
        String filePath = objectPath.get(TypeUtil.objToString(v1Service.hashCode()));
        DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndEnvId(v1Service.getMetadata().getName(), envId);
        if (devopsServiceE != null &&
                beforeSyncDelete.parallelStream()
                        .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(v1Service.getKind()))
                        .noneMatch(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceId().equals(devopsServiceE.getId()))) {
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, devopsServiceE.getId(), v1Service.getKind());
            if (devopsEnvFileResourceE != null && !devopsEnvFileResourceE.getFilePath().equals(objectPath.get(TypeUtil.objToString(v1Service.hashCode())))) {
                throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError() + v1Service.getMetadata().getName(), filePath);
            }
        }
        if (v1Services.parallelStream().anyMatch(v1Service1 -> v1Service1.getMetadata().getName().equals(v1Service.getMetadata().getName()))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError() + v1Service.getMetadata().getName(), filePath);
        } else {
            v1Services.add(v1Service);
        }
    }


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
            throw new GitOpsExplainException(GitOpsObjectError.SERVICE_APIVERSION_NOT_FOUND.getError(), filePath);

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