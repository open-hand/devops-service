package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import feign.FeignException;
import feign.RetryableException;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.gitlab.MemberVO;
import io.choerodon.devops.api.vo.gitlab.VariableDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectE;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.valueobject.DeployKey;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.application.valueobject.RepositoryFile;
import io.choerodon.devops.domain.application.valueobject.Variable;
import io.choerodon.devops.infra.util.GitUtil;
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
        ResponseEntity<List<ImpersonationTokenDO>> impersonationTokens;
        try {
            impersonationTokens = gitlabServiceClient
                    .listTokenByUserId(userId);
        } catch (FeignException e) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProject(gitlabProjectId, userId);
            throw new CommonException(e);
        }
        List<String> tokens = new ArrayList<>();
        impersonationTokens.getBody().stream().forEach(impersonationToken ->
                tokens.add(impersonationToken.getToken())
        );
        return tokens;
    }

    @Override
    public String createToken(Integer gitlabProjectId, String name, Integer userId) {
        ResponseEntity<ImpersonationTokenDO> impersonationToken;
        try {
            impersonationToken = gitlabServiceClient.createToken(userId);
        } catch (FeignException e) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProject(gitlabProjectId, userId);
            throw new CommonException(e);
        }
        return impersonationToken.getBody().getToken();
    }

    @Override
    public DevopsProjectE queryGroupByName(String groupName, Integer userId) {
        ResponseEntity<GroupDO> groupDO;
        try {
            groupDO = gitlabServiceClient.queryGroupByName(groupName, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        if (groupDO != null) {
            return ConvertHelper.convert(groupDO.getBody(), DevopsProjectE.class);
        } else {
            return null;
        }
    }

    @Override
    public DevopsProjectE createGroup(DevopsProjectE devopsProjectE, Integer userId) {
        ResponseEntity<GroupDO> groupDOResponseEntity;
        GroupDO groupDO = ConvertHelper.convert(devopsProjectE, GroupDO.class);
        try {
            groupDOResponseEntity = gitlabServiceClient.createGroup(groupDO, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return ConvertHelper.convert(groupDOResponseEntity.getBody(), DevopsProjectE.class);
    }

    @Override
    public void createFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        try {
            ResponseEntity<RepositoryFile> result = gitlabServiceClient
                    .createFile(projectId, path, content, commitMessage, userId);
            if (result.getBody().getFilePath() == null) {
                throw new CommonException("error.file.create");
            }
        } catch (RetryableException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }


    @Override
    public void createFile(Integer projectId, String path, String content, String commitMessage, Integer userId, String branch) {
        try {
            ResponseEntity<RepositoryFile> result = gitlabServiceClient
                    .createFile(projectId, path, content, commitMessage, userId, branch);
            if (result.getBody().getFilePath() == null) {
                throw new CommonException("error.file.create");
            }
        } catch (RetryableException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    @Override
    public void updateFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        try {
            ResponseEntity<RepositoryFile> result = gitlabServiceClient
                    .updateFile(projectId, path, content, commitMessage, userId);
            if (result.getBody().getFilePath() == null) {
                throw new CommonException("error.file.update");
            }
        } catch (RetryableException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(Integer projectId, String path, String commitMessage, Integer userId) {
        try {
            gitlabServiceClient.deleteFile(projectId, path, commitMessage, userId);
        } catch (FeignException e) {
            throw new CommonException("error.file.delete", e);
        }
    }

    @Override
    public void deleteDevOpsApp(String groupName, String projectName, Integer userId) {
        try {
            gitlabServiceClient.deleteProjectByProjectName(groupName, projectName, userId);
        } catch (FeignException e) {
            throw new CommonException("error.app.delete", e);
        }
    }

    @Override
    public Boolean getFile(Integer projectId, String branch, String filePath) {
        try {
            gitlabServiceClient.getFile(projectId, branch, filePath);
        } catch (FeignException e) {
            return false;
        }
        return true;
    }

    @Override
    public void createProtectBranch(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel,
                                    Integer userId) {
        try {
            gitlabServiceClient.createProtectedBranches(
                    projectId, name, mergeAccessLevel, pushAccessLevel, userId);
        } catch (FeignException e) {
            throw new CommonException("error.branch.create", e);
        }
    }

    @Override
    public void deleteProject(Integer projectId, Integer userId) {
        try {
            gitlabServiceClient.deleteProject(projectId, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public void updateGroup(Integer projectId, Integer userId, GroupDO groupDO) {
        gitlabServiceClient.updateGroup(projectId, userId, groupDO);
    }


    @Override
    public ProjectHook createWebHook(Integer projectId, Integer userId, ProjectHook projectHook) {
        try {
            return gitlabServiceClient.createProjectHook(projectId, userId, projectHook).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.projecthook.create", e);

        }
    }

    @Override
    public ProjectHook updateWebHook(Integer projectId, Integer hookId, Integer userId) {
        ResponseEntity<ProjectHook> projectHookResponseEntity;
        try {
            projectHookResponseEntity = gitlabServiceClient
                    .updateProjectHook(projectId, hookId, userId);
        } catch (FeignException e) {
            throw new CommonException(e.getMessage(), e);
        }
        return projectHookResponseEntity.getBody();
    }

    @Override
    public GitlabProjectDO createProject(Integer groupId, String projectName, Integer userId, boolean visibility) {
        try {
            return gitlabServiceClient
                    .createProject(groupId, projectName, userId, visibility).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.gitlab.project.create", e);

        }
    }

    @Override
    public GitlabProjectDO getProjectById(Integer projectId) {
        try {
            return gitlabServiceClient.getProjectById(projectId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public GitlabProjectDO getProjectByName(String groupName, String projectName, Integer userId) {
        try {
            return gitlabServiceClient.getProjectByName(userId, groupName, projectName).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public List<ProjectHook> getHooks(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.getProjectHook(projectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public List<Variable> getVariable(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.getVariable(projectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public List<DeployKey> getDeployKeys(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.getDeploykeys(projectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public void createDeployKey(Integer projectId, String title, String key, boolean canPush, Integer userId) {
        try {
            gitlabServiceClient.createDeploykey(projectId, title, key, canPush, userId);
        } catch (FeignException e) {
            throw new CommonException("error.deploykey.create", e);
        }
    }

    @Override
    public void addMemberIntoProject(Integer projectId, MemberVO memberDTO) {
        try {
            gitlabServiceClient.addMemberIntoProject(projectId, memberDTO);
        } catch (Exception e) {
            throw new CommonException("error.member.add", e);
        }
    }

    @Override
    public void updateMemberIntoProject(Integer projectId, List<MemberVO> list) {
        try {
            gitlabServiceClient.updateMemberIntoProject(projectId, list);
        } catch (Exception e) {
            throw new CommonException("error.member.update", e);
        }
    }

    @Override
    public void removeMemberFromProject(Integer groupId, Integer userId) {
        try {
            gitlabServiceClient.removeMemberFromProject(groupId, userId);
        } catch (Exception e) {
            throw new CommonException("error.member.remove", e);
        }
    }

    @Override
    public List<GitlabProjectDO> getProjectsByUserId(Integer userId) {
        try {
            return gitlabServiceClient.getProjectsByUserId(userId).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.project.get.by.userId", e);
        }
    }

    @Override
    public MergeRequestDO createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String title, String description, Integer userId) {
        try {
            return gitlabServiceClient.createMergeRequest(projectId, sourceBranch, targetBranch, title, description, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public void acceptMergeRequest(Integer projectId, Integer mergeRequestId, String mergeCommitMessage, Boolean shouldRemoveSourceBranch, Boolean mergeWhenPipelineSucceeds, Integer userId) {
        gitlabServiceClient.acceptMergeRequest(projectId, mergeRequestId, mergeCommitMessage, shouldRemoveSourceBranch, mergeWhenPipelineSucceeds, userId);
    }
}
