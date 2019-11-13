package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1EndpointAddress;
import io.kubernetes.client.models.V1EndpointPort;
import io.kubernetes.client.models.V1Endpoints;
import org.springframework.stereotype.Component;

import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.enums.GitOpsObjectError;

@Component
public class ConvertV1EndPointsServiceImpl extends ConvertK8sObjectService<V1Endpoints> {
    public ConvertV1EndPointsServiceImpl() {
        super(V1Endpoints.class);
    }

    @Override
    public void checkParameters(V1Endpoints v1Endpoints, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(v1Endpoints.hashCode()));
        if (v1Endpoints.getMetadata() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.END_POINT_ADDRESS_IP_NOT_FOUND.getError(), filePath);
        } else {
            if (v1Endpoints.getMetadata().getName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.END_POINT_METADATA_NOT_FOUND.getError(), filePath);
            }
        }
        if (v1Endpoints.getSubsets().isEmpty()) {
            throw new GitOpsExplainException(GitOpsObjectError.END_POINT_SUBSETS_NOT_FOUND.getError(), filePath);
        } else {
            if (v1Endpoints.getSubsets().get(0).getAddresses().isEmpty()) {
                throw new GitOpsExplainException(GitOpsObjectError.END_POINT_ADDRESS_NOT_FOUND.getError());
            } else {
                for (V1EndpointAddress v1EndpointAddress : v1Endpoints.getSubsets().get(0).getAddresses()) {
                    if (v1EndpointAddress.getIp() == null) {
                        throw new GitOpsExplainException(GitOpsObjectError.END_POINT_ADDRESS_IP_NOT_FOUND.getError());
                    }
                }
            }
            if (v1Endpoints.getSubsets().get(0).getPorts().isEmpty()) {
                throw new GitOpsExplainException(GitOpsObjectError.END_POINT_PORTS_NOT_FOUND.getError());
            } else {
                for (V1EndpointPort v1EndpointPort : v1Endpoints.getSubsets().get(0).getPorts()) {
                    if (v1EndpointPort.getPort() == null) {
                        throw new GitOpsExplainException(GitOpsObjectError.END_POINT_ADDRESS_IP_NOT_FOUND.getError());
                    }
                }
            }
        }
    }

    @Override
    public void checkIfExist(List<V1Endpoints> v1EndpointsList, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, V1Endpoints v1Endpoints) {
        // 暂不做校验
        v1EndpointsList.add(v1Endpoints);
    }

    @Override
    public ResourceType getType() {
        return ResourceType.ENDPOINTS;
    }
}
