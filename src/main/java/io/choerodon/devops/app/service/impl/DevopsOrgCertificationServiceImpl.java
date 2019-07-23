package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.OrgCertificationVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.app.service.DevopsCertificationProRelationshipService;
import io.choerodon.devops.app.service.DevopsOrgCertificationService;
import io.choerodon.devops.app.service.IamService;
import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.devops.infra.dto.CertificationFileDTO;
import io.choerodon.devops.infra.dto.DevopsCertificationProRelationshipDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.SslUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DevopsOrgCertificationServiceImpl implements DevopsOrgCertificationService {


    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private Gson gson = new Gson();
    @Autowired
    private DevopsCertificationProRelationshipService devopsCertificationProRelationshipService;
    @Autowired
    private IamService iamService;
    @Autowired
    private CertificationService certificationService;

    @Override
    @Transactional
    public void create(Long organizationId, MultipartFile key, MultipartFile cert, OrgCertificationVO orgCertificationVO) {
        //如果是选择上传文件方式
        OrganizationDTO organizationDTO = iamService.queryOrganizationById(organizationId);
        String path = String.format("tmp%s%s%s%s", FILE_SEPARATOR, organizationDTO.getCode(), FILE_SEPARATOR, GenerateUUID.generateUUID().substring(0, 5));
        String certFileName;
        String keyFileName;

        if (key != null && cert != null) {
            certFileName = cert.getOriginalFilename();
            keyFileName = key.getOriginalFilename();
            orgCertificationVO.setKeyValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, key))));
            orgCertificationVO.setCertValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, cert))));
        } else {
            certFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "crt");
            keyFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "key");
            FileUtil.saveDataToFile(path, certFileName, orgCertificationVO.getCertValue());
            FileUtil.saveDataToFile(path, keyFileName, orgCertificationVO.getKeyValue());
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
        certificationDTO.setName(orgCertificationVO.getName());
        certificationDTO.setOrganizationId(organizationId);
        certificationDTO.setSkipCheckProjectPermission(orgCertificationVO.getSkipCheckProjectPermission());
        certificationDTO.setDomains(orgCertificationVO.getDomain());
        certificationDTO.setCertificationFileId(certificationService.baseStoreCertFile(new CertificationFileDTO(orgCertificationVO.getCertValue(), orgCertificationVO.getKeyValue())));
        certificationDTO = certificationService.baseCreate(certificationDTO);
        if (!orgCertificationVO.getSkipCheckProjectPermission() && orgCertificationVO.getProjects() != null) {
            for (Long projectId : orgCertificationVO.getProjects()) {
                DevopsCertificationProRelationshipDTO devopsCertificationProRelationshipDTO = new DevopsCertificationProRelationshipDTO();
                devopsCertificationProRelationshipDTO.setCertId(certificationDTO.getId());
                devopsCertificationProRelationshipDTO.setProjectId(projectId);
                devopsCertificationProRelationshipService.baseInsertRelationship(devopsCertificationProRelationshipDTO);
            }
        }
    }


    @Override
    @Transactional
    public void update(Long certId, OrgCertificationVO orgCertificationVO) {
        List<Long> projects = orgCertificationVO.getProjects();
        Boolean skipCheckPro = orgCertificationVO.getSkipCheckProjectPermission();
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
        certificationDTO.setSkipCheckProjectPermission(orgCertificationVO.getSkipCheckProjectPermission());
        certificationService.baseUpdateSkipProjectPermission(certificationDTO);
    }


    @Override
    public void checkName(Long organizationId, String name) {
        CertificationE certificationE = new CertificationE();
        certificationE.setOrganizationId(organizationId);
        certificationE.setName(name);
        if (certificationService.baseQueryByOrgAndName(organizationId, name) != null) {
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
    public PageInfo<ProjectReqVO> pageProjects(Long organizationId, Long clusterId, PageRequest pageRequest,
                                               String[] params) {
        PageInfo<ProjectDTO> projectDTOPageInfo = iamService
                .queryProjectByOrgId(organizationId, pageRequest.getPage(), pageRequest.getSize(), null, params);
        PageInfo<ProjectReqVO> pageProjectDTOS = new PageInfo<>();
        List<ProjectReqVO> projectDTOS = new ArrayList<>();
        if (!projectDTOPageInfo.getList().isEmpty()) {
            BeanUtils.copyProperties(projectDTOPageInfo, pageProjectDTOS);
            List<Long> projectIds;
            if (clusterId != null) {
                projectIds = devopsCertificationProRelationshipService.baseListByCertificationId(clusterId).stream()
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
    public PageInfo<OrgCertificationVO> pageCerts(Long organizationId, PageRequest pageRequest,
                                                  String params) {
        PageInfo<CertificationDTO> certificationDTOS = certificationService
                .basePage(null, organizationId, null, pageRequest, params);
        PageInfo<OrgCertificationVO> orgCertificationDTOS = new PageInfo<>();
        BeanUtils.copyProperties(certificationDTOS, orgCertificationDTOS);
        List<OrgCertificationVO> orgCertifications = new ArrayList<>();

        if (!certificationDTOS.getList().isEmpty()) {
            certificationDTOS.getList().forEach(certificationDTO -> {
                List<String> stringList = gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {
                }.getType());
                OrgCertificationVO orgCertificationVO = new OrgCertificationVO(certificationDTO.getId(), certificationDTO.getName(), stringList.get(0), certificationDTO.getSkipCheckProjectPermission());
                orgCertifications.add(orgCertificationVO);
            });
        }
        orgCertificationDTOS.setList(orgCertifications);
        return orgCertificationDTOS;
    }

    @Override
    public OrgCertificationVO queryCert(Long certId) {
        CertificationDTO certificationDTO = certificationService.baseQueryById(certId);
        List<String> stringList = gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {
        }.getType());
        return new OrgCertificationVO(certificationDTO.getId(), certificationDTO.getName(), stringList.get(0), certificationDTO.getSkipCheckProjectPermission());
    }
}
