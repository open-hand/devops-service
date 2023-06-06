package io.choerodon.devops.app.service.impl;

import io.kubernetes.client.openapi.models.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsIngressValidator;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.app.service.DevopsIngressService;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.dto.DevopsIngressDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;

@Component
public class ConvertV1IngressServiceImpl extends ConvertK8sObjectService<V1Ingress> {
    @Autowired
    private DevopsIngressService devopsIngressService;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    public ConvertV1IngressServiceImpl() {
        super(V1Ingress.class);
    }

    @Override
    public void checkIfExist(List<V1Ingress> v1beta1Ingresses, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, V1Ingress v1beta1Ingress) {
        String filePath = objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode()));
        DevopsIngressDTO devopsIngressDTO = devopsIngressService.baseCheckByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
        if (devopsIngressDTO != null
                && beforeSyncDelete.stream()
                .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType().equals(v1beta1Ingress.getKind()))
                .noneMatch(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceId().equals(devopsIngressDTO.getId()))) {
            DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService.baseQueryByEnvIdAndResourceId(envId, devopsIngressDTO.getId(), v1beta1Ingress.getKind());
            if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath().equals(objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())))) {
                throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, v1beta1Ingress.getMetadata().getName());
            }
        }
        if (v1beta1Ingresses.stream().anyMatch(v1beta1Ingress1 -> v1beta1Ingress1.getMetadata().getName().equals(v1beta1Ingress.getMetadata().getName()))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError(), filePath, v1beta1Ingress.getMetadata().getName());
        } else {
            v1beta1Ingresses.add(v1beta1Ingress);
        }
    }

    @Override
    public ResourceType getType() {
        return ResourceType.V1_INGRESS;
    }

    @Override
    public void checkParameters(V1Ingress v1beta1Ingress, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode()));
        if (v1beta1Ingress.getMetadata() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_META_DATA_NOT_FOUND.getError(), filePath);
        } else {
            if (v1beta1Ingress.getMetadata().getName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.INGRESS_NAME_NOT_FOUND.getError(), filePath);
            }
        }
        if (v1beta1Ingress.getSpec() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_SPEC_NOT_FOUND.getError(), filePath);
        } else {
            checkV1IngressRules(v1beta1Ingress, filePath);
        }
        if (v1beta1Ingress.getApiVersion() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_API_VERSION_NOT_FOUND.getError(), filePath);
        }
        if (v1beta1Ingress.getMetadata().getAnnotations() != null) {
            DevopsIngressValidator.checkAnnotationsForGitOps(v1beta1Ingress.getMetadata().getAnnotations(), filePath);
        }
    }


    private void checkV1IngressRules(V1Ingress v1beta1Ingress, String filePath) {
        List<V1IngressRule> v1beta1IngressRules = v1beta1Ingress.getSpec().getRules();
        if (v1beta1IngressRules.isEmpty()) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_RULES_NOT_FOUND.getError(), filePath);
        } else {
            for (V1IngressRule v1beta1IngressRule : v1beta1IngressRules) {
                if (v1beta1IngressRule.getHost() == null) {
                    throw new GitOpsExplainException(GitOpsObjectError.INGRESS_RULE_HOST_NOT_FOUND.getError(), filePath);
                }
                if (v1beta1IngressRule.getHttp() == null) {
                    throw new GitOpsExplainException(GitOpsObjectError.INGRESS_RULE_HTTP_NOT_FOUND.getError(), filePath);
                } else {
                    checkV1HTTPIngressPaths(v1beta1IngressRule, filePath);
                }
            }
        }
    }

    private void checkV1HTTPIngressPaths(V1IngressRule v1beta1IngressRule, String filePath) {
        V1HTTPIngressRuleValue http = v1beta1IngressRule.getHttp();
        if (http == null) {
            throw new CommonException(GitOpsObjectError.INGRESS_PATHS_NOT_FOUND.getError(), filePath);
        }
        List<V1HTTPIngressPath> v1HTTPIngressPaths = http.getPaths();
        if (v1HTTPIngressPaths.isEmpty()) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_PATHS_NOT_FOUND.getError(), filePath);
        } else {
            for (V1HTTPIngressPath v1beta1HTTPIngressPath : v1HTTPIngressPaths) {
                if (v1beta1HTTPIngressPath.getPath() == null) {
                    throw new GitOpsExplainException(GitOpsObjectError.INGRESS_PATHS_PATH_NOT_FOUND.getError(), filePath);
                }
                if (v1beta1HTTPIngressPath.getBackend() == null) {
                    throw new GitOpsExplainException(GitOpsObjectError.INGRESS_PATHS_BACKEND_NOT_FOUND.getError(), filePath);
                } else {
                    checkBackend(v1beta1HTTPIngressPath, filePath);
                }
            }
        }
    }

    private void checkBackend(V1HTTPIngressPath v1beta1HTTPIngressPath, String filePath) {
        V1IngressServiceBackend v1IngressServiceBackend = v1beta1HTTPIngressPath.getBackend().getService();
        if (v1IngressServiceBackend == null || ObjectUtils.isEmpty(v1IngressServiceBackend.getName())) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_BACKEND_SERVICE_NAME_NOT_FOUND.getError(), filePath);
        }
        V1ServiceBackendPort port = v1IngressServiceBackend.getPort();
        if (port == null || port.getNumber() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_BACKEND_SERVICE_PORT_NOT_FOUND.getError(), filePath);
        }
    }
}