package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.api.vo.sonar.SonarInfo;
import io.choerodon.devops.app.eventhandler.payload.ProjectPayload;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.GitlabProjectSimple;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface DevopsProjectService {
    void createProject(ProjectPayload projectPayload);

    boolean queryProjectGitlabGroupReady(Long projectId);

    DevopsProjectDTO baseQueryByGitlabAppGroupId(Integer appGroupId);

    DevopsProjectDTO baseQueryByGitlabGroupId(Integer groupId);

    /**
     * 通过项目id查询纪录, 会对查询结果进行校验
     *
     * @param projectId 项目id
     * @return 项目信息
     */
    DevopsProjectDTO baseQueryByProjectId(Long projectId);

    /**
     * 根据项目id查询纪录, 不对查询结果进行校验
     *
     * @param projectId 项目id
     * @return 项目信息
     */
    @Nullable
    DevopsProjectDTO queryWithoutCheck(Long projectId);

    DevopsProjectDTO baseQueryByGitlabEnvGroupId(Integer envGroupId);

    void baseUpdate(DevopsProjectDTO devopsProjectDTO);
    /**
     * 分页查询与该项目在同一组织的项目列表（包含自身）
     *
     * @param projectId    项目id
     * @param pageable     分页参数
     * @param searchParams 查询参数
     * @return 项目信息
     */
    Page<ProjectReqVO> pageProjects(Long projectId, PageRequest pageable, String searchParams);

    /**
     * 分页查询组织下项目列表
     *
     * @param organizationId 组织id
     * @param pageable       分页参数
     * @param searchParams   查询参数
     * @return 项目信息
     */
    Page<ProjectReqVO> pageProjectsByOrganizationId(Long organizationId, PageRequest pageable, String searchParams);

    /**
     * 列出项目下的所有项目所有者和项目成员
     *
     * @param projectId 项目id
     * @return 项目所有者和项目成员
     */
    Page<UserVO> listAllOwnerAndMembers(Long projectId, PageRequest pageable, String params);

    List<DevopsProjectDTO> listAll();

    List<GitlabProjectSimple> queryGitlabGroups(List<Long> projectIds);

    List<DevopsProjectDTO> listExistGroup(Set<Integer> groupIds);

    Long queryDevopsProject(Long projectId);

    SonarInfo querySonarInfo(Long projectId);
}
