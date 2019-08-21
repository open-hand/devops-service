package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.ProjectCertificationPermissionUpdateVO;
import io.choerodon.devops.api.vo.ProjectCertificationVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.devops.infra.dto.CertificationFileDTO;
import io.choerodon.devops.infra.dto.DevopsCertificationProRelationshipDTO;
import io.choerodon.devops.infra.dto.DevopsClusterProPermissionDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper;
import io.choerodon.devops.infra.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DevopsProjectCertificationServiceImpl implements DevopsProjectCertificationService {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private Gson gson = new Gson();
    @Autowired
    private DevopsCertificationProRelationshipService devopsCertificationProRelationshipService;
    @Autowired
    private IamService iamService;
    @Autowired
    private CertificationService certificationService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
    @Autowired
    private DevopsProjectService devopsProjectService;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void assignPermission(ProjectCertificationPermissionUpdateVO update) {
        CertificationDTO certificationDTO = certificationService.baseQueryById(update.getCertificationId());
        if (certificationDTO == null) {
            throw new CommonException("error.certification.not.exist", update.getCertificationId());
        }

        if (certificationDTO.getProjectId() == null) {
            throw new CommonException("error.not.project.certification", update.getCertificationId());
        }

        if (certificationDTO.getSkipCheckProjectPermission()) {
            if (update.getSkipCheckPermission()) {
                // 原来跳过，现在也跳过，不处理
                return;
            } else {
                // 原来跳过，现在不跳过，先更新字段，然后插入关联关系
                updateSkipPermissionCheck(
                        update.getCertificationId(),
                        update.getSkipCheckPermission(),
                        update.getObjectVersionNumber());

                devopsCertificationProRelationshipService.batchInsertIgnore(
                        update.getCertificationId(),
                        update.getProjectIds());
            }
        } else {
            // 原来不跳过，现在跳过，更新证书权限字段，再删除所有数据库中与该证书有关的关联关系
            if (update.getSkipCheckPermission()) {
                updateSkipPermissionCheck(
                        update.getCertificationId(),
                        update.getSkipCheckPermission(),
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
    public PageInfo<ProjectReqVO> pageRelatedProjects(Long projectId, Long certId, PageRequest pageRequest, String params) {
        CertificationDTO certificationDTO = certificationService.baseQueryById(certId);
        if (certificationDTO == null) {
            throw new CommonException("error.certification.not.exist", certId);
        }

        // 如果跳过权限检查
        if (Boolean.TRUE.equals(certificationDTO.getSkipCheckProjectPermission())) {
            return devopsProjectService.pageProjects(projectId, pageRequest, params);
        } else {
            // 如果没有跳过权限检查
            Map<String, Object> map = TypeUtil.castMapParams(params);
            List<String> paramList = TypeUtil.cast(map.get(TypeUtil.PARAMS));
            if (CollectionUtils.isEmpty(paramList)) {
                // 如果不搜索，在数据库中进行分页
                PageInfo<DevopsClusterProPermissionDTO> relationPage = PageHelper.startPage(
                        pageRequest.getPage(), pageRequest.getSize())
                        .doSelectPageInfo(() -> devopsCertificationProRelationshipService.baseListByCertificationId(certId));
                return ConvertUtils.convertPage(relationPage, permission -> {
                    ProjectDTO projectDTO = baseServiceClientOperator.queryIamProjectById(permission.getProjectId());
                    return new ProjectReqVO(permission.getProjectId(), projectDTO.getName(), projectDTO.getCode());
                });
            } else {
                // 如果要搜索，需要手动在程序内分页
                ProjectDTO iamProjectDTO = baseServiceClientOperator.queryIamProjectById(projectId);

                // 手动查出所有组织下的项目
                PageInfo<ProjectDTO> filteredProjects = baseServiceClientOperator.pageProjectByOrgId(
                        iamProjectDTO.getOrganizationId(),
                        0, 0, null,
                        paramList.toArray(new String[0]));

                // 数据库中的有权限的项目
                List<Long> permissions = devopsCertificationProRelationshipService.baseListByCertificationId(certId)
                        .stream()
                        .map(DevopsCertificationProRelationshipDTO::getProjectId)
                        .collect(Collectors.toList());

                // 过滤出在数据库中有权限的项目信息
                List<ProjectReqVO> allMatched = filteredProjects.getList()
                        .stream()
                        .filter(p -> permissions.contains(p.getId()))
                        .map(p -> ConvertUtils.convertObject(p, ProjectReqVO.class))
                        .collect(Collectors.toList());

                return PageInfoUtil.createPageFromList(allMatched, pageRequest);
            }
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
    @Transactional
    public void create(Long projectId, MultipartFile key, MultipartFile cert, ProjectCertificationVO projectCertificationVO) {
        //如果是选择上传文件方式
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(projectId);
        String path = String.format("tmp%s%s%s%s", FILE_SEPARATOR, organizationDTO.getCode(), FILE_SEPARATOR, GenerateUUID.generateUUID().substring(0, 5));
        String certFileName;
        String keyFileName;

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
        } catch (Exception e) {
            FileUtil.deleteFile(certPath);
            FileUtil.deleteFile(keyPath);
            throw new CommonException(e);
        }
        FileUtil.deleteFile(certPath);
        FileUtil.deleteFile(keyPath);

        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setName(projectCertificationVO.getName());
        certificationDTO.setProjectId(projectId);
        certificationDTO.setSkipCheckProjectPermission(true);
        certificationDTO.setDomains(projectCertificationVO.getDomain());
        certificationDTO.setCertificationFileId(certificationService.baseStoreCertFile(new CertificationFileDTO(projectCertificationVO.getCertValue(), projectCertificationVO.getKeyValue())));
        certificationService.baseCreate(certificationDTO);
    }


    @Override
    @Transactional
    public void update(Long certId, ProjectCertificationVO projectCertificationVO) {
        List<Long> projects = projectCertificationVO.getProjects();
        Boolean skipCheckPro = projectCertificationVO.getSkipCheckProjectPermission();
        List<Long> addProjects = new ArrayList<>();
        CertificationDTO certificationDTO = certificationService.baseQueryById(certId);
        //以前不跳过项目权限校验,但是现在跳过，情况集群对应的项目集群校验表
        if (skipCheckPro && !certificationDTO.getSkipCheckProjectPermission()) {
            devopsCertificationProRelationshipService.baseDeleteByCertificationId(certId);
        } else {
            //操作集群项目权限校验表记录
            List<Long> projectIds = devopsCertificationProRelationshipService.baseListByCertificationId(certId)
                    .stream().map(DevopsCertificationProRelationshipDTO::getProjectId).collect(Collectors.toList());

            projects.forEach(projectId -> {
                if (!projectIds.contains(projectId)) {
                    addProjects.add(projectId);
                } else {
                    projectIds.remove(projectId);
                }
            });
            addProjects.forEach(addProject -> {
                DevopsCertificationProRelationshipDTO devopsCertificationProRelationshipDTO = new DevopsCertificationProRelationshipDTO();
                devopsCertificationProRelationshipDTO.setCertId(certId);
                devopsCertificationProRelationshipDTO.setProjectId(addProject);
                devopsCertificationProRelationshipService.baseInsertRelationship(devopsCertificationProRelationshipDTO);
            });
            projectIds.forEach(deleteProject -> {
                DevopsCertificationProRelationshipDTO devopsCertificationProRelationshipDTO = new DevopsCertificationProRelationshipDTO();
                devopsCertificationProRelationshipDTO.setCertId(certId);
                devopsCertificationProRelationshipDTO.setProjectId(deleteProject);
                devopsCertificationProRelationshipService.baseDelete(devopsCertificationProRelationshipDTO);
            });
        }
        certificationDTO.setSkipCheckProjectPermission(projectCertificationVO.getSkipCheckProjectPermission());
        certificationService.baseUpdateSkipProjectPermission(certificationDTO);
    }


    @Override
    public void checkName(Long projectId, String name) {
        if (certificationService.baseQueryByProjectAndName(projectId, name) != null) {
            throw new CommonException("error.cert.name.exist");
        }
    }

    @Override
    public List<ProjectReqVO> listCertProjects(Long certId) {
        return devopsCertificationProRelationshipService.baseListByCertificationId(certId).stream()
                .map(devopsCertificationProRelE -> {
                    ProjectDTO projectDTO = iamService.queryIamProject(devopsCertificationProRelE.getProjectId());
                    return new ProjectReqVO(devopsCertificationProRelE.getProjectId(), projectDTO.getName(), projectDTO.getCode(), null);
                }).collect(Collectors.toList());
    }

    @Override
    public List<ProjectReqVO> listNonRelatedMembers(Long projectId, Long certId, String params) {
        //查询出该项目所属组织下的所有项目
        Map<String, Object> searchMap = TypeUtil.castMapParams(params);
        List<String> paramList = TypeUtil.cast(searchMap.get(TypeUtil.PARAMS));
        ProjectDTO iamProjectDTO = baseServiceClientOperator.queryIamProjectById(projectId);
        OrganizationDTO organizationDTO = baseServiceClientOperator.queryOrganizationById(iamProjectDTO.getOrganizationId());
        PageInfo<ProjectDTO> projectDTOPageInfo = baseServiceClientOperator.listProject(organizationDTO.getId(), new PageRequest(0, 0), paramList == null ? null : paramList.toArray(new String[0]));

        //查询已经分配权限的项目
        List<Long> permitted = devopsCertificationProRelationshipService.baseListByCertificationId(certId)
                .stream()
                .map(DevopsCertificationProRelationshipDTO::getProjectId)
                .collect(Collectors.toList());

        //把组织下有权限的项目过滤掉再返回
        return projectDTOPageInfo.getList().stream()
                .filter(i -> !permitted.contains(i.getId()))
                .map(i -> new ProjectReqVO(i.getId(), i.getName(), i.getCode(), null))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCert(Long certId) {
        List<CertificationDTO> certificationDTOS = certificationService.baseListByOrgCertId(certId);
        if (certificationDTOS.isEmpty()) {
            devopsCertificationProRelationshipService.baseDeleteByCertificationId(certId);
            certificationService.baseDeleteById(certId);
        } else {
            throw new CommonException("error.cert.related");
        }
    }

    @Override
    public void deletePermissionOfProject(Long projectId, Long certId) {
        DevopsCertificationProRelationshipDTO devopsCertificationProRelationshipDTO = new DevopsCertificationProRelationshipDTO();
        devopsCertificationProRelationshipDTO.setProjectId(projectId);
        devopsCertificationProRelationshipDTO.setCertId(certId);
        devopsCertificationProRelationshipService.baseDelete(devopsCertificationProRelationshipDTO);
    }

    @Override
    public PageInfo<ProjectReqVO> pageProjects(Long projectId, Long certId, PageRequest pageRequest,
                                               String[] params) {
        ProjectDTO currentProjectDTO = iamService.queryIamProject(projectId);
        PageInfo<ProjectDTO> projectDTOPageInfo = iamService
                .queryProjectByOrgId(currentProjectDTO.getOrganizationId(), pageRequest.getPage(), pageRequest.getSize(), null, params);
        PageInfo<ProjectReqVO> pageProjectDTOS = new PageInfo<>();
        List<ProjectReqVO> projectDTOS = new ArrayList<>();
        if (!projectDTOPageInfo.getList().isEmpty()) {
            BeanUtils.copyProperties(projectDTOPageInfo, pageProjectDTOS);
            List<Long> projectIds;
            if (certId != null) {
                projectIds = devopsCertificationProRelationshipService.baseListByCertificationId(certId).stream()
                        .map(DevopsCertificationProRelationshipDTO::getProjectId).collect(Collectors.toList());
            } else {
                projectIds = new ArrayList<>();
            }
            projectDTOPageInfo.getList().forEach(projectDO -> {
                ProjectReqVO projectDTO = new ProjectReqVO(projectDO.getId(), projectDO.getName(), projectDO.getCode(), projectIds.contains(projectDO.getId()));
                projectDTOS.add(projectDTO);
            });
        }
        BeanUtils.copyProperties(projectDTOPageInfo, pageProjectDTOS);
        pageProjectDTOS.setList(projectDTOS);
        return pageProjectDTOS;
    }


    @Override
    public PageInfo<ProjectCertificationVO> pageCerts(Long projectId, PageRequest pageRequest,
                                                      String params) {
        PageInfo<CertificationDTO> certificationDTOS = certificationService
                .basePage(projectId, null, pageRequest, params);
        PageInfo<ProjectCertificationVO> orgCertificationDTOS = new PageInfo<>();
        BeanUtils.copyProperties(certificationDTOS, orgCertificationDTOS);
        List<ProjectCertificationVO> orgCertifications = new ArrayList<>();

        if (!certificationDTOS.getList().isEmpty()) {
            certificationDTOS.getList().forEach(certificationDTO -> {
                List<String> stringList = gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {
                }.getType());
                ProjectCertificationVO orgCertificationVO = new ProjectCertificationVO(certificationDTO.getId(), certificationDTO.getName(), stringList.get(0), certificationDTO.getSkipCheckProjectPermission());
                orgCertifications.add(orgCertificationVO);
            });
        }
        orgCertificationDTOS.setList(orgCertifications);
        return orgCertificationDTOS;
    }

    @Override
    public ProjectCertificationVO queryCert(Long certId) {
        CertificationDTO certificationDTO = certificationService.baseQueryById(certId);
        List<String> stringList = gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {
        }.getType());
        return new ProjectCertificationVO(certificationDTO.getId(), certificationDTO.getName(), stringList.get(0), certificationDTO.getSkipCheckProjectPermission());
    }
}
