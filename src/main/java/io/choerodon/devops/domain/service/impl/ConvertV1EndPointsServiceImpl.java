package io.choerodon.devops.domain.service.impl;

import java.util.Map;

import io.kubernetes.client.models.V1EndpointAddress;
import io.kubernetes.client.models.V1EndpointPort;
import io.kubernetes.client.models.V1Endpoints;

import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.service.ConvertK8sObjectService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;

public class ConvertV1EndPointsServiceImpl extends ConvertK8sObjectService<V1Endpoints> {

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
}
