package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.api.vo.kubernetes.C7nCertification;
import io.choerodon.devops.api.vo.kubernetes.certification.*;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;


public class ConvertC7nCertificationServiceImpl extends ConvertK8sObjectService<C7nCertification> {

    private CertificationService certificationService;
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    public ConvertC7nCertificationServiceImpl() {
        this.certificationService = ApplicationContextHelper.getSpringFactory().getBean(CertificationService.class);
        this.devopsEnvFileResourceService = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceService.class);
    }

    @Override
    public void checkParameters(C7nCertification c7nCertification, Map<String, String> objectPath) {
        String filePath = objectPath.get(TypeUtil.objToString(c7nCertification.hashCode()));
        if (c7nCertification.getApiVersion() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CERT_API_VERSION_NOT_FOUND.getError(), filePath);
        }
        checkMetadata(filePath, c7nCertification.getMetadata());
        checkSpec(c7nCertification.getSpec(), filePath);
    }

    @Override
    public void checkIfExist(List<C7nCertification> c7nCertifications, Long envId,
                             List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, C7nCertification c7nCertification) {
        String filePath = objectPath.get(TypeUtil.objToString(c7nCertification.hashCode()));
        String certName = c7nCertification.getMetadata().getName();
        CertificationDTO certificationDTO = certificationService.baseQueryByEnvAndName(envId, certName);
        if (certificationDTO != null) {
            Long certId = certificationDTO.getId();
            if (beforeSyncDelete.stream()
                    .filter(devopsEnvFileResourceDTO -> devopsEnvFileResourceDTO.getResourceType()
                            .equals(c7nCertification.getKind()))
                    .noneMatch(devopsEnvFileResourceE ->
                            devopsEnvFileResourceE.getResourceId()
                                    .equals(certId))) {
                DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                        .baseQueryByEnvIdAndResourceId(envId, certificationDTO.getId(), c7nCertification.getKind());
                if (devopsEnvFileResourceDTO != null && !devopsEnvFileResourceDTO.getFilePath()
                        .equals(objectPath.get(TypeUtil.objToString(c7nCertification.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError() + certName, filePath);
                }
            }
        }
        if (c7nCertifications.stream()
                .anyMatch(certification -> certification.getMetadata().getName()
                        .equals(certName))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError() + certName, filePath);
        } else {
            c7nCertifications.add(c7nCertification);
        }

    }

    private void checkSpec(CertificationSpec spec, String filePath) {
        if (spec == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CERT_SPEC_NOT_FOUND.getError(), filePath);
        } else {
            if (spec.getCommonName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.CERT_COMMON_NAME_NOT_FOUND.getError(), filePath);
            }
            CertificationExistCert existCert = spec.getExistCert();
            if (existCert != null) {
                checkExistCert(filePath, existCert);

            } else {
                CertificationAcme acme = spec.getAcme();
                if (acme != null) {
                    checkAcmeAndDomains(spec, filePath, acme);
                } else {
                    throw new GitOpsExplainException(GitOpsObjectError.CERT_ACME_OR_EXIST_CERT_NOT_FOUND.getError(), filePath);
                }
            }
        }
    }

    private void checkExistCert(String filePath, CertificationExistCert existCert) {
        if (existCert.getCert() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CERT_CRT_NOT_FOUND.getError(), filePath);
        }
        if (existCert.getKey() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CERT_KEY_NOT_FOUND.getError(), filePath);
        }
    }

    private void checkAcmeAndDomains(CertificationSpec spec, String filePath, CertificationAcme acme) {
        if (acme.getConfig() == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CERT_ACME_CONFIG_NOT_FOUND.getError(), filePath);
        } else {
            CertificationConfig certificationConfig = acme.getConfig().get(0);
            checkCertConfig(filePath, certificationConfig);
            List<String> certificationConfigDomains = certificationConfig.getDomains();
            if (certificationConfigDomains == null || certificationConfigDomains.isEmpty()) {
                throw new GitOpsExplainException(GitOpsObjectError.CERT_DOMAINS_NOT_FOUND.getError(), filePath);
            }

            List<String> domains = new ArrayList<>();
            if (spec.getDnsNames() != null) {
                domains.addAll(spec.getDnsNames());
            }
            domains.add(spec.getCommonName());
            domains.sort(String::compareTo);
            if (!certificationConfigDomains.stream().sorted().collect(Collectors.toList()).equals(domains)) {
                throw new GitOpsExplainException(GitOpsObjectError.CERT_DOMAINS_ILLEGAL.getError(), filePath);
            }
        }
    }

    private void checkCertConfig(String filePath, CertificationConfig certificationConfig) {
        Map<String, String> http01 = certificationConfig.getHttp01();
        if (http01 == null || http01.isEmpty()) {
            throw new GitOpsExplainException(GitOpsObjectError.CERT_HTTP_NOT_FOUND.getError(), filePath);
        } else {
            if (!"nginx".equals(http01.get("ingressClass"))) {
                throw new GitOpsExplainException(GitOpsObjectError.CERT_INGRESS_CLASS_ERROR.getError(), filePath);
            }
        }
    }

    private void checkMetadata(String filePath, CertificationMetadata metadata) {
        if (metadata == null) {
            throw new GitOpsExplainException(GitOpsObjectError.CERT_META_DATA_NOT_FOUND.getError(), filePath);
        } else {
            if (metadata.getName() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.CERT_NAME_NOT_FOUND.getError(), filePath);
            }
            if (metadata.getNamespace() == null) {
                throw new GitOpsExplainException(GitOpsObjectError.CERT_NAMESPACE_NOT_FOUND.getError(), filePath);
            }
        }
    }

}
