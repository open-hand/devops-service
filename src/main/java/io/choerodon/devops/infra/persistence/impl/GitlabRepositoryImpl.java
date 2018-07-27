package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabGroupE;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.infra.common.util.GitUtil;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO;
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO;
import io.choerodon.devops.infra.dataobject.gitlab.ImpersonationTokenDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;

/**
 * Created by younger on 2018/3/29.
 */
@Component
public class GitlabRepositoryImpl implements GitlabRepository {

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
    public List<String> listTokenByUserId(Integer gitlabProjectId, String name, Integer userId) {
        ResponseEntity<List<ImpersonationTokenDO>> impersonationTokens = gitlabServiceClient
                .listTokenByUserId(userId);
        if (!impersonationTokens.getStatusCode().is2xxSuccessful()) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProject(gitlabProjectId, userId);
        }
        List<String> tokens = new ArrayList<>();
        impersonationTokens.getBody().parallelStream().forEach(impersonationToken ->
                tokens.add(impersonationToken.getToken())
        );
        return tokens;
    }

    @Override
    public String createToken(Integer gitlabProjectId, String name, Integer userId) {
        ResponseEntity<ImpersonationTokenDO> impersonationToken = gitlabServiceClient.createToken(userId);
        if (!impersonationToken.getStatusCode().is2xxSuccessful()) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProject(gitlabProjectId, userId);
        }
        return impersonationToken.getBody().getToken();
    }

    @Override
    public GitlabGroupE queryGroupByName(String groupName, Integer userId) {
        ResponseEntity<GroupDO> groupDO = gitlabServiceClient.queryGroupByName(groupName, userId);
        if (groupDO != null) {
            return ConvertHelper.convert(groupDO.getBody(), GitlabGroupE.class);
        } else {
            return null;
        }
    }

    @Override
    public GitlabGroupE createGroup(GitlabGroupE gitlabGroupE, Integer userId) {
        ResponseEntity<GroupDO> groupDO = gitlabServiceClient.createGroup(ConvertHelper.convert(
                gitlabGroupE, GroupDO.class), userId);
        return ConvertHelper.convert(groupDO.getBody(), GitlabGroupE.class);
    }

    @Override
    public Boolean createFile(Integer projectId, Integer userId) {
        ResponseEntity<Boolean> result = gitlabServiceClient.createFile(projectId, userId);
        if (result.getStatusCode().is2xxSuccessful()) {
            return result.getBody();
        } else {
            throw new CommonException("error.file.create");
        }
    }

    @Override
    public void createProtectBranch(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel, Integer userId) {
        ResponseEntity<Map<String, Object>> branch = gitlabServiceClient.createProtectedBranches(
                projectId, name, mergeAccessLevel, pushAccessLevel, userId);
        if (!branch.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.branch.create");
        }
    }

    @Override
    public void deleteProject(Integer projectId, Integer userId) {
        gitlabServiceClient.deleteProject(projectId, userId);
    }


    @Override
    public String updateProject(Integer projectId, Integer userId) {
        return gitlabServiceClient.updateProject(projectId, userId).getBody().getDefaultBranch();
    }

    @Override
    public ProjectHook createWebHook(Integer projectId, Integer userId, ProjectHook projectHook) {
        ResponseEntity<ProjectHook> projectHookResponseEntity = gitlabServiceClient.createProjectHook(projectId, userId, projectHook);
        if (!projectHookResponseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
            throw new CommonException("error.projecthook.create");
        }
        return projectHookResponseEntity.getBody();
    }

    @Override
    public GitlabProjectDO createProject(Integer groupId, String projectName, Integer userId, boolean visibility) {
        ResponseEntity<GitlabProjectDO> responseEntity = gitlabServiceClient.createProject(groupId, projectName, userId, visibility);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CommonException("error.gitlab.project.create");
        }
        return responseEntity.getBody();
    }

}
