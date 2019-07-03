package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.gitlab.MemberDTO;
import io.choerodon.devops.api.dto.gitlab.VariableDTO;
import io.choerodon.devops.domain.application.entity.DevopsProjectE;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.valueobject.DeployKey;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.application.valueobject.Variable;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO;
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO;
import io.choerodon.devops.infra.dataobject.gitlab.ImpersonationTokenDO;
import io.choerodon.devops.infra.dataobject.gitlab.MergeRequestDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabRepositoryImpl implements GitlabRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabRepositoryImpl.class);

    private GitlabServiceClient gitlabServiceClient;
    private GitUtil gitUtil;

    public GitlabRepositoryImpl(GitlabServiceClient gitlabServiceClient, GitUtil gitUtil) {
        this.gitlabServiceClient = gitlabServiceClient;
        this.gitUtil = gitUtil;
    }

    @Override
    public void addVariable(Integer gitlabProjectId, String key, String value, Boolean protecteds, Integer userId) {
        gitlabServiceClient.addVariable(gitlabProjectId, key, value, protecteds, userId);
    }

    @Override
    public void batchAddVariable(Integer gitlabProjectId, Integer userId, List<VariableDTO> variableDTOS) {
        gitlabServiceClient.batchAddVariable(gitlabProjectId, userId, variableDTOS);
    }

    @Override
    public List<String> listTokenByUserId(Integer gitlabProjectId, String name, Integer userId) {

        ResponseEntity<List<ImpersonationTokenDO>> impersonationTokens = gitlabServiceClient
                .listTokenByUserId(userId);
        if (impersonationTokens.getStatusCodeValue() == 500) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProject(gitlabProjectId, userId);
            throw new CommonException("error.token.query");
        }
        List<String> tokens = new ArrayList<>();
        impersonationTokens.getBody().stream().forEach(impersonationToken ->
                tokens.add(impersonationToken.getToken())
        );
        return tokens;
    }

    @Override
    public String createToken(Integer gitlabProjectId, String name, Integer userId) {
        ResponseEntity<ImpersonationTokenDO> impersonationToken = gitlabServiceClient.createToken(userId);
        if (impersonationToken.getStatusCodeValue() == 500) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProject(gitlabProjectId, userId);
            throw new CommonException("error.token.create");
        }
        return impersonationToken.getBody().getToken();
    }

    @Override
    public DevopsProjectE queryGroupByName(String groupName, Integer userId) {

        ResponseEntity<GroupDO> groupDO = gitlabServiceClient.queryGroupByName(groupName, userId);
        return ConvertHelper.convert(groupDO.getBody(), DevopsProjectE.class);

    }

    @Override
    public DevopsProjectE createGroup(DevopsProjectE devopsProjectE, Integer userId) {
        GroupDO groupDO = ConvertHelper.convert(devopsProjectE, GroupDO.class);
        ResponseEntity<GroupDO> groupDOResponseEntity = gitlabServiceClient.createGroup(groupDO, userId);
        return ConvertHelper.convert(groupDOResponseEntity.getBody(), DevopsProjectE.class);
    }

    @Override
    public void createFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        gitlabServiceClient
                .createFile(projectId, path, content, commitMessage, userId);
    }


    @Override
    public void createFile(Integer projectId, String path, String content, String commitMessage, Integer userId, String branch) {
        gitlabServiceClient
                .createFile(projectId, path, content, commitMessage, userId, branch);
    }

    @Override
    public void updateFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        gitlabServiceClient
                .updateFile(projectId, path, content, commitMessage, userId);
    }

    @Override
    public void deleteFile(Integer projectId, String path, String commitMessage, Integer userId) {
        gitlabServiceClient.deleteFile(projectId, path, commitMessage, userId);
    }

    @Override
    public void deleteDevOpsApp(String groupName, String projectName, Integer userId) {
        gitlabServiceClient.deleteProjectByProjectName(groupName, projectName, userId);
    }

    @Override
    public Boolean getFile(Integer projectId, String branch, String filePath) {
        ResponseEntity responseEntity = gitlabServiceClient.getFile(projectId, branch, filePath);
        return responseEntity.getStatusCodeValue() != 500;
    }

    @Override
    public void createProtectBranch(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel,
                                    Integer userId) {
            gitlabServiceClient.createProtectedBranches(
                    projectId, name, mergeAccessLevel, pushAccessLevel, userId);
    }

    @Override
    public void deleteProject(Integer projectId, Integer userId) {
        gitlabServiceClient.deleteProject(projectId, userId);
    }

    @Override
    public void updateGroup(Integer projectId, Integer userId, GroupDO groupDO) {
        gitlabServiceClient.updateGroup(projectId, userId, groupDO);
    }


    @Override
    public ProjectHook createWebHook(Integer projectId, Integer userId, ProjectHook projectHook) {
            return gitlabServiceClient.createProjectHook(projectId, userId, projectHook).getBody();
    }

    @Override
    public ProjectHook updateWebHook(Integer projectId, Integer hookId, Integer userId) {
        ResponseEntity<ProjectHook> projectHookResponseEntity = gitlabServiceClient
                    .updateProjectHook(projectId, hookId, userId);
        return projectHookResponseEntity.getBody();
    }

    @Override
    public GitlabProjectDO createProject(Integer groupId, String projectName, Integer userId, boolean visibility) {
        return gitlabServiceClient
                .createProject(groupId, projectName, userId, visibility).getBody();
    }

    @Override
    public GitlabProjectDO getProjectById(Integer projectId) {
        return gitlabServiceClient.getProjectById(projectId).getBody();
    }

    @Override
    public GitlabProjectDO getProjectByName(String groupName, String projectName, Integer userId) {
        return gitlabServiceClient.getProjectByName(userId, groupName, projectName).getBody();
    }

    @Override
    public List<ProjectHook> getHooks(Integer projectId, Integer userId) {
            return gitlabServiceClient.getProjectHook(projectId, userId).getBody();
    }

    @Override
    public List<Variable> getVariable(Integer projectId, Integer userId) {
        return gitlabServiceClient.getVariable(projectId, userId).getBody();
    }

    @Override
    public List<DeployKey> getDeployKeys(Integer projectId, Integer userId) {
        return gitlabServiceClient.getDeploykeys(projectId, userId).getBody();
    }

    @Override
    public void createDeployKey(Integer projectId, String title, String key, boolean canPush, Integer userId) {
        gitlabServiceClient.createDeploykey(projectId, title, key, canPush, userId);

    }

    @Override
    public void addMemberIntoProject(Integer projectId, MemberDTO memberDTO) {
            gitlabServiceClient.addMemberIntoProject(projectId, memberDTO);
    }

    @Override
    public void updateMemberIntoProject(Integer projectId, List<MemberDTO> list) {
        gitlabServiceClient.updateMemberIntoProject(projectId, list);
    }

    @Override
    public void removeMemberFromProject(Integer groupId, Integer userId) {
            gitlabServiceClient.removeMemberFromProject(groupId, userId);
    }

    @Override
    public List<GitlabProjectDO> getProjectsByUserId(Integer userId) {
            return gitlabServiceClient.getProjectsByUserId(userId).getBody();
    }

    @Override
    public MergeRequestDO createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String title, String description, Integer userId) {
            return gitlabServiceClient.createMergeRequest(projectId, sourceBranch, targetBranch, title, description, userId).getBody();
    }

    @Override
    public void acceptMergeRequest(Integer projectId, Integer mergeRequestId, String mergeCommitMessage, Boolean shouldRemoveSourceBranch, Boolean mergeWhenPipelineSucceeds, Integer userId) {
        gitlabServiceClient.acceptMergeRequest(projectId, mergeRequestId, mergeCommitMessage, shouldRemoveSourceBranch, mergeWhenPipelineSucceeds, userId);
    }
}
