package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.ProjectCertificationVO;
import io.choerodon.devops.api.vo.ProjectReqVO;

public interface DevopsProjectCertificationService {

    void create(Long projectId, MultipartFile key, MultipartFile cert, ProjectCertificationVO projectCertificationVO);

    void update(Long certId, ProjectCertificationVO projectCertificationVO);

    void checkName(Long projectId, String name);

    List<ProjectReqVO> listCertProjects(Long certId);

    List<ProjectReqVO> listNonRelatedMembers(Long projectId, Long certId, String params);

    void deleteCert(Long certId);

    void deletePermissionOfProject(Long projectId, Long certId);

    PageInfo<ProjectReqVO> pageProjects(Long projectId, Long clusterId, PageRequest pageRequest,
                                        String[] params);

    PageInfo<ProjectCertificationVO> pageCerts(Long projectId, PageRequest pageRequest,
                                               String params);

    ProjectCertificationVO queryCert(Long certId);

}
