package io.choerodon.devops.domain.service.impl;

import java.util.List;
import java.util.Map;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.devops.domain.application.entity.CertificationE;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.handler.GitOpsExplainException;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import io.choerodon.devops.domain.service.ConvertK8sObjectService;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.GitOpsObjectError;

public class ConvertC7nCertificationServiceImpl extends ConvertK8sObjectService<C7nCertification> {

    private CertificationRepository certificationRepository;
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;

    public ConvertC7nCertificationServiceImpl() {
        this.certificationRepository = ApplicationContextHelper.getSpringFactory().getBean(CertificationRepository.class);
        this.devopsEnvFileResourceRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileResourceRepository.class);
    }

    public void checkParameters(C7nCertification c7nCertification, Map<String, String> objectPath) {
    }

    public void checkIfexist(List<C7nCertification> c7nCertifications, Long envId,
                             List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, C7nCertification c7nCertification) {
        String filePath = objectPath.get(TypeUtil.objToString(c7nCertification.hashCode()));
        CertificationE certificationE = certificationRepository.queryByEnvAndName(envId, c7nCertification.getMetadata().getName());
        if (certificationE != null) {
            Long certId = certificationE.getId();
            if (beforeSyncDelete.parallelStream()
                    .filter(devopsEnvFileResourceE -> devopsEnvFileResourceE.getResourceType()
                            .equals(c7nCertification.getKind()))
                    .noneMatch(devopsEnvFileResourceE ->
                            devopsEnvFileResourceE.getResourceId()
                                    .equals(certId))) {
                DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository.queryByEnvIdAndResource(envId, certificationE.getId(), c7nCertification.getKind());
                if (devopsEnvFileResourceE != null && !devopsEnvFileResourceE.getFilePath().equals(objectPath.get(TypeUtil.objToString(c7nCertification.hashCode())))) {
                    throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError() + c7nCertification.getMetadata().getName(), filePath);
                }
            }
        }
        if (c7nCertifications.parallelStream()
                .anyMatch(certification -> certification.getMetadata().getName()
                        .equals(certificationE.getName()))) {
            throw new GitOpsExplainException(GitOpsObjectError.OBJECT_EXIST.getError() + c7nCertification.getMetadata().getName(), filePath);
        } else {
            c7nCertifications.add(c7nCertification);
        }

    }
}
