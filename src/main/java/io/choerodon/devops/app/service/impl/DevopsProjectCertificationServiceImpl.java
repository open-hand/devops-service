package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ProjectCertificationCreateUpdateVO;
import io.choerodon.devops.api.vo.ProjectCertificationPermissionUpdateVO;
import io.choerodon.devops.api.vo.ProjectCertificationVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.app.service.DevopsCertificationProRelationshipService;
import io.choerodon.devops.app.service.DevopsProjectCertificationService;
import io.choerodon.devops.app.service.SendNotificationService;
import io.choerodon.devops.infra.constant.MiscConstants;
import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.devops.infra.dto.CertificationFileDTO;
import io.choerodon.devops.infra.dto.DevopsCertificationProRelationshipDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCertificationFileMapper;
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper;
import io.choerodon.devops.infra.util.*;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsProjectCertificationServiceImpl implements DevopsProjectCertificationService {
    private static final String CREATE = "create";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String ERROR_CERTIFICATION_NOT_EXIST = "devops.certification.not.exist";

    private final Gson gson = new Gson();
    @Autowired
    private DevopsCertificationProRelationshipService devopsCertificationProRelationshipService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private CertificationService certificationService;
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
    @Autowired
    private DevopsCertificationFileMapper devopsCertificationFileMapper;
    @Autowired
    private SendNotificationService sendNotificationService;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void assignPermission(ProjectCertificationPermissionUpdateVO update) {
        CertificationDTO certificationDTO = certificationService.baseQueryById(update.getCertificationId());
        if (certificationDTO == null) {
            throw new CommonException(ERROR_CERTIFICATION_NOT_EXIST, update.getCertificationId());
        }

        if (certificationDTO.getProjectId() == null) {
            throw new CommonException("devops.not.project.certification", update.getCertificationId());
        }

        if (certificationDTO.getSkipCheckProjectPermission()) {
            if (update.getSkipCheckProjectPermission()) {
                // 原来跳过，现在也跳过，不处理
                return;
            } else {
                // 原来跳过，现在不跳过，先更新字段，然后插入关联关系
                updateSkipPermissionCheck(
                        update.getCertificationId(),
                        update.getSkipCheckProjectPermission(),
                        update.getObjectVersionNumber());

                devopsCertificationProRelationshipService.batchInsertIgnore(
                        update.getCertificationId(),
                        update.getProjectIds());
            }
        } else {
            // 原来不跳过，现在跳过，更新证书权限字段，再删除所有数据库中与该证书有关的关联关系
            if (update.getSkipCheckProjectPermission()) {
                updateSkipPermissionCheck(
                        update.getCertificationId(),
                        update.getSkipCheckProjectPermission(),
                        update.getObjectVersionNumber());

                devopsCertificationProRelationshipService.baseDeleteByCertificationId(update.getCertificationId());
            } else {
                // 原来不跳过，现在也不跳过，批量添加权限
                devopsCertificationProRelationshipService.batchInsertIgnore(
                        update.getCertificationId(),
                        update.getProjectIds());
            }
        }
    }

    @Override
    public Page<ProjectReqVO> pageRelatedProjects(Long projectId, Long certId, PageRequest pageable, String params) {
        CertificationDTO certificationDTO = certificationService.baseQueryById(certId);
        if (certificationDTO == null) {
            throw new CommonException(ERROR_CERTIFICATION_NOT_EXIST, certId);
        }

        Map<String, String> paramMap = JsonHelper.unmarshalByJackson(params, new TypeReference<Map<String, String>>() {
        });

        String param = paramMap.get("params");

        String name = null;
        String code = null;
        if (!CollectionUtils.isEmpty(paramMap)) {
            name = TypeUtil.cast(paramMap.get("name"));
            code = TypeUtil.cast(paramMap.get("code"));
        }
        if (ObjectUtils.isEmpty(param) && ObjectUtils.isEmpty(name) && ObjectUtils.isEmpty(code)) {
            // 如果不搜索，在数据库中进行分页
            Page<DevopsCertificationProRelationshipDTO> relationPage = PageHelper.doPage(pageable, () -> devopsCertificationProRelationshipService.baseListByCertificationId(certId));
            return ConvertUtils.convertPage(relationPage, permission -> {
                ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(permission.getProjectId());
                return new ProjectReqVO(permission.getProjectId(), projectDTO.getName(), projectDTO.getCode());
            });
        } else {
            // 如果要搜索，需要手动在程序内分页
            ProjectDTO iamProjectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);

            // 手动查出所有组织下的项目
            List<ProjectDTO> filteredProjects = baseServiceClientOperator.listIamProjectByOrgId(iamProjectDTO.getOrganizationId(), name, code, param);

            // 数据库中的有权限的项目
            List<Long> permissions = devopsCertificationProRelationshipService.baseListByCertificationId(certId)
                    .stream()
                    .map(DevopsCertificationProRelationshipDTO::getProjectId)
                    .collect(Collectors.toList());

            // 过滤出在数据库中有权限的项目信息
            List<ProjectReqVO> allMatched = filteredProjects
                    .stream()
                    .filter(p -> permissions.contains(p.getId()))
                    .map(p -> ConvertUtils.convertObject(p, ProjectReqVO.class))
                    .collect(Collectors.toList());

            return PageInfoUtil.createPageFromList(allMatched, pageable);
        }
    }

    /**
     * 更新证书的权限校验字段
     *
     * @param certId              证书id
     * @param skipCheckPermission 是否跳过权限校验
     * @param objectVersionNumber 版本号
     */
    private void updateSkipPermissionCheck(Long certId, Boolean skipCheckPermission, Long objectVersionNumber) {
        CertificationDTO toUpdate = new CertificationDTO();
        toUpdate.setId(certId);
        toUpdate.setObjectVersionNumber(objectVersionNumber);
        toUpdate.setSkipCheckProjectPermission(skipCheckPermission);
        devopsCertificationMapper.updateByPrimaryKeySelective(toUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdate(Long projectId, MultipartFile key, MultipartFile cert, ProjectCertificationCreateUpdateVO createUpdateVO) {
        // 特殊情况的主键加密手动处理
        ProjectCertificationVO projectCertificationVO = ConvertUtils.convertObject(createUpdateVO, ProjectCertificationVO.class);
        projectCertificationVO.setId(KeyDecryptHelper.decryptValue(createUpdateVO.getId()));

        ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(projectDTO.getOrganizationId());
        String path = String.format("tmp%s%s%s%s", FILE_SEPARATOR, organizationDTO.getTenantNum(), FILE_SEPARATOR, GenerateUUID.generateUUID().substring(0, 5));
        String certFileName;
        String keyFileName;
        //如果是选择上传文件方式
        if (key != null && cert != null) {
            certFileName = cert.getOriginalFilename();
            keyFileName = key.getOriginalFilename();
            projectCertificationVO.setKeyValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, key))));
            projectCertificationVO.setCertValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, cert))));
        } else {
            certFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "crt");
            keyFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "key");
            FileUtil.saveDataToFile(path, certFileName, projectCertificationVO.getCertValue());
            FileUtil.saveDataToFile(path, keyFileName, projectCertificationVO.getKeyValue());
        }

        File certPath = new File(path + FILE_SEPARATOR + certFileName);
        File keyPath = new File(path + FILE_SEPARATOR + keyFileName);
        try {
            SslUtil.validate(certPath, keyPath);
            SslUtil.CertInfo certInfo = SslUtil.parseCert(certPath);
            projectCertificationVO.setDomain(CollectionUtils.isEmpty(certInfo.getDomains()) ? "[]" : gson.toJson(certInfo.getDomains()));
            projectCertificationVO.setValidFrom(certInfo.getValidFrom());
            projectCertificationVO.setValidUntil(certInfo.getValidUntil());
        } catch (Exception e) {
            FileUtil.deleteFile(certPath);
            FileUtil.deleteFile(keyPath);
            throw new CommonException(e.getMessage());
        }

        FileUtil.deleteFile(certPath);
        FileUtil.deleteFile(keyPath);

        if (projectCertificationVO.getType().equals(CREATE)) {
            CertificationDTO certificationDTO = new CertificationDTO();
            certificationDTO.setName(projectCertificationVO.getName());
            certificationDTO.setProjectId(projectId);
            // 创建项目层证书需要组织id
            certificationDTO.setOrganizationId(organizationDTO.getTenantId());
            certificationDTO.setSkipCheckProjectPermission(true);
            certificationDTO.setDomains(projectCertificationVO.getDomain());
            certificationDTO.setCertificationFileId(certificationService.baseStoreCertFile(new CertificationFileDTO(projectCertificationVO.getCertValue(), projectCertificationVO.getKeyValue())));
            certificationDTO.setValidFrom(projectCertificationVO.getValidFrom());
            certificationDTO.setValidUntil(projectCertificationVO.getValidUntil());
            certificationService.baseCreate(certificationDTO);
        } else {
            CertificationDTO certificationDTO = new CertificationDTO();
            BeanUtils.copyProperties(projectCertificationVO, certificationDTO);
            certificationDTO.setProjectId(projectId);
            certificationDTO.setDomains(projectCertificationVO.getDomain());
            certificationDTO.setValidFrom(projectCertificationVO.getValidFrom());
            certificationDTO.setValidUntil(projectCertificationVO.getValidUntil());
            devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);

            CertificationFileDTO certificationFileDTO = devopsCertificationFileMapper.queryByCertificationId(certificationDTO.getId());
            certificationFileDTO.setKeyFile(certificationDTO.getKeyValue());
            certificationFileDTO.setCertFile(certificationDTO.getCertValue());
            devopsCertificationFileMapper.updateByPrimaryKeySelective(certificationFileDTO);
        }
    }

    @Override
    public boolean isNameUnique(Long projectId, String name) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setName(name);
        certificationDTO.setProjectId(projectId);
        return devopsCertificationMapper.selectCount(certificationDTO) == 0;
    }

    @Override
    public Page<ProjectReqVO> listNonRelatedMembers(Long projectId, Long certId, Long selectedProjectId, PageRequest pageable, String params) {
        CertificationDTO certificationDTO = certificationService.baseQueryById(certId);
        if (certificationDTO == null) {
            throw new CommonException(ERROR_CERTIFICATION_NOT_EXIST, certId);
        }

        Map<String, String> searchParamMap = new HashMap<>();
        List<String> paramList = new ArrayList<>();
        if (!StringUtils.isEmpty(params)) {
            Map maps = gson.fromJson(params, Map.class);
            searchParamMap = Optional.ofNullable((Map) TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM))).orElse(new HashMap<>());
            paramList = Optional.ofNullable((List) TypeUtil.cast(maps.get(TypeUtil.PARAMS))).orElse(new ArrayList<>());
        }
        //查询出该项目所属组织下的所有项目
        ProjectDTO iamProjectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(projectId);
        Tenant organizationDTO = baseServiceClientOperator.queryOrganizationById(iamProjectDTO.getOrganizationId());
        List<ProjectDTO> projectDTOList = baseServiceClientOperator.listIamProjectByOrgId(organizationDTO.getTenantId(),
                searchParamMap.get("name"),
                searchParamMap.get("code"),
                CollectionUtils.isEmpty(paramList) ? null : paramList.get(0));

        //查询已经分配权限的项目
        List<Long> permitted = devopsCertificationProRelationshipService.baseListByCertificationId(certId)
                .stream()
                .map(DevopsCertificationProRelationshipDTO::getProjectId)
                .collect(Collectors.toList());

        //把组织下有权限的项目过滤掉再返回
        List<ProjectReqVO> nonRelatedMembers = projectDTOList.stream()
                .filter(i -> !permitted.contains(i.getId()))
                .map(i -> new ProjectReqVO(i.getId(), i.getName(), i.getCode()))
                .collect(Collectors.toList());

        if (selectedProjectId != null) {
            ProjectDTO selectedProjectDTO = baseServiceClientOperator.queryIamProjectBasicInfoById(selectedProjectId);
            ProjectReqVO projectReqVO = new ProjectReqVO(selectedProjectDTO.getId(), selectedProjectDTO.getName(), selectedProjectDTO.getCode());
            if (!nonRelatedMembers.isEmpty()) {
                nonRelatedMembers.remove(projectReqVO);
                nonRelatedMembers.add(0, projectReqVO);
            } else {
                nonRelatedMembers.add(projectReqVO);
            }
        }

        return PageInfoUtil.createPageFromList(nonRelatedMembers, pageable);
    }

    @Override
    public void deleteCert(Long projectId, Long certId) {
        CertificationDTO certificationDTO = certificationService.baseQueryById(certId);
        if (certificationDTO == null) {
            return;
        }
        CommonExAssertUtil.assertTrue(projectId.equals(certificationDTO.getProjectId()), MiscConstants.DEVOPS_OPERATING_RESOURCE_IN_OTHER_PROJECT);

        List<CertificationDTO> certificationDTOS = certificationService.baseListByOrgCertId(certId);
        if (certificationDTOS.isEmpty()) {
            devopsCertificationProRelationshipService.baseDeleteByCertificationId(certId);
            certificationService.baseDeleteById(certId);
        } else {
            throw new CommonException("devops.cert.related");
        }
    }

    @Override
    public void deletePermissionOfProject(Long projectId, Long certId) {
        if (projectId == null || certId == null) {
            return;
        }
        DevopsCertificationProRelationshipDTO devopsCertificationProRelationshipDTO = new DevopsCertificationProRelationshipDTO();
        devopsCertificationProRelationshipDTO.setProjectId(projectId);
        devopsCertificationProRelationshipDTO.setCertId(certId);
        devopsCertificationProRelationshipService.baseDelete(devopsCertificationProRelationshipDTO);
    }

    @Override
    public Page<ProjectCertificationVO> pageCerts(Long projectId, PageRequest pageable,
                                                  String params) {
        Page<CertificationDTO> certificationDTOS = certificationService
                .basePage(projectId, null, pageable, params);
        Page<ProjectCertificationVO> orgCertificationDTOS = new Page<>();
        BeanUtils.copyProperties(certificationDTOS, orgCertificationDTOS);
        List<ProjectCertificationVO> orgCertifications = new ArrayList<>();

        if (!certificationDTOS.getContent().isEmpty()) {
            certificationDTOS.getContent().forEach(certificationDTO -> {
                ProjectCertificationVO orgCertificationVO = new ProjectCertificationVO();
                orgCertificationVO.setId(certificationDTO.getId());
                orgCertificationVO.setName(certificationDTO.getName());
                orgCertificationVO.setDomain(certificationDTO.getDomains());
                orgCertificationVO.setSkipCheckProjectPermission(certificationDTO.getSkipCheckProjectPermission());
                orgCertificationVO.setObjectVersionNumber(certificationDTO.getObjectVersionNumber());
                orgCertifications.add(orgCertificationVO);
            });
        }
        orgCertificationDTOS.setContent(orgCertifications);
        return orgCertificationDTOS;
    }

    @Override
    public ProjectCertificationVO queryCert(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.queryById(certId);
        List<String> stringList = gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {
        }.getType());
        ProjectCertificationVO projectCertificationVO = new ProjectCertificationVO();
        projectCertificationVO.setId(certificationDTO.getId());
        projectCertificationVO.setName(certificationDTO.getName());
        if (!CollectionUtils.isEmpty(stringList)) {
            projectCertificationVO.setDomain(stringList.get(0));
        }
        projectCertificationVO.setSkipCheckProjectPermission(certificationDTO.getSkipCheckProjectPermission());
        projectCertificationVO.setObjectVersionNumber(certificationDTO.getObjectVersionNumber());
        projectCertificationVO.setKeyValue(certificationDTO.getKeyValue());
        projectCertificationVO.setCertValue(certificationDTO.getCertValue());
        return projectCertificationVO;
    }
}
