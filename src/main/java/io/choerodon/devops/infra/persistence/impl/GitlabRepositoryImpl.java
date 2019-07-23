package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import feign.FeignException;
import feign.RetryableException;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsProjectVO;
import io.choerodon.devops.infra.dto.gitlab.DeployKeyDTO;
import io.choerodon.devops.infra.dto.gitlab.ProjectHookDTO;
import io.choerodon.devops.domain.application.valueobject.RepositoryFile;
import io.choerodon.devops.infra.dto.gitlab.VariableDTO;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.dataobject.gitlab.GroupDO;
import io.choerodon.devops.infra.dataobject.gitlab.ImpersonationTokenDO;
import io.choerodon.devops.infra.dataobject.gitlab.MergeRequestDTO;
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
        gitlabServiceClient.addProjectVariable(gitlabProjectId, key, value, protecteds, userId);
    }

    @Override
    public void batchAddVariable(Integer gitlabProjectId, Integer userId, List<io.choerodon.devops.api.vo.gitlab.VariableDTO> variableDTODTOS) {
        gitlabServiceClient.batchAddProjectVariable(gitlabProjectId, userId, variableDTODTOS);
    }

    @Override
    public List<String> listTokenByUserId(Integer gitlabProjectId, String name, Integer userId) {
        ResponseEntity<List<ImpersonationTokenDO>> impersonationTokens;
        try {
            impersonationTokens = gitlabServiceClient
                    .listProjectToken(userId);
        } catch (FeignException e) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProjectById(gitlabProjectId, userId);
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
            impersonationToken = gitlabServiceClient.createProjectToken(userId);
        } catch (FeignException e) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProjectById(gitlabProjectId, userId);
            throw new CommonException(e);
        }
        return impersonationToken.getBody().getToken();
    }

    @Override
    public DevopsProjectVO queryGroupByName(String groupName, Integer userId) {
        ResponseEntity<GroupDO> groupDO;
        try {
            groupDO = gitlabServiceClient.queryGroupByName(groupName, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        if (groupDO != null) {
            return ConvertHelper.convert(groupDO.getBody(), DevopsProjectVO.class);
        } else {
            return null;
        }
    }

    @Override
    public DevopsProjectVO createGroup(DevopsProjectVO devopsProjectE, Integer userId) {
        ResponseEntity<GroupDO> groupDOResponseEntity;
        GroupDO groupDO = ConvertHelper.convert(devopsProjectE, GroupDO.class);
        try {
            groupDOResponseEntity = gitlabServiceClient.createGroup(groupDO, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return ConvertHelper.convert(groupDOResponseEntity.getBody(), DevopsProjectVO.class);
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
            gitlabServiceClient.deleteProjectByName(groupName, projectName, userId);
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
            gitlabServiceClient.createProtectedBranch(
                    projectId, name, mergeAccessLevel, pushAccessLevel, userId);
        } catch (FeignException e) {
            throw new CommonException("error.branch.create", e);
        }
    }

    @Override
    public void deleteProject(Integer projectId, Integer userId) {
        try {
            gitlabServiceClient.deleteProjectById(projectId, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public void updateGroup(Integer projectId, Integer userId, GroupDO groupDO) {
        gitlabServiceClient.updateGroup(projectId, userId, groupDO);
    }


    @Override
    public ProjectHookDTO createWebHook(Integer projectId, Integer userId, ProjectHookDTO projectHookDTO) {
        try {
            return gitlabServiceClient.createProjectHook(projectId, userId, projectHookDTO).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.projecthook.create", e);

        }
    }

    @Override
    public ProjectHookDTO updateWebHook(Integer projectId, Integer hookId, Integer userId) {
        ResponseEntity<ProjectHookDTO> projectHookResponseEntity;
        try {
            projectHookResponseEntity = gitlabServiceClient
                    .updateProjectHook(projectId, hookId, userId);
        } catch (FeignException e) {
            throw new CommonException(e.getMessage(), e);
        }
        return projectHookResponseEntity.getBody();
    }

    @Override
    public GitlabProjectDTO createProject(Integer groupId, String projectName, Integer userId, boolean visibility) {
        try {
            return gitlabServiceClient
                    .createProject(groupId, projectName, userId, visibility).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.gitlab.project.create", e);

        }
    }

    @Override
    public GitlabProjectDTO getProjectById(Integer projectId) {
        try {
            return gitlabServiceClient.queryProjectById(projectId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public GitlabProjectDTO getProjectByName(String groupName, String projectName, Integer userId) {
        try {
            return gitlabServiceClient.queryProjectByName(userId, groupName, projectName).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public List<ProjectHookDTO> getHooks(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.listProjectHook(projectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public List<VariableDTO> getVariable(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.listVariable(projectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public List<DeployKeyDTO> getDeployKeys(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.listDeploykey(projectId, userId).getBody();
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
            gitlabServiceClient.createProjectMember(projectId, memberDTO);
        } catch (Exception e) {
            throw new CommonException("error.member.add", e);
        }
    }

    @Override
    public void updateMemberIntoProject(Integer projectId, List<MemberVO> list) {
        try {
            gitlabServiceClient.updateProjectMember(projectId, list);
        } catch (Exception e) {
            throw new CommonException("error.member.update", e);
        }
    }

    @Override
    public void removeMemberFromProject(Integer groupId, Integer userId) {
        try {
            gitlabServiceClient.deleteProjectMember(groupId, userId);
        } catch (Exception e) {
            throw new CommonException("error.member.remove", e);
        }
    }

    @Override
    public List<GitlabProjectDTO> getProjectsByUserId(Integer userId) {
        try {
            return gitlabServiceClient.listProjectByUser(userId).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.project.get.by.userId", e);
        }
    }

    @Override
    public MergeRequestDTO createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String title, String description, Integer userId) {
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
