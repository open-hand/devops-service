package io.choerodon.devops.app.service;

import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.ProjectCertificationCreateUpdateVO;
import io.choerodon.devops.api.vo.ProjectCertificationPermissionUpdateVO;
import io.choerodon.devops.api.vo.ProjectCertificationVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsProjectCertificationService {
    /**
     * 分配权限
     *
     * @param permissionUpdateVO 权限信息
     */
    void assignPermission(ProjectCertificationPermissionUpdateVO permissionUpdateVO);

    /**
     * 分页查询在数据中和证书已经有关联关系的项目列表
     *
     * @param projectId 项目id
     * @param certId    证书id
     * @param pageable  分页参数
     * @param params    查询参数
     * @return List
     */
    Page<ProjectReqVO> pageRelatedProjects(Long projectId, Long certId, PageRequest pageable, String params);

    void createOrUpdate(Long projectId, MultipartFile key, MultipartFile cert, ProjectCertificationCreateUpdateVO projectCertificationVO);

    boolean isNameUnique(Long projectId, String name);

    /**
     * 列出组织下所有项目中在数据库中没有权限关联关系的项目(不论当前数据库中是否跳过权限检查)
     *
     * @param projectId 项目id
     * @param certId    证书
     * @param params    查询参数
     * @return 组织下所有项目中在数据库中没有权限关联关系的项目
     */
    Page<ProjectReqVO> listNonRelatedMembers(Long projectId, Long certId, Long selectedProjectId, PageRequest pageable, String params);

    void deleteCert(Long projectId, Long certId);

    void deletePermissionOfProject(Long projectId, Long certId);


    Page<ProjectCertificationVO> pageCerts(Long projectId, PageRequest pageable,
                                           String params);

    ProjectCertificationVO queryCert(Long certId);

}
