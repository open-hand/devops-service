package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CertificationVO;
import io.choerodon.devops.api.vo.OrgCertificationDTO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.service.DevopsOrgCertificationService;
import io.choerodon.devops.api.vo.iam.entity.CertificationE;
import io.choerodon.devops.api.vo.iam.entity.DevopsCertificationProRelE;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.domain.application.repository.DevopsCertificationProRelRepository;
import io.choerodon.devops.domain.application.valueobject.OrganizationVO;
import io.choerodon.devops.infra.util.FileUtil;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.SslUtil;
import io.choerodon.devops.infra.dto.CertificationFileDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DevopsOrgCertificationServiceImpl implements DevopsOrgCertificationService {


    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private Gson gson = new Gson();
    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private DevopsCertificationProRelRepository devopsCertificationProRelRepository;
    @Autowired
    private IamRepository iamRepository;

    @Override
    public void insert(Long organizationId, MultipartFile key, MultipartFile cert, OrgCertificationDTO orgCertificationDTO) {


        //如果是选择上传文件方式
        OrganizationVO organization = iamRepository.queryOrganizationById(organizationId);
        String path = String.format("tmp%s%s%s%s", FILE_SEPARATOR, organization.getCode(), FILE_SEPARATOR, GenerateUUID.generateUUID().substring(0, 5));
        String certFileName;
        String keyFileName;

        if (key != null && cert != null) {
            certFileName = cert.getOriginalFilename();
            keyFileName = key.getOriginalFilename();
            orgCertificationDTO.setKeyValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, key))));
            orgCertificationDTO.setCertValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, cert))));
        } else {
            certFileName = String.format("%s.%s",GenerateUUID.generateUUID().substring(0, 5),"crt");
            keyFileName = String.format("%s.%s",GenerateUUID.generateUUID().substring(0, 5),"key");
            FileUtil.saveDataToFile(path, certFileName, orgCertificationDTO.getCertValue());
            FileUtil.saveDataToFile(path, keyFileName, orgCertificationDTO.getKeyValue());
        }
        File certPath = new File(path + FILE_SEPARATOR + certFileName);
        File keyPath = new File(path + FILE_SEPARATOR + keyFileName);
        try {
            SslUtil.validate(certPath, keyPath);
        }catch (Exception e) {
            FileUtil.deleteFile(certPath);
            FileUtil.deleteFile(keyPath);
            throw new CommonException(e);
        }
        FileUtil.deleteFile(certPath);
        FileUtil.deleteFile(keyPath);



        CertificationE certificationE = new CertificationE();
        certificationE.setName(orgCertificationDTO.getName());
        certificationE.setOrganizationId(organizationId);
        certificationE.setSkipCheckProjectPermission(orgCertificationDTO.getSkipCheckProjectPermission());
        certificationE.setDomains(Arrays.asList(orgCertificationDTO.getDomain()));
        certificationE.setCertificationFileId(certificationRepository.baseStoreCertFile(new CertificationFileDTO(orgCertificationDTO.getCertValue(), orgCertificationDTO.getKeyValue())));
        certificationE = certificationRepository.baseCreate(certificationE);
        if (!orgCertificationDTO.getSkipCheckProjectPermission() && orgCertificationDTO.getProjects() != null) {
            for (Long projectId : orgCertificationDTO.getProjects()) {
                DevopsCertificationProRelE devopsCertificationProRelE = new DevopsCertificationProRelE();
                devopsCertificationProRelE.setCertId(certificationE.getId());
                devopsCertificationProRelE.setProjectId(projectId);
                devopsCertificationProRelRepository.baseInsertRelationship(devopsCertificationProRelE);
            }
        }
    }


    public void update(Long certId, OrgCertificationDTO orgCertificationDTO) {
        List<Long> projects = orgCertificationDTO.getProjects();
        Boolean skipCheckPro = orgCertificationDTO.getSkipCheckProjectPermission();
        List<Long> addProjects = new ArrayList<>();
        CertificationE certificationE = certificationRepository.baseQueryById(certId);
        //以前不跳过项目权限校验,但是现在跳过，情况集群对应的项目集群校验表
        if (skipCheckPro && !certificationE.getSkipCheckProjectPermission()) {
            devopsCertificationProRelRepository.baseDeleteByCertificationId(certId);
        } else {
            //操作集群项目权限校验表记录
            List<Long> projectIds = devopsCertificationProRelRepository.baseListByCertificationId(certId)
                    .stream().map(DevopsCertificationProRelE::getProjectId).collect(Collectors.toList());

            projects.forEach(projectId -> {
                if (!projectIds.contains(projectId)) {
                    addProjects.add(projectId);
                } else {
                    projectIds.remove(projectId);
                }
            });
            addProjects.forEach(addProject -> {
                DevopsCertificationProRelE devopsClusterProPermissionE = new DevopsCertificationProRelE();
                devopsClusterProPermissionE.setCertId(certId);
                devopsClusterProPermissionE.setProjectId(addProject);
                devopsCertificationProRelRepository.baseInsertRelationship(devopsClusterProPermissionE);
            });
            projectIds.forEach(deleteProject -> {
                DevopsCertificationProRelE devopsClusterProPermissionE = new DevopsCertificationProRelE();
                devopsClusterProPermissionE.setCertId(certId);
                devopsClusterProPermissionE.setProjectId(deleteProject);
                devopsCertificationProRelRepository.baseDelete(devopsClusterProPermissionE);
            });
        }
        certificationE.setSkipCheckProjectPermission(orgCertificationDTO.getSkipCheckProjectPermission());
        certificationRepository.baseUpdateSkipProjectPermission(certificationE);
    }


    public void checkName(Long organizationId, String name) {
        CertificationE certificationE = new CertificationE();
        certificationE.setOrganizationId(organizationId);
        certificationE.setName(name);
        if (certificationRepository.baseQueryByOrgAndName(organizationId, name) != null) {
            throw new CommonException("error.cert.name.exist");
        }
    }

    public List<ProjectReqVO> listCertProjects(Long certId) {
        return devopsCertificationProRelRepository.baseListByCertificationId(certId).stream()
                .map(devopsCertificationProRelE -> {
                    ProjectVO projectE = iamRepository.queryIamProject(devopsCertificationProRelE.getProjectId());

                    return new ProjectReqVO(devopsCertificationProRelE.getProjectId(), projectE.getName(), projectE.getCode(), null);
                }).collect(Collectors.toList());
    }

    public void deleteCert(Long certId) {
        List<CertificationE> certificationES = certificationRepository.baseListByOrgCertId(certId);
        if (certificationES.isEmpty()) {
            devopsCertificationProRelRepository.baseDeleteByCertificationId(certId);
            certificationRepository.baseDeleteById(certId);
        } else {
            throw new CommonException("error.cert.related");
        }
    }

    public PageInfo<ProjectReqVO> listProjects(Long organizationId, Long clusterId, PageRequest pageRequest,
                                               String[] params) {
        PageInfo<ProjectVO> projects = iamRepository
                .queryProjectByOrgId(organizationId, pageRequest.getPage(), pageRequest.getSize(), null, params);
        PageInfo<ProjectReqVO> pageProjectDTOS = new PageInfo<>();
        List<ProjectReqVO> projectDTOS = new ArrayList<>();
        if (!projects.getList().isEmpty()) {
            BeanUtils.copyProperties(projects, pageProjectDTOS);
            List<Long> projectIds;
            if (clusterId != null) {
                projectIds = devopsCertificationProRelRepository.baseListByCertificationId(clusterId).stream()
                        .map(DevopsCertificationProRelE::getProjectId).collect(Collectors.toList());
            } else {
                projectIds = new ArrayList<>();
            }
            projects.getList().forEach(projectDO -> {
                ProjectReqVO projectDTO = new ProjectReqVO(projectDO.getId(), projectDO.getName(), projectDO.getCode(), projectIds.contains(projectDO.getId()));
                projectDTOS.add(projectDTO);
            });
        }
        BeanUtils.copyProperties(projects, pageProjectDTOS);
        pageProjectDTOS.setList(projectDTOS);
        return pageProjectDTOS;
    }


    public PageInfo<OrgCertificationDTO> pageCerts(Long organizationId, PageRequest pageRequest,
                                               String params) {
        PageInfo<CertificationVO> certificationDTOS = certificationRepository
                .basePage(null, organizationId, null, pageRequest, params);
        PageInfo<OrgCertificationDTO> orgCertificationDTOS = new PageInfo<>();
        BeanUtils.copyProperties(certificationDTOS, orgCertificationDTOS);
        List<OrgCertificationDTO> orgCertifications = new ArrayList<>();
        if (!certificationDTOS.getList().isEmpty()) {
            certificationDTOS.getList().forEach(certificationDTO -> {
                OrgCertificationDTO orgCertificationDTO = new OrgCertificationDTO(certificationDTO.getId(), certificationDTO.getCertName(), certificationDTO.getDomains().get(0), certificationDTO.getSkipCheckProjectPermission());
                orgCertifications.add(orgCertificationDTO);
            });
        }
        orgCertificationDTOS.setList(orgCertifications);
        return orgCertificationDTOS;
    }

    public OrgCertificationDTO getCert(Long certId) {
        CertificationE certificationE = certificationRepository.baseQueryById(certId);
        return new OrgCertificationDTO(certificationE.getId(), certificationE.getName(), certificationE.getDomains().get(0), certificationE.getSkipCheckProjectPermission());
    }
}
