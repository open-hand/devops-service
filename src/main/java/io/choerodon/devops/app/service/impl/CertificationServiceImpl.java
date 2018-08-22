package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.CertificationDTO;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.domain.application.entity.CertificationE;
import io.choerodon.devops.domain.application.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.domain.application.repository.DevopsEnvironmentRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import io.choerodon.devops.domain.application.valueobject.certification.*;
import io.choerodon.devops.infra.common.util.CertificationStatus;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CertificationType;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:47
 * Description:
 */

@Service
public class CertificationServiceImpl implements CertificationService {

    private static final Integer ADMIN_ID = 1;
    private static final String CERT_PREFIX = "cert-";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private Gson gson = new Gson();
    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;

    @Autowired
    private IamRepository iamRepository;

    @Override
    public C7nCertification create(Long projectId,
                                   Long envId,
                                   String name,
                                   String type,
                                   List<String> domains,
                                   MultipartFile key,
                                   MultipartFile cert) {
        if (certificationRepository.queryByEnvAndName(envId,
                name) != null) {
            throw new CommonException("Error.cert.exist");
        }
        // 得到环境id
        DevopsEnvironmentE devopsEnvironment = devopsEnvironmentRepository.queryById(envId);
        C7nCertification c7nCertification = getC7nCertification(projectId,
                name,
                type,
                domains,
                key,
                cert,
                devopsEnvironment.getCode());

        // sent certification to agent
//        ObjectOperation<C7nCertification> certificationOperation = new ObjectOperation<>();
//        certificationOperation.setType(c7nCertification);
//        certificationOperation.operationEnvGitlabFile(CERT_PREFIX + name,
//                devopsEnvironment.getGitlabEnvProjectId()
//                        .intValue(),
//                "create",
//                TypeUtil.objToLong(ADMIN_ID),
//                null,
//                null,
//                null,
//                null);

        // status operating
        CertificationE certificationE = new CertificationE(null,
                name,
                devopsEnvironment,
                domains,
                CertificationStatus.OPERATING.getStatus());
        certificationRepository.create(certificationE);
        return null;
    }

    private C7nCertification getC7nCertification(Long projectId,
                                                 String name,
                                                 String type,
                                                 List<String> domains,
                                                 MultipartFile key,
                                                 MultipartFile cert,
                                                 String envCode) {
        C7nCertification c7nCertification = new C7nCertification();

        c7nCertification.setMetadata(new CertificationMetadata(name,
                envCode));
        CertificationSpec spec = new CertificationSpec();
        if (type.equals(CertificationType.REQUEST.getType())) {
            CertificationAcme acme = new CertificationAcme();
            acme.initSetConfig(new CertificationConfig(domains));
            spec.setAcme(acme);
        } else if (type.equals(CertificationType.UPLOAD.getType())) {

            ProjectE projectE = iamRepository.queryIamProject(projectId);
            String classPath = String.format("tmp%s%s%s%s",
                    FILE_SEPARATOR,
                    projectE.getCode(),
                    FILE_SEPARATOR,
                    envCode);

            String keyContent = FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(classPath,
                    key)));
            String certContent = FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(classPath,
                    cert)));
            CertificationExistCert existCert = new CertificationExistCert(keyContent,
                    certContent);
            spec.setExistCert(existCert);
        }
        spec.setCommonName(domains.get(0));
        spec.setDnsName(domains.stream()
                .skip(1)
                .collect(Collectors.toList()));
        c7nCertification.setSpec(spec);
        return c7nCertification;
    }

    @Override
    public void deleteById(Long certId) {
        certificationRepository.deleteById(certId);
    }

    @Override
    public Page<CertificationDTO> getByEnvid(PageRequest pageRequest,
                                             Long envId,
                                             String params) {
        return certificationRepository.getCertification(envId, pageRequest, params);
    }

    @Override
    public List<CertificationDTO> getActiveByDomain(Long envId, String domain) {
        return certificationRepository.getActiveByDomain(envId, domain);
    }
}
