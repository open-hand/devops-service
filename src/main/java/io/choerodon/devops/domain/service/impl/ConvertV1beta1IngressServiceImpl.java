package io.choerodon.devops.domain.service.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1beta1HTTPIngressPath;
import io.kubernetes.client.models.V1beta1Ingress;
import io.kubernetes.client.models.V1beta1IngressRule;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.entity.DevopsIngressE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsIngressRepository;
import io.choerodon.devops.domain.service.ConvertK8sObjectService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;

public class ConvertV1beta1IngressServiceImpl extends ConvertK8sObjectService<V1beta1Ingress> {

    private DevopsIngressRepository devopsIngressRepository;
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    public ConvertV1beta1IngressServiceImpl() {
        this.devopsIngressRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsIngressRepository.class);
        this.devopsEnvFileResourceRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceRepository.class);
    }

    public void checkIfExist(List<V1beta1Ingress> v1beta1Ingresses, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, V1beta1Ingress v1beta1Ingress) {
        String filePath = objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode()));
        DevopsIngressE devopsIngressE = devopsIngressRepository.selectByEnvAndName(envId, v1beta1Ingress.getMetadata().getName());
        if (devopsIngressE != null
                && beforeSyncDelete.parallelStream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(v1beta1Ingress.getKind()))
                .noneMatch(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceId().equals(devopsIngressE.getId()))) {
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, devopsIngressE.getId(), v1beta1Ingress.getKind());
            if (devopsEnvFileResourceE != null && !devopsEnvFileResourceE.getFilePath().equals(objectPath.get(TypeUtil.objToString(v1beta1Ingress.hashCode())))) {
                throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError() + v1beta1Ingress.getMetadata().getName(), filePath);
            }
        }
        if (v1beta1Ingresses.parallelStream().anyMatch(v1beta1Ingress1 -> v1beta1Ingress1.getMetadata().getName().equals(v1beta1Ingress.getMetadata().getName()))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError() + v1beta1Ingress.getMetadata().getName(), filePath);
        } else {
            v1beta1Ingresses.add(v1beta1Ingress);
        }

    }


    public void checkParameters(V1beta1Ingress v1beta1Ingress, Map<String, String> objectPath) {
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
            checkV1beta1IngressRules(v1beta1Ingress, filePath);
        }
        if (v1beta1Ingress.getApiVersion() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_API_VERSION_NOT_FOUND.getError(), filePath);

        }
    }


    private void checkV1beta1IngressRules(V1beta1Ingress v1beta1Ingress, String filePath) {
        List<V1beta1IngressRule> v1beta1IngressRules = v1beta1Ingress.getSpec().getRules();
        if (v1beta1IngressRules.isEmpty()) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_RULES_NOT_FOUND.getError(), filePath);
        } else {
            for (V1beta1IngressRule v1beta1IngressRule : v1beta1IngressRules) {
                if (v1beta1IngressRule.getHost() == null) {
                    throw new GitOpsExplainException(GitOpsObjectError.INGRESS_RULE_HOST_NOT_FOUND.getError(), filePath);
                }
                if (v1beta1IngressRule.getHttp() == null) {
                    throw new GitOpsExplainException(GitOpsObjectError.INGRESS_RULE_HTTP_NOT_FOUND.getError(), filePath);
                } else {
                    checkV1beta1HTTPIngressPaths(v1beta1IngressRule, filePath);
                }
            }
        }
    }

    private void checkV1beta1HTTPIngressPaths(V1beta1IngressRule v1beta1IngressRule, String filePath) {
        List<V1beta1HTTPIngressPath> v1beta1HTTPIngressPaths = v1beta1IngressRule.getHttp().getPaths();
        if (v1beta1HTTPIngressPaths.isEmpty()) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_PATHS_NOT_FOUND.getError(), filePath);
        } else {
            for (V1beta1HTTPIngressPath v1beta1HTTPIngressPath : v1beta1HTTPIngressPaths) {
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

    private void checkBackend(V1beta1HTTPIngressPath v1beta1HTTPIngressPath, String filePath) {
        if (v1beta1HTTPIngressPath.getBackend().getServiceName() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_BACKEND_SERVICE_NAME_NOT_FOUND.getError(), filePath);
        }
        if (v1beta1HTTPIngressPath.getBackend().getServicePort() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.INGRESS_BACKEND_SERVICE_PORT_NOT_FOUND.getError(), filePath);
        }
    }

}