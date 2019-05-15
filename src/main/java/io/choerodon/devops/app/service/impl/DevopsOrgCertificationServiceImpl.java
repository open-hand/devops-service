package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.CertificationDTO;
import io.choerodon.devops.api.dto.OrgCertificationDTO;
import io.choerodon.devops.api.dto.ProjectDTO;
import io.choerodon.devops.app.service.DevopsOrgCertificationService;
import io.choerodon.devops.domain.application.entity.CertificationE;
import io.choerodon.devops.domain.application.entity.DevopsCertificationProRelE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.repository.CertificationRepository;
import io.choerodon.devops.domain.application.repository.DevopsCertificationProRelRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.infra.dataobject.CertificationFileDO;
import io.choerodon.devops.infra.dataobject.iam.ProjectDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

@Service
public class DevopsOrgCertificationServiceImpl implements DevopsOrgCertificationService {

    private Gson gson = new Gson();
    @Autowired
    private CertificationRepository certificationRepository;
    @Autowired
    private DevopsCertificationProRelRepository devopsCertificationProRelRepository;
    @Autowired
    private IamRepository iamRepository;

    public void insert(Long organizationId, OrgCertificationDTO orgCertificationDTO) {
        CertificationE certificationE = new CertificationE();
        certificationE.setName(orgCertificationDTO.getName());
        certificationE.setOrganizationId(organizationId);
        certificationE.setSkipCheckProjectPermission(orgCertificationDTO.getSkipCheckProjectPermission());
        certificationE.setDomains(Arrays.asList(orgCertificationDTO.getDomain()));
        certificationE.setCertificationFileId(certificationRepository.storeCertFile(new CertificationFileDO(orgCertificationDTO.getKeyValue(), orgCertificationDTO.getCertValue())));
        certificationE = certificationRepository.create(certificationE);
        if (!orgCertificationDTO.getSkipCheckProjectPermission() && orgCertificationDTO.getProjects() != null) {
            for (Long projectId : orgCertificationDTO.getProjects()) {
                DevopsCertificationProRelE devopsCertificationProRelE = new DevopsCertificationProRelE();
                devopsCertificationProRelE.setCertId(certificationE.getId());
                devopsCertificationProRelE.setProjectId(projectId);
                devopsCertificationProRelRepository.insert(devopsCertificationProRelE);
            }
        }
    }


    public void update(Long certId, OrgCertificationDTO orgCertificationDTO) {
        List<Long> projects = orgCertificationDTO.getProjects();
        Boolean skipCheckPro = orgCertificationDTO.getSkipCheckProjectPermission();
        List<Long> addProjects = new ArrayList<>();
        CertificationE certificationE = certificationRepository.queryById(certId);
        //以前不跳过项目权限校验,但是现在跳过，情况集群对应的项目集群校验表
        if (skipCheckPro && !certificationE.getSkipCheckProjectPermission()) {
            devopsCertificationProRelRepository.deleteByCertId(certId);
        } else {
            //操作集群项目权限校验表记录
            List<Long> projectIds = devopsCertificationProRelRepository.listByCertId(certId)
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
                devopsCertificationProRelRepository.insert(devopsClusterProPermissionE);
            });
            projectIds.forEach(deleteProject -> {
                DevopsCertificationProRelE devopsClusterProPermissionE = new DevopsCertificationProRelE();
                devopsClusterProPermissionE.setCertId(certId);
                devopsClusterProPermissionE.setProjectId(deleteProject);
                devopsCertificationProRelRepository.delete(devopsClusterProPermissionE);
            });
        }
        certificationE.setSkipCheckProjectPermission(orgCertificationDTO.getSkipCheckProjectPermission());
        certificationRepository.updateSkipProjectPermission(certificationE);
    }


    public void checkName(Long organizationId, String name) {
        CertificationE certificationE = new CertificationE();
        certificationE.setOrganizationId(organizationId);
        certificationE.setName(name);
        if (certificationRepository.queryByOrgAndName(organizationId, name) != null) {
            throw new CommonException("error.cert.name.exist");
        }
    }

    public List<ProjectDTO> listCertProjects(Long certId) {
        return devopsCertificationProRelRepository.listByCertId(certId).stream()
                .map(devopsCertificationProRelE -> {
                    ProjectE projectE = iamRepository.queryIamProject(devopsCertificationProRelE.getProjectId());

                    return new ProjectDTO(devopsCertificationProRelE.getProjectId(), projectE.getName(), projectE.getCode(), null);
                }).collect(Collectors.toList());
    }

    public void deleteCert(Long certId) {
        List<CertificationE> certificationES = certificationRepository.listByOrgCertId(certId);
        if (certificationES.isEmpty()) {
            devopsCertificationProRelRepository.deleteByCertId(certId);
            certificationRepository.deleteById(certId);
        } else {
            throw new CommonException("error.cert.related");
        }
    }

    public Page<ProjectDTO> listProjects(Long organizationId, Long clusterId, PageRequest pageRequest,
                                         String[] params) {
        List<ProjectE> projects = iamRepository
                .queryProjectByOrgId(organizationId, pageRequest.getPage(), pageRequest.getSize(), null, params);
        Page<ProjectDTO> pageProjectDTOS = new Page<>();
        List<ProjectDTO> projectDTOS = new ArrayList<>();
        if (!projects.isEmpty()) {
            BeanUtils.copyProperties(projects, pageProjectDTOS);
            List<Long> projectIds;
            if (clusterId != null) {
                projectIds = devopsCertificationProRelRepository.listByCertId(clusterId).stream()
                        .map(DevopsCertificationProRelE::getProjectId).collect(Collectors.toList());
            } else {
                projectIds = new ArrayList<>();
            }
            projects.forEach(projectDO -> {
                ProjectDTO projectDTO = new ProjectDTO(projectDO.getId(), projectDO.getName(), projectDO.getCode(), projectIds.contains(projectDO.getId()));
                projectDTOS.add(projectDTO);
            });
        }
        pageProjectDTOS.setContent(projectDTOS);
        return pageProjectDTOS;
    }


    public Page<OrgCertificationDTO> pageCerts(Long organizationId, PageRequest pageRequest,
                                               String params) {
        Page<CertificationDTO> certificationDTOS = certificationRepository
                .page(null, organizationId, null, pageRequest, params);
        Page<OrgCertificationDTO> orgCertificationDTOS = new Page<>();
        BeanUtils.copyProperties(certificationDTOS, orgCertificationDTOS);
        List<OrgCertificationDTO> orgCertifications = new ArrayList<>();
        if (!certificationDTOS.getContent().isEmpty()) {
            certificationDTOS.getContent().forEach(certificationDTO -> {
                OrgCertificationDTO orgCertificationDTO = new OrgCertificationDTO(certificationDTO.getId(), certificationDTO.getCertName(), certificationDTO.getDomains().get(0), certificationDTO.getSkipCheckProjectPermission());
                orgCertifications.add(orgCertificationDTO);
            });
        }
        orgCertificationDTOS.setContent(orgCertifications);
        return orgCertificationDTOS;
    }

    public OrgCertificationDTO getCert(Long certId) {
        CertificationE certificationE = certificationRepository.queryById(certId);
        return new OrgCertificationDTO(certificationE.getId(), certificationE.getName(), certificationE.getDomains().get(0), certificationE.getSkipCheckProjectPermission());
    }
}
