package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.C7nCertificationDTO;
import io.choerodon.devops.api.dto.CertificationDTO;
import io.choerodon.devops.api.validator.DevopsCertificationValidator;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.handler.ObjectOperation;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import io.choerodon.devops.domain.application.valueobject.certification.*;
import io.choerodon.devops.infra.common.util.EnvUtil;
import io.choerodon.devops.infra.common.util.FileUtil;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.*;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.websocket.helper.EnvListener;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:47
 * Description:
 */
@Service
public class CertificationServiceImpl implements CertificationService {

    private static final String CERT_PREFIX = "cert-";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    @Value("${cert.testCert}")
    private Boolean testCert;

    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DevopsCertificationValidator devopsCertificationValidator;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private EnvListener envListener;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;


    @Override
    public void create(Long projectId, C7nCertificationDTO certificationDTO,
                       MultipartFile key, MultipartFile cert, Boolean isGitOps) {
        String certName = certificationDTO.getCertName();
        String type = certificationDTO.getType();
        List<String> domains = certificationDTO.getDomains();

        Long envId = certificationDTO.getEnvId();
        devopsCertificationValidator.checkCertification(envId, certName);
        // agent certification
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(CommandType.CREATE.getType());

        // status operating
        CertificationE certificationE = new CertificationE(null,
                certName, devopsEnvironmentE, domains, CertificationStatus.OPERATING.getStatus());
        // create
        if (!isGitOps) {
            C7nCertification c7nCertification = getC7nCertification(
                    projectId, certName, type, domains, key, cert, devopsEnvironmentE.getCode());

            // sent certification to agent
            ObjectOperation<C7nCertification> certificationOperation = new ObjectOperation<>();
            certificationOperation.setType(c7nCertification);
            operateEnvGitLabFile(certName, devopsEnvironmentE, c7nCertification);
        }
        devopsEnvCommandE.setObjectId(certificationRepository.create(certificationE).getId());
        certificationE.setCommandId(devopsEnvCommandRepository.create(devopsEnvCommandE).getId());
        certificationRepository.updateCommandId(certificationE);
    }

    private C7nCertification getC7nCertification(Long projectId, String name, String type, List<String> domains,
                                                 MultipartFile key, MultipartFile cert, String envCode) {
        C7nCertification c7nCertification = new C7nCertification();

        c7nCertification.setMetadata(new CertificationMetadata(name,
                envCode));
        CertificationSpec spec = new CertificationSpec(testCert);
        if (type.equals(CertificationType.REQUEST.getType())) {
            CertificationAcme acme = new CertificationAcme();
            acme.initConfig(new CertificationConfig(domains));
            spec.setAcme(acme);
        } else if (type.equals(CertificationType.UPLOAD.getType())) {

            ProjectE projectE = iamRepository.queryIamProject(projectId);
            String path = String.format("tmp%s%s%s%s",
                    FILE_SEPARATOR,
                    projectE.getCode(),
                    FILE_SEPARATOR,
                    envCode);

            String keyContent = FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, key)));
            String certContent = FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, cert)));

            CertificationExistCert existCert = new CertificationExistCert(keyContent, certContent);
            spec.setExistCert(existCert);
        }
        spec.setCommonName(domains.get(0));
        spec.setDnsNames(domains.size() > 1 ? domains.stream().skip(1).collect(Collectors.toList()) : null);
        c7nCertification.setSpec(spec);
        return c7nCertification;
    }

    private void operateEnvGitLabFile(String certName,
                                      DevopsEnvironmentE devopsEnvironmentE,
                                      C7nCertification c7nCertification) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);
        devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        ObjectOperation<C7nCertification> objectOperation = new ObjectOperation<>();
        objectOperation.setType(c7nCertification);
        objectOperation.operationEnvGitlabFile(CERT_PREFIX + certName,
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "create",
                userAttrE.getGitlabUserId(), null, null, null, null);
    }

    @Override
    public void deleteById(Long certId, Boolean isGitOps) {
        CertificationE certificationE = certificationRepository.queryById(certId);
        Long certEnvId = certificationE.getEnvironmentE().getId();
        envUtil.checkEnvConnection(certEnvId, envListener);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(certEnvId);

        if (!isGitOps) {
            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);
            Integer gitLabEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            String certificateType = ObjectType.CERTIFICATE.getType();
            String certName = certificationE.getName();
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                    .queryByEnvIdAndResource(certEnvId, certId, certificateType);
            if (devopsEnvFileResourceE != null && devopsEnvFileResourceE.getFilePath() != null
                    && devopsEnvFileResourceRepository
                    .queryByEnvIdAndPath(certEnvId, devopsEnvFileResourceE.getFilePath()).size() == 1) {
                gitlabRepository.deleteFile(
                        gitLabEnvProjectId,
                        devopsEnvFileResourceE.getFilePath(),
                        "DELETE FILE " + certName,
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            } else {
                ObjectOperation<C7nCertification> certificationOperation = new ObjectOperation<>();
                C7nCertification c7nCertification = new C7nCertification();
                CertificationMetadata certificationMetadata = new CertificationMetadata();
                certificationMetadata.setName(certName);
                c7nCertification.setMetadata(certificationMetadata);
                certificationOperation.setType(c7nCertification);
                certificationOperation.operationEnvGitlabFile(
                        null, gitLabEnvProjectId,
                        "delete", userAttrE.getGitlabUserId(), certId, certificateType, certEnvId,
                        devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE));
            }
        }
        DevopsEnvCommandE devopsEnvCommandE = initDevopsEnvCommandE(CommandType.DELETE.getType());
        devopsEnvCommandE.setObjectId(certId);
        devopsEnvCommandRepository.create(devopsEnvCommandE);
        certificationRepository.deleteById(certId);
    }

    @Override
    public Page<CertificationDTO> page(Long projectId, Long envId, PageRequest pageRequest, String params) {
        if (params == null) {
            params = "{}";
        }
        return certificationRepository.page(projectId, envId, pageRequest, params);
    }

    @Override
    public List<CertificationDTO> getActiveByDomain(Long envId, String domain) {
        return certificationRepository.getActiveByDomain(envId, domain);
    }

    @Override
    public Boolean checkCertNameUniqueInEnv(Long envId, String certName) {
        return certificationRepository.checkCertNameUniqueInEnv(envId, certName);
    }

    private DevopsEnvCommandE initDevopsEnvCommandE(String type) {
        DevopsEnvCommandE devopsEnvCommandE = new DevopsEnvCommandE();
        devopsEnvCommandE.setCommandType(type);
        devopsEnvCommandE.setObject(ObjectType.CERTIFICATE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.DOING.getStatus());
        return devopsEnvCommandE;
    }

}
