package io.choerodon.devops.domain.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.domain.application.entity.CertificationE;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import io.choerodon.devops.domain.application.valueobject.certification.CertificationSpec;
import io.choerodon.devops.domain.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CertificationStatus;
import io.choerodon.devops.infra.common.util.enums.ObjectType;


@Service
public class HandlerC7nCertificationServiceImpl implements HandlerObjectFileRelationsService<C7nCertification> {

    public static final String CERTIFICATE = "Certificate";

    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private CertificationService certificationService;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync, List<C7nCertification> c7nCertifications, Long envId, Long projectId, String path) {
        beforeSync.parallelStream().filter(devopsEnvFileResourceE ->
                devopsEnvFileResourceE.getResourceType().equals(CERTIFICATE))
                .map(devopsEnvFileResourceE -> {
                    CertificationE certificationE = certificationRepository
                            .queryById(devopsEnvFileResourceE.getResourceId());
                    if (certificationE == null) {
                        throw new CommonException("the certification in the file is not exist in devops database");
                    }
                    return certificationE.getName();
                })
                .forEach(certName -> {
                    CertificationE certificationE = certificationRepository.queryByEnvAndName(envId, certName);
                    certificationService.deleteById(certificationE.getId(), true);
                    devopsEnvFileResourceRepository
                            .deleteByEnvIdAndResource(envId, certificationE.getId(), ObjectType.CERTIFICATE.getType());
                });
        c7nCertifications.parallelStream().forEach(c7nCertification -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(c7nCertification.hashCode()));
                DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(c7nCertification.hashCode())));
                devopsEnvFileResourceE.setResourceId(
                        getOrCreateCertificationId(envId, c7nCertification, c7nCertification.getMetadata().getName()));
                devopsEnvFileResourceE.setResourceType(c7nCertification.getKind());
                devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
            } catch (Exception e) {
                throw new GitOpsExplainException(e.getMessage(), filePath, e);
            }
        });
    }


    private Long getOrCreateCertificationId(Long envId, C7nCertification c7nCertification, String certName) {
        CertificationE certificationE = certificationRepository
                .queryByEnvAndName(envId, certName);
        DevopsEnvironmentE environmentE = new DevopsEnvironmentE(envId);
        if (certificationE == null) {
            certificationE = new CertificationE();
            CertificationSpec certificationSpec = c7nCertification.getSpec();
            String domain = certificationSpec.getCommonName();
            List<String> dnsDomain = certificationSpec.getDnsNames();
            List<String> domains = new ArrayList<>();
            domains.add(domain);
            if (dnsDomain != null && !dnsDomain.isEmpty()) {
                domains.addAll(dnsDomain);
            }
            certificationE.setDomains(domains);
            certificationE.setEnvironmentE(environmentE);
            certificationE.setName(certName);
            certificationE.setStatus(CertificationStatus.OPERATING.getStatus());
            certificationE = certificationRepository.create(certificationE);
        }
        return certificationE.getId();
    }

}
