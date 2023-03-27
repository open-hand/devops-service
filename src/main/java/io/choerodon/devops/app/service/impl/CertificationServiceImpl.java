package io.choerodon.devops.app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsCertificationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.kubernetes.C7nCertification;
import io.choerodon.devops.api.vo.kubernetes.certification.*;
import io.choerodon.devops.app.eventhandler.constants.CertManagerConstants;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.*;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static io.choerodon.devops.app.eventhandler.constants.CertManagerConstants.NEW_V1_CERT_MANAGER_CHART_VERSION;
import static io.choerodon.devops.infra.constant.ExceptionConstants.CertificationExceptionCode.DEVOPS_CERTIFICATION_OPERATE_TYPE_NULL;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:47
 * Description:
 */
@Service
public class CertificationServiceImpl implements CertificationService {

    private static final String UPLOAD = "upload";
    private static final String CERT_PREFIX = "cert-";
    private static final String MASTER = "master";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String DEVOPS_CERT_MANAGER_NOT_INSTALLED = "devops.cert.manager.not.installed";
    private static final String DEVOPS_CERTIFICATION_CREATE = "devops.certification.create";

    private static final String DEVOPS_CERTIFICATION_UPDATE = "devops.certification.update";
    private static final String DEVOPS_INSERT_CERTIFICATION_FILE = "devops.insert.certification.file";

    private static final String DEVOPS_UPDATE_CERTIFICATION_FILE = "devops.update.certification.file";


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
    public void createOrUpdateCertification(Long projectId, C7nCertificationCreateOrUpdateVO c7NCertificationCreateOrUpdateVO,
                                            MultipartFile key, MultipartFile cert) {
        if (ObjectUtils.isEmpty(c7NCertificationCreateOrUpdateVO.getOperateType())) {
            throw new CommonException(DEVOPS_CERTIFICATION_OPERATE_TYPE_NULL);
        }
        if (!ObjectUtils.isEmpty(c7NCertificationCreateOrUpdateVO.getNotifyObjectsJsonStr())) {
            c7NCertificationCreateOrUpdateVO.setNotifyObjects(JsonHelper.unmarshalByJackson(c7NCertificationCreateOrUpdateVO.getNotifyObjectsJsonStr(), new TypeReference<List<C7nCertificationCreateOrUpdateVO.NotifyObject>>() {
            }));
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

        String certFileName;
        String keyFileName;

        //如果是选择上传文件方式
        if (certificationVO.getType().equals(UPLOAD)) {
            if (key != null && cert != null) {
                certificationVO.setKeyValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, key))));
                certificationVO.setCertValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, cert))));
            } else {
                certFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "crt");
                keyFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "key");
                FileUtil.saveDataToFile(path, certFileName, certificationVO.getCertValue());
                FileUtil.saveDataToFile(path, keyFileName, certificationVO.getKeyValue());
            }
            // 因为放开了证书格式限制，所以不同格式有不同的校验逻辑。但是证书内容都是文本，无法正确判断当前上传的证书是什么格式，所以将证书校验这块交给用户自己来控制
//        File certPath = new File(path + FILE_SEPARATOR + certFileName);
//        File keyPath = new File(path + FILE_SEPARATOR + keyFileName);
//        try {
//            SslUtil.validate(certPath, keyPath);
//        } catch (Exception e) {
//            FileUtil.deleteFile(certPath);
//            FileUtil.deleteFile(keyPath);
//            throw new CommonException(e.getMessage());
//        }
//
//        FileUtil.deleteFile(certPath);
//        FileUtil.deleteFile(keyPath);
        }

        String certName = certificationVO.getCertName();
        String type = certificationVO.getType();
        List<String> domains = certificationVO.getDomains();


        CertificationFileDTO certificationFileDTO = null;
        //如果创建的时候选择证书
        if (certificationVO.getCertId() != null) {
            CommonExAssertUtil.assertTrue(permissionHelper.projectPermittedToCert(certificationVO.getCertId(), projectId), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

            certificationVO.setType(UPLOAD);
            type = certificationVO.getType();
            certificationFileDTO = baseQueryCertFile(baseQueryById(certificationVO.getCertId()).getId());
        }

        devopsCertificationValidator.checkCertification(envId, certName);


        // status operating
        CertificationDTO newCertificationDTO = new CertificationDTO(null,
                certName, devopsEnvironmentDTO.getId(), gson.toJson(domains), CertificationStatus.OPERATING.getStatus(), certificationVO.getCertId());

        String keyContent;
        String certContent;
        if (certificationFileDTO == null) {
            keyContent = certificationVO.getKeyValue();
            certContent = certificationVO.getCertValue();
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
            updateAndStore(newCertificationDTO, keyContent, certContent);
        }

        // 将资源对象生成yaml提交到gitlab
        handleCertificationToGitlab(certManagerVersion, certName, type, domains, keyContent, certContent, devopsEnvironmentDTO, c7NCertificationCreateOrUpdateVO.getOperateType());
    }

    private void handleCertificationToGitlab(String certManagerVersion, String certName, String createType, List<String> domains, String keyContent, String certContent, DevopsEnvironmentDTO devopsEnvironmentDTO, String operateType) {
        if (CertManagerConstants.OLD_V1_CERT_MANAGER_CHART_VERSION.equals(certManagerVersion)) {
            handleAlpha1Certification(certName, createType, domains, keyContent, certContent, devopsEnvironmentDTO, operateType);
        } else {
            handleV1Certification(certName, createType, domains, keyContent, certContent, devopsEnvironmentDTO, operateType);
        }
    }

    private void handleAlpha1Certification(String certName, String createType, List<String> domains, String keyContent, String certContent, DevopsEnvironmentDTO devopsEnvironmentDTO, String operateType) {
        C7nCertification c7nCertification = getV1Alpha1C7nCertification(certName, createType, domains, keyContent, certContent, devopsEnvironmentDTO.getCode());
        // sent certification to agent
        operateEnvGitLabFile(certName, devopsEnvironmentDTO, c7nCertification, operateType);
    }

    private void handleV1Certification(String certName, String createType, List<String> domains, String keyContent, String certContent, DevopsEnvironmentDTO devopsEnvironmentDTO, String operateType) {
        C7nCertification c7nCertification = getV1C7nCertification(certName, createType, domains, keyContent, certContent, devopsEnvironmentDTO.getCode());
        operateEnvGitLabFile(certName, devopsEnvironmentDTO, c7nCertification, operateType);
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
    }

    /**
     * update certification, command and store cert file
     *
     * @param certificationDTO the information of certification
     */
    private void updateAndStore(CertificationDTO certificationDTO, @Nullable String keyContent, @Nullable String certContent) {
        // create
        certificationDTO = baseUpdate(certificationDTO);
        Long certId = certificationDTO.getId();

        CertificationDTO updateCertificationDTO = new CertificationDTO();
        updateCertificationDTO.setId(certificationDTO.getId());
        updateCertificationDTO.setCommandId(createCertCommand(CommandType.UPDATE.getType(), certId, null));
        // cert command
        baseUpdateCommandId(updateCertificationDTO);
        // store crt & key if type is upload
        updateCertFile(keyContent, certContent, certificationDTO);
    }


    private void storeCertFile(String keyContent, String certContent, Long certId) {
        if (keyContent != null && certContent != null) {
            CertificationDTO certificationDTO = new CertificationDTO();
            certificationDTO.setCertificationFileId(baseStoreCertFile(
                    new CertificationFileDTO(certContent, keyContent)));
            certificationDTO.setId(certId);
            baseUpdateCertFileId(certificationDTO);
        }
    }

    private void updateCertFile(String keyContent, String certContent, CertificationDTO certificationDTO) {
        if (keyContent != null && certContent != null) {
            CertificationFileDTO certificationFileDTO = devopsCertificationFileMapper.selectByPrimaryKey(certificationDTO.getCertificationFileId());
            certificationFileDTO.setKeyFile(keyContent);
            certificationFileDTO.setCertFile(certContent);
            baseUpdateCertFile(certificationFileDTO);
        }
    }

    @Override
    public C7nCertification getV1Alpha1C7nCertification(String name, String type, List<String> domains,
                                                        String keyContent, String certContent, String envCode) {
        C7nCertification c7nCertification = new C7nCertification(C7nCertification.API_VERSION_V1ALPHA1);

        c7nCertification.setMetadata(new CertificationMetadata(name,
                envCode));
        CertificationSpec spec = new CertificationSpec(type);
        if (type.equals(CertificationType.REQUEST.getType())) {
            CertificationAcme acme = new CertificationAcme();
            acme.initConfig(new CertificationConfig(domains));
            spec.setAcme(acme);
        } else if (type.equals(CertificationType.UPLOAD.getType())) {
            CertificationExistCert existCert = new CertificationExistCert(keyContent, certContent);
            spec.setExistCert(existCert);
        }
        spec.setCommonName(domains.get(0));
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
        if (type.equals(CertificationType.UPLOAD.getType())) {
            CertificationExistCert existCert = new CertificationExistCert(keyContent, certContent);
            spec.setExistCert(existCert);
        }
        spec.setDnsNames(new ArrayList<>(domains));
        spec.setSecretName(name);
        c7nCertification.setSpec(spec);
        return c7nCertification;
    }

    private void operateEnvGitLabFile(String certName,
                                      DevopsEnvironmentDTO devopsEnvironmentDTO,
                                      C7nCertification c7nCertification, String operateType) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentDTO, userAttrDTO);
        clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getId(), devopsEnvironmentDTO.getEnvIdRsa(), EnvironmentType.USER.getValue(), devopsEnvironmentDTO.getClusterCode());

        ResourceConvertToYamlHandler<C7nCertification> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(c7nCertification);
        resourceConvertToYamlHandler.operationEnvGitlabFile(CERT_PREFIX + certName,
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), operateType,
                userAttrDTO.getGitlabUserId(), null, null, null, false, null, null);
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
        DevopsEnvFileResourceDTO devopsEnvFileResourceDTO = devopsEnvFileResourceService
                .baseQueryByEnvIdAndResourceId(certEnvId, certId, certificateType);

        if (devopsEnvFileResourceDTO == null) {
            baseDeleteById(certId);
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    CERT_PREFIX + certificationDTO.getName() + ".yaml")) {
                gitlabServiceClientOperator.deleteFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        CERT_PREFIX + certificationDTO.getName() + ".yaml",
                        String.format("【DELETE】%s", CERT_PREFIX + certificationDTO.getName() + ".yaml"),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()),
                        "master");
            }
            return;
        } else {
            if (!gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                baseDeleteById(certId);
                devopsEnvFileResourceService.baseDeleteById(devopsEnvFileResourceDTO.getId());
                return;
            }
        }
        certificationDTO.setCommandId(createCertCommand(CommandType.DELETE.getType(), certId, null));
        baseUpdateCommandId(certificationDTO);
        certificationDTO.setStatus(CertificationStatus.DELETING.getStatus());
        updateStatus(certificationDTO);

        if (devopsEnvFileResourceDTO.getFilePath() != null
                && devopsEnvFileResourceService
                .baseQueryByEnvIdAndPath(certEnvId, devopsEnvFileResourceDTO.getFilePath()).size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
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
            certificationOperation.operationEnvGitlabFile(
                    null, gitLabEnvProjectId,
                    "delete", userAttrDTO.getGitlabUserId(), certId, certificateType, null, false, certEnvId,
                    clusterConnectionHandler.handDevopsEnvGitRepository(
                            devopsEnvironmentDTO.getProjectId(),
                            devopsEnvironmentDTO.getCode(),
                            devopsEnvironmentDTO.getId(),
                            devopsEnvironmentDTO.getEnvIdRsa(),
                            devopsEnvironmentDTO.getType(),
                            devopsEnvironmentDTO.getClusterCode()));
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
            projectCertificationVO.setDomain(domains.get(0));
            projectCertificationVO.setId(certificationDTO.getId());
            projectCertificationVO.setDomain(domains.get(0));
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
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedClusterList();
        certificationDTOPage.getContent().stream()
                .filter(certificationDTO -> certificationDTO.getOrganizationId() == null)
                .forEach(certificationDTO -> {
                    DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(certificationDTO.getEnvId());
                    certificationDTO.setEnvConnected(updatedEnvList.contains(devopsEnvironmentDTO.getClusterId()));
                });
        return certificationDTOPage;
    }

    @Override
    public List<CertificationVO> queryActiveCertificationByDomain(Long projectId, Long envId, String domain) {
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);
        return ConvertUtils.convertList(baseQueryActiveByDomain(projectId, devopsEnvironmentDTO.getClusterId(), domain), this::dtoToVo);
    }

    @Override
    public Boolean checkCertNameUniqueInEnv(Long envId, String certName) {
        return baseCheckCertNameUniqueInEnv(envId, certName);
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

        CertificationRespVO respVO = new CertificationRespVO();
        BeanUtils.copyProperties(certificationDTO, respVO);
        List<String> domains = gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {
        }.getType());
        respVO.setCommonName(domains.isEmpty() ? null : domains.remove(0));
        respVO.setDNSNames(domains);
        respVO.setIngresses(listIngressNamesByCertId(certId));
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
        certificationVO.setCommonName(certificationVO.getDomains().get(0));
        if (certificationDTO.getEnvId() != null) {
            Optional.ofNullable(devopsEnvironmentService.baseQueryById(certificationDTO.getEnvId())).ifPresent(
                    dto -> certificationVO.setEnvName(dto.getName())
            );
        }
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
    public void updateStatus(CertificationDTO inputCertificationDTO) {
        devopsCertificationMapper.updateStatus(inputCertificationDTO.getId(), inputCertificationDTO.getStatus());
    }

    @Override
    public int updateStatusIfOperating(Long certId, CertificationStatus certificationStatus) {
        return devopsCertificationMapper.updateStatusIfOperating(Objects.requireNonNull(certId), certificationStatus.getStatus());
    }

    @Override
    public void baseUpdateCommandId(CertificationDTO certificationDTO) {
        CertificationDTO certificationDTOInDb = devopsCertificationMapper.selectByPrimaryKey(certificationDTO.getId());
        certificationDTOInDb.setCommandId(certificationDTO.getCommandId());
        certificationDTOInDb.setCertificationFileId(certificationDTO.getCertificationFileId());
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
        return validFrom != null && validUntil != null
                && date.after(validFrom) && date.before(validUntil);
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
        if (certificationDTO != null
                && (certificationDTO.getValidFrom() != null || certificationDTO.getValidUntil() != null)) {
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
    public Boolean baseCheckCertNameUniqueInEnv(Long envId, String certName) {
        return devopsCertificationMapper.select(new CertificationDTO(certName, envId)).isEmpty();
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
}
