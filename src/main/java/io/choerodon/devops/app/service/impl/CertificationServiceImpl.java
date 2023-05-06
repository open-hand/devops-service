package io.choerodon.devops.app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netflix.servo.util.Strings;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsCertificationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.C7nCertification;
import io.choerodon.devops.api.vo.kubernetes.certification.*;
import io.choerodon.devops.app.eventhandler.constants.CertManagerConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.ExceptionConstants;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsCertificationFileMapper;
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper;
import io.choerodon.devops.infra.mapper.DevopsIngressMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.message.entity.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.File;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.choerodon.devops.app.eventhandler.constants.CertManagerConstants.NEW_V1_CERT_MANAGER_CHART_VERSION;
import static io.choerodon.devops.app.service.impl.SendNotificationServiceImpl.ENV_AND_CERTIFICATION_LINK;
import static io.choerodon.devops.infra.constant.ExceptionConstants.CertificationExceptionCode.DEVOPS_CERTIFICATION_OPERATE_TYPE_NULL;
import static io.choerodon.devops.infra.constant.ExceptionConstants.IngressExceptionCode.ERROR_DEVOPS_INGRESS_DOMAIN_INVALID;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:47
 * Description:
 */
@Service
public class CertificationServiceImpl implements CertificationService {

    public static final Pattern DOMAIN_REGEX = Pattern.compile("(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$");

    private static final String CERT_PREFIX = "cert-";
    private static final String MASTER = "master";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String DEVOPS_CERT_MANAGER_NOT_INSTALLED = "devops.cert.manager.not.installed";
    private static final String DEVOPS_CERTIFICATION_CREATE = "devops.certification.create";

    private static final String DEVOPS_CERTIFICATION_UPDATE = "devops.certification.update";
    private static final String DEVOPS_INSERT_CERTIFICATION_FILE = "devops.insert.certification.file";

    private static final String DEVOPS_UPDATE_CERTIFICATION_FILE = "devops.update.certification.file";
    private static final Logger LOGGER = LoggerFactory.getLogger(CertificationServiceImpl.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value(value = "${services.front.url: http://app.example.com}")
    private String frontUrl;

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Lazy
    @Autowired
    private DevopsCertificationValidator devopsCertificationValidator;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvFileResourceService devopsEnvFileResourceService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private DevopsEnvCommandService devopsEnvCommandService;
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
    @Autowired
    private DevopsCertificationFileMapper devopsCertificationFileMapper;
    @Autowired
    private DevopsIngressMapper devopsIngressMapper;
    @Autowired
    private SendNotificationService sendNotificationService;
    @Autowired
    private PermissionHelper permissionHelper;
    @Autowired
    private DevopsClusterResourceService devopsClusterResourceService;
    @Autowired
    private DevopsCertificationNoticeService devopsCertificationNoticeService;
    @Autowired
    private ObjectMapper objectMapper;
    /**
     * 前端传入的排序字段和Mapper文件中的字段名的映射
     */
    private static final Map<String, String> orderByFieldMap;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("envName", "de.name");
        map.put("envCode", "de.code");
        map.put("certName", "dc.`name`");
        map.put("commonName", "dc.domains");
        map.put("domain", "dc.domains");
        map.put("name", "dc.`name`");
        map.put("id", "dc.id");
        orderByFieldMap = Collections.unmodifiableMap(map);
    }


    private final Gson gson = new Gson();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdateCertification(Long projectId, C7nCertificationCreateOrUpdateVO c7NCertificationCreateOrUpdateVO, MultipartFile key, MultipartFile cert) {
        if (ObjectUtils.isEmpty(c7NCertificationCreateOrUpdateVO.getOperateType())) {
            throw new CommonException(DEVOPS_CERTIFICATION_OPERATE_TYPE_NULL);
        }
        if (!ObjectUtils.isEmpty(c7NCertificationCreateOrUpdateVO.getNotifyObjectsJsonStr())) {
            try {
                c7NCertificationCreateOrUpdateVO.setNotifyObjects(objectMapper.readValue(c7NCertificationCreateOrUpdateVO.getNotifyObjectsJsonStr(), new TypeReference<List<CertificationNotifyObject>>() {
                }));
            } catch (Exception e) {
                throw new CommonException(ExceptionConstants.CertificationExceptionCode.ERROR_DEVOPS_CERTIFICATION_READ_NOTIFY_OBJECTS);
            }
        }
        C7nCertificationVO certificationVO = processEncryptCertification(c7NCertificationCreateOrUpdateVO);
        Long envId = certificationVO.getEnvId();
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, envId);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        String certManagerVersion = Optional.ofNullable(devopsClusterResourceService.queryCertManagerVersion(devopsEnvironmentDTO.getClusterId())).orElse(NEW_V1_CERT_MANAGER_CHART_VERSION);
        // 校验CertManager已经安装
        // TODO 也许有必要进一步校验 CertManager 的状态是否为可用的
        CommonExAssertUtil.assertNotNull(certManagerVersion, DEVOPS_CERT_MANAGER_NOT_INSTALLED);


        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        String path = String.format("tmp%s%s%s%s", FILE_SEPARATOR, projectDTO.getDevopsComponentCode(), FILE_SEPARATOR, devopsEnvironmentDTO.getCode());

        // status operating
        CertificationDTO newCertificationDTO = new CertificationDTO(certificationVO.getId(),
                certificationVO.getCertName(),
                devopsEnvironmentDTO.getId(),
                CollectionUtils.isEmpty(certificationVO.getDomains()) ? "[]" : gson.toJson(certificationVO.getDomains()),
                certificationVO.getStatus(),
                certificationVO.getCertId(),
                certificationVO.getType(),
                certificationVO.getExpireNotice(),
                certificationVO.getAdvanceDays(),
                certificationVO.getNotifyObjects(),
                certificationVO.getObjectVersionNumber(),
                certificationVO.getKeyValue(),
                certificationVO.getCertValue());
        //如果是选择上传文件方式
        if (newCertificationDTO.getType().equals(CertificationType.UPLOAD.getType())) {
            String certFileName;
            String keyFileName;
            if (key != null && cert != null) {
                certFileName = cert.getOriginalFilename();
                keyFileName = key.getOriginalFilename();
                newCertificationDTO.setKeyValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, key))));
                newCertificationDTO.setCertValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, cert))));
            } else {
                certFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "crt");
                keyFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "key");
                FileUtil.saveDataToFile(path, certFileName, newCertificationDTO.getCertValue());
                FileUtil.saveDataToFile(path, keyFileName, newCertificationDTO.getKeyValue());
            }
            File certFile = new File(path + FILE_SEPARATOR + certFileName);
            File keyFile = new File(path + FILE_SEPARATOR + keyFileName);
            try {
                SslUtil.validate(certFile, keyFile);
                SslUtil.CertInfo certInfo = SslUtil.parseCert(certFile);
                newCertificationDTO.setDomains(CollectionUtils.isEmpty(certInfo.getDomains()) ? "[]" : gson.toJson(certInfo.getDomains()));
                newCertificationDTO.setValidFrom(certInfo.getValidFrom());
                newCertificationDTO.setValidUntil(certInfo.getValidUntil());
            } catch (CommonException e) {
                FileUtil.deleteFile(certFile);
                FileUtil.deleteFile(keyFile);
                throw e;
            }
            FileUtil.deleteFile(certFile);
            FileUtil.deleteFile(keyFile);
        }

        String certName = certificationVO.getCertName();
        String type = certificationVO.getType();

        CertificationFileDTO certificationFileDTO = null;
        //如果创建的时候选择证书
        if (CertificationType.CHOOSE.getType().equals(type) && certificationVO.getCertId() != null) {
            CommonExAssertUtil.assertTrue(permissionHelper.projectPermittedToCert(certificationVO.getCertId(), projectId), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);
            CertificationDTO orgCertificationDTO = baseQueryById(certificationVO.getCertId());
            newCertificationDTO.setDomains(orgCertificationDTO.getDomains());
            newCertificationDTO.setValidFrom(orgCertificationDTO.getValidFrom());
            newCertificationDTO.setValidUntil(orgCertificationDTO.getValidUntil());
            certificationFileDTO = baseQueryCertFile(baseQueryById(certificationVO.getCertId()).getId());
        }

        if (c7NCertificationCreateOrUpdateVO.getOperateType().equals("create")) {
            devopsCertificationValidator.checkCertification(envId, certName);
        }

        boolean needToUpdateGitOps = checkNeedToUpdateGitOpsAndSetStatus(certificationVO, c7NCertificationCreateOrUpdateVO.getOperateType());
        newCertificationDTO.setStatus(certificationVO.getStatus());


        String keyContent;
        String certContent;
        if (certificationFileDTO == null) {
            keyContent = newCertificationDTO.getKeyValue();
            certContent = newCertificationDTO.getCertValue();
        } else {
            keyContent = certificationFileDTO.getKeyFile();
            certContent = certificationFileDTO.getCertFile();
        }

        String apiVersion = CertManagerConstants.OLD_V1_CERT_MANAGER_CHART_VERSION.equals(certManagerVersion) ? C7nCertification.API_VERSION_V1ALPHA1 : C7nCertification.API_VERSION_V1;
        newCertificationDTO.setApiVersion(apiVersion);

        // 存入数据库
        if (c7NCertificationCreateOrUpdateVO.getOperateType().equals("create")) {
            createAndStore(newCertificationDTO, keyContent, certContent);
        } else {
            updateAndStore(newCertificationDTO, keyContent, certContent, needToUpdateGitOps);
        }

        // 将资源对象生成yaml提交到gitlab
        if (needToUpdateGitOps) {
            handleCertificationToGitlab(newCertificationDTO.getId(), certManagerVersion, certName, type, JsonHelper.unmarshalByJackson(newCertificationDTO.getDomains(), new TypeReference<List<String>>() {
            }), keyContent, certContent, devopsEnvironmentDTO, c7NCertificationCreateOrUpdateVO.getOperateType(), clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO, devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvironmentDTO.getType(), devopsEnvironmentDTO.getClusterCode()));
        }
    }

    private void handleCertificationToGitlab(Long certificationId, String certManagerVersion, String certName, String createType, List<String> domains, String keyContent, String certContent, DevopsEnvironmentDTO devopsEnvironmentDTO, String operateType, String filePath) {
        if (CertManagerConstants.OLD_V1_CERT_MANAGER_CHART_VERSION.equals(certManagerVersion)) {
            handleAlpha1Certification(certificationId, certName, createType, domains, keyContent, certContent, devopsEnvironmentDTO, operateType, filePath);
        } else {
            handleV1Certification(certificationId, certName, createType, domains, keyContent, certContent, devopsEnvironmentDTO, operateType, filePath);
        }
    }

    private void handleAlpha1Certification(Long certificationId, String certName, String createType, List<String> domains, String keyContent, String certContent, DevopsEnvironmentDTO devopsEnvironmentDTO, String operateType, String filePath) {
        C7nCertification c7nCertification = getV1Alpha1C7nCertification(certName, createType, domains, keyContent, certContent, devopsEnvironmentDTO.getCode());
        // sent certification to agent
        operateEnvGitLabFile(certName, devopsEnvironmentDTO, c7nCertification, operateType, devopsEnvironmentDTO.getId(), certificationId, filePath);
    }

    private void handleV1Certification(Long certificationId, String certName, String createType, List<String> domains, String keyContent, String certContent, DevopsEnvironmentDTO devopsEnvironmentDTO, String operateType, String filePath) {
        C7nCertification c7nCertification = getV1C7nCertification(certName, createType, domains, keyContent, certContent, devopsEnvironmentDTO.getCode());
        operateEnvGitLabFile(certName, devopsEnvironmentDTO, c7nCertification, operateType, devopsEnvironmentDTO.getId(), certificationId, filePath);
    }

    /**
     * create certification, command and store cert file
     *
     * @param certificationDTO the information of certification
     */
    private void createAndStore(CertificationDTO certificationDTO, @Nullable String keyContent, @Nullable String certContent) {
        // create
        certificationDTO = baseCreate(certificationDTO);
        Long certId = certificationDTO.getId();

        CertificationDTO updateCertificationDTO = new CertificationDTO();
        updateCertificationDTO.setId(certificationDTO.getId());
        updateCertificationDTO.setCommandId(createCertCommand(CommandType.CREATE.getType(), certId, null));
        // cert command
        baseUpdateCommandId(updateCertificationDTO);
        // store crt & key if type is upload
        storeCertFile(keyContent, certContent, certId);
        // 保存通知信息
        storeNotifyInfo(certificationDTO);
    }

    /**
     * update certification, command and store cert file
     *
     * @param certificationDTO the information of certification
     */
    private void updateAndStore(CertificationDTO certificationDTO, @Nullable String keyContent, @Nullable String certContent, Boolean needToUpdateGitOps) {
        // create
        certificationDTO.setName(null);
        certificationDTO = baseUpdate(certificationDTO);
        Long certId = certificationDTO.getId();

        if (Boolean.TRUE.equals(needToUpdateGitOps)) {
            CertificationDTO updateCertificationDTO = new CertificationDTO();
            updateCertificationDTO.setId(certificationDTO.getId());
            updateCertificationDTO.setCommandId(createCertCommand(CommandType.UPDATE.getType(), certId, null));
            // cert command
            baseUpdateCommandId(updateCertificationDTO);
        }
        // store crt & key if type is upload
        updateCertFile(keyContent, certContent, certificationDTO);

        // 保存通知信息
        updateNotifyInfo(certificationDTO);
    }


    private void storeCertFile(String keyContent, String certContent, Long certId) {
        if (keyContent != null && certContent != null) {
            CertificationDTO certificationDTO = new CertificationDTO();
            certificationDTO.setCertificationFileId(baseStoreCertFile(new CertificationFileDTO(certContent, keyContent)));
            certificationDTO.setId(certId);
            baseUpdateCertFileId(certificationDTO);
        }
    }

    private void updateCertFile(String keyContent, String certContent, CertificationDTO certificationDTO) {
        if (keyContent != null && certContent != null) {
            CertificationFileDTO certificationFileDTO = devopsCertificationFileMapper.queryByCertificationId(certificationDTO.getId());
            certificationFileDTO.setKeyFile(keyContent);
            certificationFileDTO.setCertFile(certContent);
            baseUpdateCertFile(certificationFileDTO);
        }
    }

    @Override
    public C7nCertification getV1Alpha1C7nCertification(String name, String type, List<String> domains, String keyContent, String certContent, String envCode) {
        C7nCertification c7nCertification = new C7nCertification(C7nCertification.API_VERSION_V1ALPHA1);

        c7nCertification.setMetadata(new CertificationMetadata(name, envCode));
        CertificationSpec spec = new CertificationSpec(type);
        if (type.equals(CertificationType.REQUEST.getType())) {
            CertificationAcme acme = new CertificationAcme();
            acme.initConfig(new CertificationConfig(domains));
            spec.setAcme(acme);
        } else {
            CertificationExistCert existCert = new CertificationExistCert(keyContent, certContent);
            spec.setExistCert(existCert);
        }
        if (!CollectionUtils.isEmpty(domains)) {
            spec.setCommonName(domains.get(0));
        }
        spec.setDnsNames(domains.size() > 1 ? domains.stream().skip(1).collect(Collectors.toList()) : null);
        c7nCertification.setSpec(spec);
        return c7nCertification;
    }

    @Override
    public C7nCertification getV1C7nCertification(String name, String type, List<String> domains, String keyContent, String certContent, String envCode) {
        C7nCertification c7nCertification = new C7nCertification(C7nCertification.API_VERSION_V1);
        c7nCertification.setMetadata(new CertificationMetadata(name, envCode));
        CertificationSpec spec = new CertificationSpec(type);
        // 如果是上传类型的，将证书放进去
        if (type.equals(CertificationType.UPLOAD.getType()) || type.equals(CertificationType.CHOOSE.getType())) {
            CertificationExistCert existCert = new CertificationExistCert(keyContent, certContent);
            spec.setExistCert(existCert);
        }
        spec.setDnsNames(new ArrayList<>(domains));
        spec.setSecretName(name);
        c7nCertification.setSpec(spec);
        return c7nCertification;
    }

    private void operateEnvGitLabFile(String certName, DevopsEnvironmentDTO devopsEnvironmentDTO, C7nCertification c7nCertification, String operateType, Long envId, Long certificationId, String filePath) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentDTO, userAttrDTO);
        clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO, devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), EnvironmentType.USER.getValue(), devopsEnvironmentDTO.getClusterCode());

        ResourceConvertToYamlHandler<C7nCertification> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(c7nCertification);
        resourceConvertToYamlHandler.operationEnvGitlabFile(CERT_PREFIX + certName, TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), operateType, userAttrDTO.getGitlabUserId(), certificationId, "Certificate", null, false, envId, filePath);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long projectId, Long certId) {
        CertificationDTO certificationDTO = baseQueryById(certId);

        if (certificationDTO == null) {
            return;
        }

        Long certEnvId = certificationDTO.getEnvId();
        DevopsEnvironmentDTO devopsEnvironmentDTO = permissionHelper.checkEnvBelongToProject(projectId, certEnvId);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);

        Integer gitLabEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId());
        String certificateType = ObjectType.CERTIFICATE.getType();
        String certName = certificationDTO.getName();
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService.baseQueryByEnvIdAndResourceId(certEnvId, certId, certificateType);

        if (devopsEnvFileResourceDTO == null) {
            baseDeleteById(certId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER, CERT_PREFIX + certificationDTO.getName() + ".yaml")) {
                gitlabServiceClientOperator.deleteFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), CERT_PREFIX + certificationDTO.getName() + ".yaml", String.format("【DELETE】%s", CERT_PREFIX + certificationDTO.getName() + ".yaml"), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "master");
            }
            return;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER, devopsEnvFileResourceDTO.getFilePath())) {
                baseDeleteById(certId);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return;
            }
        }
        certificationDTO.setCommandId(createCertCommand(CommandType.DELETE.getType(), certId, null));
        baseUpdateCommandId(certificationDTO);
        certificationDTO.setStatus(CertificationStatus.DELETING.getStatus());
        updateStatus(certificationDTO);

        if (devopsEnvFileResourceDTO.getFilePath() != null && devopsEnvFileResourceService.baseQueryByEnvIdAndPath(certEnvId, devopsEnvFileResourceDTO.getFilePath()).size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER, devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(gitLabEnvProjectId, devopsEnvFileResourceDTO.getFilePath(), String.format("【DELETE】%s", devopsEnvFileResourceDTO.getFilePath()), TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), "master");
            }
        } else {
            ResourceConvertToYamlHandler<C7nCertification> certificationOperation = new ResourceConvertToYamlHandler<>();
            // 这里的apiVersion没有作用，可以任意填
            C7nCertification c7nCertification = new C7nCertification(C7nCertification.API_VERSION_V1ALPHA1);
            CertificationMetadata certificationMetadata = new CertificationMetadata();
            certificationMetadata.setName(certName);
            c7nCertification.setMetadata(certificationMetadata);
            certificationOperation.setType(c7nCertification);
            certificationOperation.operationEnvGitlabFile(null, gitLabEnvProjectId, "delete", userAttrDTO.getGitlabUserId(), certId, certificateType, null, false, certEnvId, clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO, devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), devopsEnvironmentDTO.getType(), devopsEnvironmentDTO.getClusterCode()));
        }
        //删除证书资源发送webhook
        sendNotificationService.sendWhenCertSuccessOrDelete(certificationDTO, SendSettingEnum.DELETE_RESOURCE.value());
    }

    @Override
    public List<ProjectCertificationVO> listProjectCertInProject(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        List<ProjectCertificationVO> projectCertificationVOS = new ArrayList<>();
        baseListByProject(projectId, projectDTO.getOrganizationId()).forEach(certificationDTO -> {
            List<String> domains = gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {
            }.getType());
            ProjectCertificationVO projectCertificationVO = new ProjectCertificationVO();
            projectCertificationVO.setName(certificationDTO.getName());
            projectCertificationVO.setId(certificationDTO.getId());
            if (!CollectionUtils.isEmpty(domains)) {
                projectCertificationVO.setDomain(domains.get(0));
            }
            projectCertificationVOS.add(projectCertificationVO);
        });
        return projectCertificationVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void certDeleteByGitOps(Long certId) {
        CertificationDTO certificationDTO = baseQueryById(certId);

        //校验环境是否连接
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(certificationDTO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentDTO.getClusterId());

        //实例相关对象数据库操作
        devopsEnvCommandService.baseListByObject(ObjectType.CERTIFICATE.getType(), certificationDTO.getId()).forEach(t -> devopsEnvCommandService.baseDelete(t.getId()));
        baseDeleteById(certId);
    }

    @Override
    public Page<CertificationVO> pageByOptions(Long projectId, Long envId, PageRequest pageable, String params) {
        Page<CertificationVO> certificationDTOPage = ConvertUtils.convertPage(basePage(null, envId, pageable, params), this::dtoToVo);
        if (certificationDTOPage.getContent().isEmpty()) {
            return new Page<>();
        }

        Set<Long> envIds = certificationDTOPage.getContent().stream().map(CertificationVO::getEnvId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, DevopsEnvironmentDTO> environmentMap = devopsEnvironmentService.baseListByIds(new ArrayList<>(envIds)).stream().collect(Collectors.toMap(DevopsEnvironmentDTO::getId, Function.identity()));

        List<Long> uploadCertificationIds = new ArrayList<>();
        List<Long> orgCertificationIds = new ArrayList<>();
        List<Long> certificationIds = new ArrayList<>();

        for (CertificationVO certificationVO : certificationDTOPage.getContent()) {
            certificationIds.add(certificationVO.getId());
            if (CertificationType.UPLOAD.getType().equals(certificationVO.getType())) {
                uploadCertificationIds.add(certificationVO.getId());
            }
            if (CertificationType.CHOOSE.getType().equals(certificationVO.getType())) {
                orgCertificationIds.add(certificationVO.getCertId());
            }
        }

        Map<Long, List<CertificationNotifyObject>> notifyObjectMap = new HashMap<>();
        if (!ObjectUtils.isEmpty(certificationIds)) {
            List<CertificationNoticeDTO> certificationNoticeDTOS = devopsCertificationNoticeService.listByCertificationIds(certificationIds);
            Set<Long> userIds = certificationNoticeDTOS.stream().filter(certificationNoticeDTO -> certificationNoticeDTO.getType().equals("user")).map(CertificationNoticeDTO::getObjectId).collect(Collectors.toSet());
            Map<Long, IamUserDTO> iamUserDTOMap = baseServiceClientOperator.listUsersByIds(new ArrayList<>(userIds)).stream().collect(Collectors.toMap(IamUserDTO::getId, Function.identity()));
            notifyObjectMap = certificationNoticeDTOS.stream().map(certificationNoticeDTO -> {
                CertificationNotifyObject notifyObject = new CertificationNotifyObject(certificationNoticeDTO.getType(), certificationNoticeDTO.getObjectId(), certificationNoticeDTO.getCertificationId());
                if (notifyObject.getType().equals("user")) {
                    if (iamUserDTOMap.get(notifyObject.getId()) != null) {
                        notifyObject.setRealName(iamUserDTOMap.get(notifyObject.getId()).getRealName());
                    }
                }
                return notifyObject;
            }).collect(Collectors.groupingBy(CertificationNotifyObject::getCertificationId));
        }

        Map<Long, CertificationFileDTO> certificationFileMap = new HashMap<>();
        Map<Long, CertificationDTO> orgCertificationDTOMap = new HashMap<>();
        if (!ObjectUtils.isEmpty(uploadCertificationIds)) {
            certificationFileMap = devopsCertificationFileMapper.listByCertificationIds(uploadCertificationIds).stream().collect(Collectors.toMap(CertificationFileDTO::getCertificationId, Function.identity()));
        }

        if (!ObjectUtils.isEmpty(orgCertificationIds)) {
            orgCertificationDTOMap = devopsCertificationMapper.listByIds(orgCertificationIds).stream().collect(Collectors.toMap(CertificationDTO::getId, Function.identity()));
        }


        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();
        Map<Long, List<CertificationNotifyObject>> finalNotifyObjectMap = notifyObjectMap;
        Map<Long, CertificationFileDTO> finalCertificationFileMap = certificationFileMap;
        Map<Long, CertificationDTO> finalOrgCertificationDTOMap = orgCertificationDTOMap;
        certificationDTOPage.getContent().stream().filter(certificationDTO -> certificationDTO.getOrganizationId() == null).forEach(certificationDTO -> {
            certificationDTO.setFullDomains(certificationDTO.getDomains());
            if (CertificationType.UPLOAD.getType().equals(certificationDTO.getType())) {
                CertificationFileDTO certificationFileDTO = finalCertificationFileMap.get(certificationDTO.getId());
                certificationDTO.setKeyValue(certificationFileDTO.getKeyFile());
                certificationDTO.setCertValue(certificationFileDTO.getCertFile());
            }

            DevopsEnvironmentDTO devopsEnvironmentDTO = environmentMap.get(certificationDTO.getEnvId());
            if (devopsEnvironmentDTO != null) {
                certificationDTO.setEnvConnected(updatedEnvList.contains(devopsEnvironmentDTO.getClusterId()));
                certificationDTO.setEnvName(devopsEnvironmentDTO.getName());
            }
            certificationDTO.setNotifyObjects(finalNotifyObjectMap.get(certificationDTO.getId()));
        });
        return certificationDTOPage;
    }

    @Override
    public List<CertificationVO> queryActiveCertificationByDomain(Long projectId, Long envId, String domain) {
        if (!DOMAIN_REGEX.matcher(domain).matches()) {
            throw new CommonException(ERROR_DEVOPS_INGRESS_DOMAIN_INVALID, domain);
        }
        String wildcardDomain = "*" + domain.substring(domain.indexOf("."));
        List<CertificationDTO> certificationDTOList = baseQueryActive(projectId, envId);
        return certificationDTOList.stream().filter(c -> {
            List<String> domains = JsonHelper.unmarshalByJackson(c.getDomains(), new TypeReference<List<String>>() {
            });
            return (CollectionUtils.isEmpty(domains)) || domains.contains(domain) || domains.contains(wildcardDomain);
        }).map(this::dtoToVo).collect(Collectors.toList());
    }

    @Override
    public Boolean checkCertNameUniqueInEnv(Long envId, String certName, Long certId) {
        return devopsCertificationMapper.checkNameUnique(envId, certName, certId);
    }

    @Override
    public CertificationVO queryByName(Long envId, String certName) {
        return dtoToVo(baseQueryByEnvAndName(envId, certName));
    }

    @Override
    public CertificationRespVO queryByCertId(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.queryDetailById(certId);
        if (certificationDTO == null) {
            return null;
        }

        if (certificationDTO.getCertificationFileId() != null) {
            CertificationFileDTO certificationFileDTO = devopsCertificationFileMapper.queryByCertificationId(certificationDTO.getId());
            if (certificationFileDTO != null) {
                certificationDTO.setKeyValue(certificationFileDTO.getKeyFile());
                certificationDTO.setCertValue(certificationFileDTO.getCertFile());
            }
        }

        CertificationRespVO respVO = new CertificationRespVO();
        BeanUtils.copyProperties(certificationDTO, respVO);
        List<String> domains = gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {
        }.getType());
        respVO.setFullDomains(new ArrayList<>(domains));
        respVO.setDomains(domains);
        respVO.setCommonName(Strings.join(",", domains.iterator()));
        respVO.setIngresses(listIngressNamesByCertId(certId));
        respVO.setCertId(certificationDTO.getOrgCertId());
        respVO.setCertName(respVO.getName());

        List<CertificationNoticeDTO> certificationNoticeDTOList = devopsCertificationNoticeService.listByCertificationId(certId);
        if (!CollectionUtils.isEmpty(certificationNoticeDTOList)) {
            Set<Long> userIds = certificationNoticeDTOList.stream().filter(certificationNoticeDTO -> certificationNoticeDTO.getType().equals("user")).map(CertificationNoticeDTO::getObjectId).collect(Collectors.toSet());
            Map<Long, IamUserDTO> iamUserDTOMap = baseServiceClientOperator.listUsersByIds(new ArrayList<>(userIds)).stream().collect(Collectors.toMap(IamUserDTO::getId, Function.identity()));
            List<CertificationNotifyObject> notifyObjectList = certificationNoticeDTOList.stream().map(certificationNoticeDTO -> {
                CertificationNotifyObject notifyObject = new CertificationNotifyObject(certificationNoticeDTO.getType(), certificationNoticeDTO.getObjectId(), certificationNoticeDTO.getCertificationId());
                if (notifyObject.getType().equals("user")) {
                    if (iamUserDTOMap.get(notifyObject.getId()) != null) {
                        notifyObject.setRealName(iamUserDTOMap.get(notifyObject.getId()).getRealName());
                    }
                }
                return notifyObject;
            }).collect(Collectors.toList());
            respVO.setNotifyObjects(notifyObjectList);
        }

        if (certificationDTO.getCreatedBy() != null && certificationDTO.getCreatedBy() != 0) {
            respVO.setCreatorName(ResourceCreatorInfoUtil.getOperatorName(baseServiceClientOperator, certificationDTO.getCreatedBy()));
        }
        return respVO;
    }


    private List<String> listIngressNamesByCertId(Long certId) {
        DevopsIngressDTO devopsIngressDTO = new DevopsIngressDTO();
        devopsIngressDTO.setCertId(certId);
        return devopsIngressMapper.select(devopsIngressDTO).stream().map(DevopsIngressDTO::getName).collect(Collectors.toList());
    }


    private CertificationVO dtoToVo(CertificationDTO certificationDTO) {
        if (certificationDTO == null) {
            return null;
        }

        CertificationVO certificationVO = new CertificationVO();
        BeanUtils.copyProperties(certificationDTO, certificationVO);
        certificationVO.setCertName(certificationDTO.getName());
        certificationVO.setDomains(gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {
        }.getType()));
        if (!CollectionUtils.isEmpty(certificationVO.getDomains())) {
            certificationVO.setCommonName(certificationVO.getDomains().get(0));
        }
        certificationVO.setCertId(certificationDTO.getOrgCertId());
        return certificationVO;
    }


    @Override
    public Long createCertCommand(String type, Long certId, Long userId) {
        DevopsEnvCommandDTO devopsEnvCommandDTO = new DevopsEnvCommandDTO();
        devopsEnvCommandDTO.setCommandType(type);
        devopsEnvCommandDTO.setCreatedBy(userId);
        devopsEnvCommandDTO.setObject(ObjectType.CERTIFICATE.getType());
        devopsEnvCommandDTO.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandDTO.setObjectId(certId);
        return devopsEnvCommandService.baseCreate(devopsEnvCommandDTO).getId();
    }


    @Override
    public CertificationDTO baseQueryByEnvAndName(Long envId, String name) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setEnvId(envId);
        certificationDTO.setName(name);
        return devopsCertificationMapper.selectOne(certificationDTO);
    }

    @Override
    public CertificationDTO baseCreate(CertificationDTO certificationDTO) {
        return MapperUtil.resultJudgedInsert(devopsCertificationMapper, certificationDTO, DEVOPS_CERTIFICATION_CREATE);
    }

    @Override
    public void storeNotifyInfo(CertificationDTO certificationDTO) {
        devopsCertificationNoticeService.batchCreate(certificationDTO.getId(), certificationDTO.getNotifyObjects());
    }

    @Override
    public void updateNotifyInfo(CertificationDTO certificationDTO) {
        if (certificationDTO.getAdvanceDays() == null) {
            devopsCertificationMapper.updateAdvanceDaysToNull(certificationDTO.getId());
        }
        devopsCertificationNoticeService.batchUpdate(certificationDTO.getId(), certificationDTO.getNotifyObjects());
    }

    @Override
    public CertificationDTO baseUpdate(CertificationDTO certificationDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCertificationMapper, certificationDTO, DEVOPS_CERTIFICATION_UPDATE);
        return certificationDTO;
    }

    @Override
    public CertificationDTO baseQueryById(Long certId) {
        return devopsCertificationMapper.selectByPrimaryKey(certId);
    }

    @Override
    public Page<CertificationDTO> basePage(Long projectId, Long envId, PageRequest pageable, String params) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);

        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        Page<CertificationDTO> certificationDTOPage = PageHelper.doPageAndSort(PageRequestUtil.getMappedPage(pageable, orderByFieldMap), () -> devopsCertificationMapper.listCertificationByOptions(projectId, envId, searchParamMap, TypeUtil.cast(maps.get(TypeUtil.PARAMS))));

        // check if cert is overdue
        certificationDTOPage.getContent().forEach(dto -> {
            if (CertificationStatus.ACTIVE.getStatus().equals(dto.getStatus())) {
                if (!checkValidity(new Date(), dto.getValidFrom(), dto.getValidUntil())) {
                    dto.setStatus(CertificationStatus.OVERDUE.getStatus());
                    CertificationDTO certificationDTO = new CertificationDTO();
                    certificationDTO.setId(dto.getId());
                    certificationDTO.setStatus(CertificationStatus.OVERDUE.getStatus());
                    certificationDTO.setObjectVersionNumber(dto.getObjectVersionNumber());
                    devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
                }
            }
        });

        return certificationDTOPage;
    }

    @Override
    public List<CertificationDTO> baseQueryActiveByDomain(Long projectId, Long clusterId, String domain) {
        return devopsCertificationMapper.queryActiveByDomain(projectId, clusterId, domain);
    }

    @Override
    public List<CertificationDTO> baseQueryActive(Long projectId, Long envId) {
        return devopsCertificationMapper.queryActive(projectId, envId);
    }

    @Override
    public void updateStatus(CertificationDTO inputCertificationDTO) {
        devopsCertificationMapper.updateStatus(inputCertificationDTO.getId(), inputCertificationDTO.getStatus());
    }

    @Override
    public int updateStatusIfOperating(Long certId, CertificationStatus certificationStatus) {
        return devopsCertificationMapper.updateStatusIfOperating(Objects.requireNonNull(certId), certificationStatus.getStatus());
    }

    @Override
    public void findAndSendCertificationExpireNotice() {
        // 查询开启到期前通知的证书
        List<CertificationDTO> certificationDTOS = devopsCertificationMapper.listExpireCertificate();
        Set<Long> roleIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(new Date());
        certificationDTOS.stream().filter(c -> {
            if (Boolean.TRUE.equals(c.getNoticeSendFlag())) {
                return false;
            }
            Date validUntil = c.getValidUntil();
            if (validUntil == null || c.getAdvanceDays() == null) {
                return false;
            }
            Calendar validUtilCalendar = Calendar.getInstance();
            validUtilCalendar.setTime(validUntil);
            validUtilCalendar.add(Calendar.DATE, -c.getAdvanceDays());
            if (nowCalendar.after(validUtilCalendar)) {
                LOGGER.info("Certification:{} will expire.Expire Date:{}", c.getName(), validUntil.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DATE_TIME_FORMATTER));
                return true;
            }
            return false;
        }).forEach(certificationDTO -> {
            for (CertificationNotifyObject notifyObject : certificationDTO.getNotifyObjects()) {
                if (notifyObject.getType().equals("user")) {
                    userIds.add(notifyObject.getId());
                } else {
                    roleIds.add(notifyObject.getId());
                }
            }
            ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(certificationDTO.getProjectId());
            StringMapBuilder params = StringMapBuilder.newBuilder();
            params.put("envId", certificationDTO.getEnvId());
            params.put("projectId", projectDTO.getId());
            params.put("organizationId", projectDTO.getOrganizationId());
            params.put("projectName", projectDTO.getName());
            params.put("certId", certificationDTO.getId());
            params.put("certName", certificationDTO.getName());
            params.put("searchName", certificationDTO.getName());
            params.put("searchId", certificationDTO.getId());
            params.put("envName", certificationDTO.getEnvName());
            params.put("date", certificationDTO.getValidUntil().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DATE_TIME_FORMATTER));
            params.put("link", String.format(ENV_AND_CERTIFICATION_LINK, frontUrl, projectDTO.getId(), projectDTO.getName(), projectDTO.getOrganizationId(), certificationDTO.getName(), certificationDTO.getId()));


            List<IamUserDTO> iamUserDTOS = new ArrayList<>();
            iamUserDTOS.addAll(baseServiceClientOperator.listUsersByIds(new ArrayList<>(userIds)));
            iamUserDTOS.addAll(baseServiceClientOperator.listUsersUnderRoleByIds(certificationDTO.getProjectId(), Strings.join(",", roleIds.iterator())));

            List<Receiver> receivers = new ArrayList<>();

            iamUserDTOS.forEach(user -> {
                Receiver receiver = new Receiver();
                receiver.setEmail(user.getEmail());
                receiver.setUserId(user.getId());
                receiver.setTargetUserTenantId(user.getOrganizationId());
                receiver.setPhone(user.getPhone());
                receivers.add(receiver);
            });

            try {
                sendNotificationService.sendCertificationExpireNotice(receivers, params.build(), certificationDTO.getProjectId());
                certificationDTO.setNoticeSendFlag(true);
                MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCertificationMapper, certificationDTO, "error.devops.certification.expire.notice.flag.update");
            } catch (Exception e) {
                throw new CommonException("devops.certification.expire.notice.send");
            }
        });

    }

    @Override
    public int queryCountWithNullType() {
        return devopsCertificationMapper.queryCountWithNullType();
    }

    @Override
    public List<CertificationDTO> listWithNullType() {
        return devopsCertificationMapper.listWithNullType();
    }

    @Override
    public void baseUpdateCommandId(CertificationDTO certificationDTO) {
        CertificationDTO certificationDTOInDb = devopsCertificationMapper.selectByPrimaryKey(certificationDTO.getId());
        certificationDTOInDb.setCommandId(certificationDTO.getCommandId());
        certificationDTOInDb.setCertificationFileId(certificationDTO.getCertificationFileId());
        certificationDTOInDb.setType(certificationDTO.getType());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTOInDb);
    }

    /**
     * check weather cert is active on date
     *
     * @param date       checkDate
     * @param validFrom  valid date from
     * @param validUntil valid date until
     * @return true if cert is active, else false
     */
    public Boolean checkValidity(Date date, Date validFrom, Date validUntil) {
        return validFrom != null && validUntil != null && date.after(validFrom) && date.before(validUntil);
    }

    @Override
    public void baseUpdateValidField(CertificationDTO inputCertificationDTO) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(inputCertificationDTO.getId());
        if (checkValidity(new Date(), inputCertificationDTO.getValidFrom(), inputCertificationDTO.getValidUntil())) {
            certificationDTO.setStatus(CertificationStatus.ACTIVE.getStatus());
        } else {
            certificationDTO.setStatus(CertificationStatus.OVERDUE.getStatus());
        }
        certificationDTO.setValid(inputCertificationDTO.getValidFrom(), inputCertificationDTO.getValidUntil());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
    }

    @Override
    public void baseUpdateCertFileId(CertificationDTO inputCertificationDTO) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(inputCertificationDTO.getId());
        certificationDTO.setCertificationFileId(inputCertificationDTO.getCertificationFileId());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
    }

    @Override
    public void baseClearValidField(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certId);
        if (certificationDTO != null && (certificationDTO.getValidFrom() != null || certificationDTO.getValidUntil() != null)) {
            certificationDTO.setValid(null, null);
            devopsCertificationMapper.updateByPrimaryKey(certificationDTO);
        }
    }

    @Override
    public void baseDeleteById(Long id) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(id);
        if (certificationDTO.getOrgCertId() == null) {
            deleteCertFile(id);
        }
        devopsCertificationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Long baseStoreCertFile(CertificationFileDTO certificationFileDTO) {
        return MapperUtil.resultJudgedInsert(devopsCertificationFileMapper, certificationFileDTO, DEVOPS_INSERT_CERTIFICATION_FILE).getId();
    }

    @Override
    public void baseUpdateCertFile(CertificationFileDTO certificationFileDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsCertificationFileMapper, certificationFileDTO, DEVOPS_UPDATE_CERTIFICATION_FILE);
    }

    @Override
    public CertificationFileDTO baseQueryCertFile(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certId);
        return devopsCertificationFileMapper.selectByPrimaryKey(certificationDTO.getCertificationFileId());
    }

    @Override
    public List<CertificationDTO> baseListByEnvId(Long envId) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setEnvId(envId);
        return devopsCertificationMapper.select(certificationDTO);
    }

    @Override
    public void baseUpdateSkipProjectPermission(CertificationDTO certificationDTO) {
        devopsCertificationMapper.updateSkipCheckPro(certificationDTO.getId(), certificationDTO.getSkipCheckProjectPermission());
    }

    @Override
    public CertificationDTO baseQueryByProjectAndName(Long projectId, String name) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setName(name);
        certificationDTO.setProjectId(projectId);
        return devopsCertificationMapper.selectOne(certificationDTO);
    }

    @Override
    public List<CertificationDTO> baseListByOrgCertId(Long orgCertId) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setOrgCertId(orgCertId);
        return devopsCertificationMapper.select(certificationDTO);
    }

    @Override
    public List<CertificationDTO> baseListByProject(Long projectId, Long organizationId) {
        return devopsCertificationMapper.listByProjectId(projectId, organizationId);
    }

    private void deleteCertFile(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certId);
        if (devopsCertificationFileMapper.selectByPrimaryKey(certificationDTO.getCertificationFileId()) != null) {
            devopsCertificationFileMapper.deleteByPrimaryKey(certificationDTO.getCertificationFileId());
        }
    }

    private C7nCertificationVO processEncryptCertification(C7nCertificationCreateOrUpdateVO c7NCertificationCreateOrUpdateVO) {
        // TODO hzero 主键加密组件修复后删除
        C7nCertificationVO certificationVO = ConvertUtils.convertObject(c7NCertificationCreateOrUpdateVO, C7nCertificationVO.class);
        certificationVO.setCertId(KeyDecryptHelper.decryptValue(c7NCertificationCreateOrUpdateVO.getCertId()));
        certificationVO.setEnvId(KeyDecryptHelper.decryptValue(c7NCertificationCreateOrUpdateVO.getEnvId()));
        certificationVO.setId(KeyDecryptHelper.decryptValue(c7NCertificationCreateOrUpdateVO.getId()));
        return certificationVO;
    }

    private boolean checkNeedToUpdateGitOpsAndSetStatus(C7nCertificationVO certificationVO, String operateType) {
        certificationVO.setStatus(CertificationStatus.OPERATING.getStatus());
        if (CommandType.CREATE.getType().equals(operateType)) {
            return true;
        } else {
            CertificationDTO oldCertification = devopsCertificationMapper.selectByPrimaryKey(certificationVO.getId());
            switch (CertificationType.valueOf(certificationVO.getType().toUpperCase())) {
                case REQUEST:
                    if (!org.apache.commons.collections.CollectionUtils.isEqualCollection(JsonHelper.unmarshalByJackson(oldCertification.getDomains(), new TypeReference<List<String>>() {
                    }), certificationVO.getDomains())) {
                        return true;
                    } else {
                        certificationVO.setStatus(oldCertification.getStatus());
                        return false;
                    }
                case UPLOAD:
                    CertificationFileDTO certificationFileDTO = devopsCertificationFileMapper.queryByCertificationId(certificationVO.getId());
                    if (!org.apache.commons.collections.CollectionUtils.isEqualCollection(JsonHelper.unmarshalByJackson(oldCertification.getDomains(), new TypeReference<List<String>>() {
                    }), certificationVO.getDomains()) || !certificationFileDTO.getKeyFile().equals(certificationVO.getKeyValue()) || !certificationFileDTO.getCertFile().equals(certificationVO.getCertValue())) {
                        return true;
                    } else {
                        certificationVO.setStatus(oldCertification.getStatus());
                        return false;
                    }
                case CHOOSE:
                    CertificationFileDTO oldCertificationFileDTO = devopsCertificationFileMapper.queryByCertificationId(certificationVO.getId());
                    CertificationFileDTO orgCertificationFileDTO = devopsCertificationFileMapper.queryByCertificationId(certificationVO.getCertId());
                    if (!org.apache.commons.collections.CollectionUtils.isEqualCollection(JsonHelper.unmarshalByJackson(oldCertification.getDomains(), new TypeReference<List<String>>() {
                    }), certificationVO.getDomains()) || !oldCertificationFileDTO.getKeyFile().equals(orgCertificationFileDTO.getKeyFile()) || !oldCertificationFileDTO.getCertFile().equals(orgCertificationFileDTO.getCertFile())) {
                        return true;
                    } else {
                        certificationVO.setStatus(oldCertification.getStatus());
                        return false;
                    }
                default:
                    throw new CommonException(ExceptionConstants.CertificationExceptionCode.ERROR_DEVOPS_CERTIFICATION_TYPE_UNKNOWN);
            }
        }
    }
}
