package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.gitlab.MemberVO;
import io.choerodon.devops.api.vo.gitlab.VariableDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectVO;
import io.choerodon.devops.api.vo.iam.entity.gitlab.CommitE;
import io.choerodon.devops.domain.application.valueobject.DeployKey;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.application.valueobject.Variable;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO;
import io.choerodon.devops.infra.dataobject.gitlab.MergeRequestDTO;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:45 2019/7/11
 * Description:
 */
public interface GitLabService {

    void addVariable(Integer gitlabProjectId, String key, String value, Boolean protecteds, Integer userId);

    void batchAddVariable(Integer gitlabProjectId, Integer userId, List<VariableDTO> variableDTOS);

    List<String> listTokenByUserId(Integer gitlabProjectId, String name, Integer userId);

    String createToken(Integer gitlabProjectId, String name, Integer userId);

    DevopsProjectVO queryGroupByName(String groupName, Integer userId);

    DevopsProjectVO createGroup(DevopsProjectVO gitlabGroupE, Integer userId);

    void createFile(Integer projectId, String path, String content, String commitMessage, Integer userId);

    void createFile(Integer projectId, String path, String content, String commitMessage, Integer userId, String branch);


    void updateFile(Integer projectId, String path, String content, String commitMessage, Integer userId);

    void deleteFile(Integer projectId, String path, String commitMessage, Integer userId);

    void deleteDevOpsApp(String groupName, String projectName, Integer userId);

    void createProtectBranch(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel,
                             Integer userId);

    void deleteProject(Integer projectId, Integer userId);

    void updateGroup(Integer projectId, Integer userId, GroupDO groupDO);

    ProjectHook createWebHook(Integer projectId, Integer userId, ProjectHook projectHook);

    GitlabProjectDTO createProject(Integer groupId, String projectName, Integer userId, boolean visibility);

    void createDeployKey(Integer projectId, String title, String key, boolean canPush, Integer userId);

    Boolean getFile(Integer projectId, String branch, String filePath);

    ProjectHook updateWebHook(Integer projectId, Integer hookId, Integer userId);

    GitlabProjectDTO getProjectById(Integer projectId);

    GitlabProjectDTO getProjectByName(String groupName, String projectName, Integer userId);

    List<ProjectHook> getHooks(Integer projectId, Integer userId);

    List<Variable> getVariable(Integer projectId, Integer userId);

    List<DeployKey> getDeployKeys(Integer projectId, Integer userId);

    /**
     * 将成员添加到gitlab项目，应该先检查成员是否存在，否则会报成员已存在的异常
     */
    void addMemberIntoProject(Integer projectId, MemberVO memberDTO);

    void updateMemberIntoProject(Integer projectId, List<MemberVO> list);

    void removeMemberFromProject(Integer projectId, Integer userId);

    List<GitlabProjectDTO> getProjectsByUserId(Integer userId);

    MergeRequestDTO createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String title, String description, Integer userId);

    void acceptMergeRequest(Integer projectId, Integer mergeRequestId, String mergeCommitMessage, Boolean shouldRemoveSourceBranch, Boolean mergeWhenPipelineSucceeds, Integer userId);


}
