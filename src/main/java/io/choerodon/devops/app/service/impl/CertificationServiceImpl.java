package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsCertificationValidator;
import io.choerodon.devops.api.vo.C7nCertificationVO;
import io.choerodon.devops.api.vo.CertificationRespVO;
import io.choerodon.devops.api.vo.CertificationVO;
import io.choerodon.devops.api.vo.ProjectCertificationVO;
import io.choerodon.devops.api.vo.kubernetes.C7nCertification;
import io.choerodon.devops.api.vo.kubernetes.certification.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.*;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.gitops.ResourceConvertToYamlHandler;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsCertificationFileMapper;
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
import io.choerodon.devops.infra.mapper.DevopsIngressMapper;
import io.choerodon.devops.infra.util.*;

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


    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
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

    private Gson gson = new Gson();


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCertification(Long projectId, C7nCertificationVO certificationDTO,
                                    MultipartFile key, MultipartFile cert, Boolean isGitOps) {

        Long envId = certificationDTO.getEnvId();

        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentService.baseQueryById(envId);

        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentDTO, userAttrDTO);


        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        String path = String.format("tmp%s%s%s%s", FILE_SEPARATOR, projectDTO.getCode(), FILE_SEPARATOR, devopsEnvironmentDTO.getCode());

        String certFileName;
        String keyFileName;

        //如果是选择上传文件方式
        if (certificationDTO.getType().equals(UPLOAD)) {
            if (key != null && cert != null) {
                certFileName = cert.getOriginalFilename();
                keyFileName = key.getOriginalFilename();
                certificationDTO.setKeyValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, key))));
                certificationDTO.setCertValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, cert))));
            } else {
                certFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "crt");
                keyFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "key");
                FileUtil.saveDataToFile(path, certFileName, certificationDTO.getCertValue());
                FileUtil.saveDataToFile(path, keyFileName, certificationDTO.getKeyValue());
            }
            File certPath = new File(path + FILE_SEPARATOR + certFileName);
            File keyPath = new File(path + FILE_SEPARATOR + keyFileName);
            try {
                SslUtil.validate(certPath, keyPath);
            } catch (CommonException e) {
                FileUtil.deleteFile(certPath);
                FileUtil.deleteFile(keyPath);
                throw e;
            }
            FileUtil.deleteFile(certPath);
            FileUtil.deleteFile(keyPath);
        }

        String certName = certificationDTO.getCertName();
        String type = certificationDTO.getType();
        List<String> domains = certificationDTO.getDomains();


        CertificationFileDTO certificationFileDTO = null;
        //如果创建的时候选择证书
        if (certificationDTO.getCertId() != null) {
            certificationDTO.setType(UPLOAD);
            certificationFileDTO = baseQueryCertFile(baseQueryById(certificationDTO.getCertId()).getId());
        }

        devopsCertificationValidator.checkCertification(envId, certName);


        // status operating
        CertificationDTO newCertificationDTO = new CertificationDTO(null,
                certName, devopsEnvironmentDTO.getId(), gson.toJson(domains), CertificationStatus.OPERATING.getStatus(), certificationDTO.getCertId());

        if (!isGitOps) {
            String envCode = devopsEnvironmentDTO.getCode();

            String keyContent;
            String certContent;
            if (certificationFileDTO == null) {
                keyContent = certificationDTO.getKeyValue();
                certContent = certificationDTO.getCertValue();
            } else {
                keyContent = certificationFileDTO.getKeyFile();
                certContent = certificationFileDTO.getCertFile();
            }

            C7nCertification c7nCertification = getC7nCertification(certName, type, domains, keyContent, certContent, envCode);

            createAndStore(newCertificationDTO, c7nCertification);

            // sent certification to agent
            operateEnvGitLabFile(certName, devopsEnvironmentDTO, c7nCertification);

        } else {
            createAndStore(newCertificationDTO, null);
        }

    }

    /**
     * create certification, command and store cert file
     *
     * @param certificationDTO the information of certification
     * @param c7nCertification the certification (null_able)
     */
    private void createAndStore(CertificationDTO certificationDTO, C7nCertification c7nCertification) {
        // create
        certificationDTO = baseCreate(certificationDTO);
        Long certId = certificationDTO.getId();

        CertificationDTO updateCertificationDTO = new CertificationDTO();
        updateCertificationDTO.setId(certificationDTO.getId());
        updateCertificationDTO.setCommandId(createCertCommand(CommandType.CREATE.getType(), certId, null));
        // cert command
        baseUpdateCommandId(updateCertificationDTO);
        // store crt & key if type is upload
        storeCertFile(c7nCertification, certId);
    }


    private void storeCertFile(C7nCertification c7nCertification, Long certId) {
        if (c7nCertification != null) {
            CertificationExistCert existCert = c7nCertification.getSpec().getExistCert();
            if (existCert != null) {
                CertificationDTO certificationDTO = new CertificationDTO();
                certificationDTO.setCertificationFileId(baseStoreCertFile(
                        new CertificationFileDTO(existCert.getCert(), existCert.getKey())));
                certificationDTO.setId(certId);
                baseUpdateCertFileId(certificationDTO);
            }
        }
    }

    @Override
    public C7nCertification getC7nCertification(String name, String type, List<String> domains,
                                                String keyContent, String certContent, String envCode) {
        C7nCertification c7nCertification = new C7nCertification();

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

    private void operateEnvGitLabFile(String certName,
                                      DevopsEnvironmentDTO devopsEnvironmentDTO,
                                      C7nCertification c7nCertification) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentDTO, userAttrDTO);
        clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa());

        ResourceConvertToYamlHandler<C7nCertification> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(c7nCertification);
        resourceConvertToYamlHandler.operationEnvGitlabFile(CERT_PREFIX + certName,
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), "create",
                userAttrDTO.getGitlabUserId(), null, null, null, false, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long certId) {
        CertificationDTO certificationDTO = baseQueryById(certId);
        Long certEnvId = certificationDTO.getEnvId();
        DevopsEnvironmentDTO devopsEnvironmentDTO = devopsEnvironmentMapper.selectByPrimaryKey(certEnvId);

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
                gitlabServiceClientOperator.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                        CERT_PREFIX + certificationDTO.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
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
        baseUpdateStatus(certificationDTO);

        if (devopsEnvFileResourceDTO.getFilePath() != null
                && devopsEnvFileResourceService
                .baseQueryByEnvIdAndPath(certEnvId, devopsEnvFileResourceDTO.getFilePath()).size() == 1) {
            if (gitlabServiceClientOperator.getFile(TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()), MASTER,
                    devopsEnvFileResourceDTO.getFilePath())) {
                gitlabServiceClientOperator.deleteFile(
                        gitLabEnvProjectId,
                        devopsEnvFileResourceDTO.getFilePath(),
                        "DELETE FILE " + certName,
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
            }
        } else {
            ResourceConvertToYamlHandler<C7nCertification> certificationOperation = new ResourceConvertToYamlHandler<>();
            C7nCertification c7nCertification = new C7nCertification();
            CertificationMetadata certificationMetadata = new CertificationMetadata();
            certificationMetadata.setName(certName);
            c7nCertification.setMetadata(certificationMetadata);
            certificationOperation.setType(c7nCertification);
            certificationOperation.operationEnvGitlabFile(
                    null, gitLabEnvProjectId,
                    "delete", userAttrDTO.getGitlabUserId(), certId, certificateType, null, false, certEnvId,
                    clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentDTO.getProjectId(), devopsEnvironmentDTO.getCode(), devopsEnvironmentDTO.getEnvIdRsa()));
        }
    }

    @Override
    public List<ProjectCertificationVO> listProjectCertInProject(Long projectId) {
        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        List<ProjectCertificationVO> projectCertificationVOS = new ArrayList<>();
        baseListByProject(projectId, projectDTO.getOrganizationId()).forEach(certificationDTO -> {
            List<String> domains = gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {}.getType());
            ProjectCertificationVO projectCertificationVO = new ProjectCertificationVO(certificationDTO.getName(),domains.get(0));
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
        devopsEnvCommandService.baseListByObject(HelmObjectKind.CERTIFICATE.toValue(), certificationDTO.getId()).forEach(t -> devopsEnvCommandService.baseDelete(t.getId()));
        baseDeleteById(certId);
    }

    @Override
    public PageInfo<CertificationVO> pageByOptions(Long projectId, Long envId, PageRequest pageRequest, String params) {
        PageInfo<CertificationVO> certificationDTOPage = ConvertUtils.convertPage(basePage(null, envId, pageRequest, params), this::dtoToVo);
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        certificationDTOPage.getList().stream()
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
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certId);
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


    public CertificationDTO voToDTO(CertificationVO certificationVO) {
        CertificationDTO certificationDTO = new CertificationDTO();
        BeanUtils.copyProperties(certificationVO, certificationDTO);
        certificationDTO.setDomains(gson.toJson(certificationVO.getDomains()));
        return certificationDTO;
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
        devopsCertificationMapper.insert(certificationDTO);
        return certificationDTO;
    }

    @Override
    public CertificationDTO baseQueryById(Long certId) {
        return devopsCertificationMapper.selectByPrimaryKey(certId);
    }

    @Override
    public PageInfo<CertificationDTO> basePage(Long projectId, Long envId, PageRequest pageRequest, String params) {
        Map<String, Object> maps = TypeUtil.castMapParams(params);

        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("envName")) {
                            property = "de.name";
                        } else if (property.equals("envCode")) {
                            property = "de.code";
                        } else if (property.equals("certName")) {
                            property = "dc.`name`";
                        } else if (property.equals("commonName")) {
                            property = "dc.domains";
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        PageInfo<CertificationDTO> certificationDTOPage = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), sortResult)
                .doSelectPageInfo(() -> devopsCertificationMapper.listCertificationByOptions(projectId, envId, searchParamMap, TypeUtil.cast(maps.get(TypeUtil.PARAMS))));

        // check if cert is overdue
        certificationDTOPage.getList().forEach(dto -> {
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
    public void baseUpdateStatus(CertificationDTO inputCertificationDTO) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(inputCertificationDTO.getId());
        certificationDTO.setStatus(inputCertificationDTO.getStatus());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
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
        devopsCertificationFileMapper.insert(certificationFileDTO);
        return certificationFileDTO.getId();
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
}
