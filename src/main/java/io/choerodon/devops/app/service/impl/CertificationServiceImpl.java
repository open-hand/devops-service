package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import io.choerodon.devops.infra.common.util.enums.CertificationStatus;
import io.choerodon.devops.infra.common.util.enums.CertificationType;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
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

    private static final Integer ADMIN_ID = 1;
    private static final String CERT_PREFIX = "cert-";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

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
        C7nCertification c7nCertification = getC7nCertification(
                projectId, certName, type, domains, key, cert, devopsEnvironmentE.getCode());

        // sent certification to agent
        ObjectOperation<C7nCertification> certificationOperation = new ObjectOperation<>();
        certificationOperation.setType(c7nCertification);
        certificationOperation.operationEnvGitlabFile(
                CERT_PREFIX + certName, devopsEnvironmentE.getGitlabEnvProjectId().intValue(),
                "create", TypeUtil.objToLong(ADMIN_ID),
                null, null, null, null);

        // status operating
        CertificationE certificationE = new CertificationE(null,
                certName, devopsEnvironmentE, domains, CertificationStatus.OPERATING.getStatus());
        // create
        if (isGitOps) {
            certificationRepository.create(certificationE);
        } else {
            operateEnvGitLabFile(certName, devopsEnvironmentE,
                    c7nCertification, devopsEnvironmentE.getId(), certificationE);
        }
    }

    private C7nCertification getC7nCertification(Long projectId, String name, String type, List<String> domains,
                                                 MultipartFile key, MultipartFile cert, String envCode) {
        C7nCertification c7nCertification = new C7nCertification();

        c7nCertification.setMetadata(new CertificationMetadata(name,
                envCode));
        CertificationSpec spec = new CertificationSpec();
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
        spec.setDnsNames(domains.stream().skip(1).collect(Collectors.toList()));
        c7nCertification.setSpec(spec);
        return c7nCertification;
    }

    private void operateEnvGitLabFile(String certName,
                                      DevopsEnvironmentE devopsEnvironmentE,
                                      C7nCertification c7nCertification,
                                      Long envId,
                                      CertificationE certificationE) {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);
        String path = devopsEnvironmentService.handDevopsEnvGitRepository(devopsEnvironmentE);

        ObjectOperation<C7nCertification> objectOperation = new ObjectOperation<>();
        objectOperation.setType(c7nCertification);
        objectOperation.operationEnvGitlabFile("ing-" + certName,
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "create",
                userAttrE.getGitlabUserId(), null, "Ingress", envId, path);
        certificationRepository.create(certificationE);
    }

    /*
     * 通过id删除证书，含gitops操作
     * */
    @Override
    public void deleteById(Long certId, Boolean isGitOps) {
        // 通过证书id得到实体类对象E
        CertificationE certificationE = certificationRepository.queryById(certId);
        // 得到证书部署的环境id
        Long certEnvId = certificationE.getEnvironmentE().getId();
        // 检测环境是否连接，固定操作
        envUtil.checkEnvConnection(certEnvId, envListener);
        // 得到证书环境实体类对象E
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(certEnvId);

        if (isGitOps) {
            // 如果有gitops操作，就直接在数据库中删掉对应的证书
            certificationRepository.deleteById(certId);
        } else {
            // 得到操作用户，固定操作
            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            // 1项目是否存在，2用户是否存在，3用户是否有操作权限，固定操作
            gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);
            // 得到gitlab环境下的项目的项目id
            Integer gitLabEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
            // 得到部署类型，固定操作，类型为证书
            // 部署类型只有 1实例instance 2网络service 3域名ingress 4证书certification
            String certificateType = ObjectType.CERTIFICATE.getType();
            // 得到证书名字
            String certName = certificationE.getName();
            // 通过证书环境id，证书id，部署类型(类型为证书CERTIFICATION) 得到部署资源文件
            DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                    .queryByEnvIdAndResource(certEnvId, certId, certificateType);
            // 得到部署资源文件路径
            // 使用List因为
            List<DevopsEnvFileResourceE> devopsEnvFileResourceES = devopsEnvFileResourceRepository
                    .queryByEnvIdAndPath(certEnvId, devopsEnvFileResourceE.getFilePath());
            if (devopsEnvFileResourceES.size() == 1) {
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
            certificationE.setStatus(CertificationStatus.OPERATING.getStatus());
            certificationRepository.updateStatus(certificationE);
        }
    }

    @Override
    public Page<CertificationDTO> page(PageRequest pageRequest, String params) {
        return certificationRepository.page(pageRequest, params);
    }

    @Override
    public List<CertificationDTO> getActiveByDomain(String domain) {
        return certificationRepository.getActiveByDomain(domain);
    }
}
