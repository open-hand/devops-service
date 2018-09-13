package io.choerodon.devops.domain.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.domain.application.entity.CertificationE;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import io.choerodon.devops.domain.application.valueobject.certification.CertificationExistCert;
import io.choerodon.devops.domain.application.valueobject.certification.CertificationSpec;
import io.choerodon.devops.domain.service.HandlerObjectFileRelationsService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.*;
import io.choerodon.devops.infra.dataobject.CertificationFileDO;


@Service
public class HandlerC7nCertificationServiceImpl implements HandlerObjectFileRelationsService<C7nCertification> {

    private static final String CERTIFICATE = "Certificate";

    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private CertificationService certificationService;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;

    @Override
    public void handlerRelations(Map<String, String> objectPath, List<DevopsEnvFileResourceE> beforeSync,
                                 List<C7nCertification> c7nCertifications, Long envId, Long projectId, String path) {
        //todo command操作
        List<C7nCertification> updateC7nCertification = new ArrayList<>();
        List<String> beforeC7nCertification = beforeSync.parallelStream()
                .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType().equals(CERTIFICATE))
                .map(devopsEnvFileResourceE -> {
                    CertificationE certificationE = certificationRepository
                            .queryById(devopsEnvFileResourceE.getResourceId());
                    if (certificationE == null) {
                        throw new CommonException("certification.not.exist.in.database", null, certificationE.getName(), null);
                    }
                    return certificationE.getName();
                })
                .collect(Collectors.toList());

        List<C7nCertification> addC7nCertification = new ArrayList<>();
        c7nCertifications.parallelStream()
                .forEach(certification -> {
                    int index = beforeC7nCertification.indexOf(certification.getMetadata().getName());
                    if (index != -1) {
                        updateC7nCertification.add(certification);
                        beforeC7nCertification.remove(index);
                    } else {
                        addC7nCertification.add(certification);
                    }
                });
        updateC7nCertification.forEach(c7nCertification1 ->
                updateC7nCertificationPath(c7nCertification1, envId, objectPath));
        beforeC7nCertification
                .forEach(certName -> {
                    CertificationE certificationE = certificationRepository.queryByEnvAndName(envId, certName);
                    certificationService.deleteById(certificationE.getId(), true);
                    devopsEnvFileResourceRepository
                            .deleteByEnvIdAndResource(envId, certificationE.getId(), ObjectType.CERTIFICATE.getType());
                    certificationService.createCertCommandE(CommandType.DELETE.getType(), certificationE.getId());
                });
        addC7nCertification.parallelStream().forEach(c7nCertification -> {
            String filePath = "";
            try {
                filePath = objectPath.get(TypeUtil.objToString(c7nCertification.hashCode()));
                DevopsEnvFileResourceE devopsEnvFileResourceE = new DevopsEnvFileResourceE();
                devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
                devopsEnvFileResourceE.setFilePath(objectPath.get(TypeUtil.objToString(c7nCertification.hashCode())));
                devopsEnvFileResourceE.setResourceId(
                        createCertificationAndGetId(envId, c7nCertification, c7nCertification.getMetadata().getName()));
                devopsEnvFileResourceE.setResourceType(c7nCertification.getKind());
                devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
            } catch (Exception e) {
                throw new GitOpsExplainException(e.getMessage(), filePath, e);
            }
        });
    }

    private void updateC7nCertificationPath(C7nCertification c7nCertification,
                                            Long envId, Map<String, String> objectPath) {
        Long certId = checkC7nCertificationChanges(c7nCertification, envId, objectPath);

        String kind = c7nCertification.getKind();
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(envId, certId, kind);
        devopsEnvFileResourceService.updateOrCreateFileResource(objectPath, envId,
                devopsEnvFileResourceE, c7nCertification.hashCode(), certId, kind);
    }

    private Long checkC7nCertificationChanges(C7nCertification c7nCertification, Long envId, Map<String, String> objectPath) {
        DevopsEnvironmentE environmentE = devopsEnvironmentRepository.queryById(envId);
        String certName = c7nCertification.getMetadata().getName();
        CertificationE certificationE = certificationRepository.queryByEnvAndName(envId, certName);
        CertificationFileDO certificationFileDO = certificationRepository.getCertFile(certificationE.getId());
        String type;
        String keyContent = null;
        String certContent = null;
        if (certificationFileDO != null) {
            type = CertificationType.UPLOAD.getType();
            keyContent = certificationFileDO.getKeyFile();
            certContent = certificationFileDO.getCertFile();
        } else {
            type = CertificationType.REQUEST.getType();
        }
        String filePath = objectPath.get(TypeUtil.objToString(c7nCertification.hashCode()));
        C7nCertification oldC7nCertification = certificationService.getC7nCertification(
                certName, type, certificationE.getDomains(), keyContent, certContent, environmentE.getCode());
        if (!c7nCertification.equals(oldC7nCertification)) {
            throw new GitOpsExplainException(GitOpsObjectError.CERT_CHANGED.getError(), filePath);
        }
        return certificationE.getId();
    }

    private Long createCertificationAndGetId(Long envId, C7nCertification c7nCertification, String certName) {
        CertificationE certificationE = certificationRepository
                .queryByEnvAndName(envId, certName);
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
            certificationE.setEnvironmentE(new DevopsEnvironmentE(envId));
            certificationE.setName(certName);
            certificationE.setStatus(CertificationStatus.OPERATING.getStatus());
            certificationE = certificationRepository.create(certificationE);
            CertificationExistCert existCert = c7nCertification.getSpec().getExistCert();
            if (existCert != null) {
                certificationRepository.storeCertFile(
                        new CertificationFileDO(certificationE.getId(), existCert.getCert(), existCert.getKey()));
            }
            certificationService.createCertCommandE(CommandType.CREATE.getType(), certificationE.getId());
        }
        return certificationE.getId();
    }
}
