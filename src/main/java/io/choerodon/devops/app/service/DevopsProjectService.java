package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.app.eventhandler.payload.ProjectPayload;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;

/**
 * Created by Sheep on 2019/7/15.
 */
public interface DevopsProjectService {
    void createProject(ProjectPayload projectPayload);

    boolean queryProjectGitlabGroupReady(Long projectId);

    DevopsProjectDTO baseQueryByGitlabAppGroupId(Integer appGroupId);

    DevopsProjectDTO baseQueryByProjectId(Long projectId);

    DevopsProjectDTO baseQueryByGitlabEnvGroupId(Integer envGroupId);

    void baseUpdate(DevopsProjectDTO devopsProjectDTO);

    /**
     * 分页查询与该项目在同一组织的项目列表（包含自身）
     *
     * @param projectId    项目id
     * @param pageRequest  分页参数
     * @param searchParams 查询参数
     * @return 项目信息
     */
    PageInfo<ProjectReqVO> pageProjects(Long projectId, PageRequest pageRequest, String searchParams);

    /**
     * 列出项目下的所有项目所有者和项目成员
     *
     * @param projectId 项目id
     * @return 项目所有者和项目成员
     */
    List<UserVO> listAllOwnerAndMembers(Long projectId);
}
