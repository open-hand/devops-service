package io.choerodon.devops.infra.feign.operator;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.RepositoryFileDTO;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;


/**
 * Created by Sheep on 2019/7/11.
 */

@Component
public class GitlabServiceClientOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabServiceClientOperator.class);

    @Autowired
    private GitlabServiceClient gitlabServiceClient;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private GitUtil gitUtil;


    public GitLabUserDTO createUser(String password, Integer projectsLimit, GitlabUserReqDTO userReqDTO) {
        ResponseEntity<GitLabUserDTO> userDOResponseEntity;
        try {
            userDOResponseEntity = gitlabServiceClient.createUser(
                    password, projectsLimit, userReqDTO);
        } catch (FeignException e) {
            LOGGER.info("error.gitlab.user.create");
            throw new CommonException(e);
        }
        return userDOResponseEntity.getBody();
    }

    public GitLabUserDTO queryUserByUserName(String userName) {
        ResponseEntity<GitLabUserDTO> userDTOResponseEntity;
        try {
            userDTOResponseEntity = gitlabServiceClient.queryUserByUserName(userName);
        } catch (FeignException e) {
            return null;
        }
        return userDTOResponseEntity.getBody();
    }

    public GitLabUserDTO updateUser(Integer userId, Integer projectsLimit, GitlabUserReqDTO userReqDTO) {
        ResponseEntity<GitLabUserDTO> userDTOResponseEntity;
        try {
            userDTOResponseEntity = gitlabServiceClient.updateGitLabUser(
                    userId, projectsLimit, userReqDTO);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return userDTOResponseEntity.getBody();
    }

    public void enableUser(Integer userId) {

        try {
            gitlabServiceClient.enableUser(userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public void disableUser(Integer userId) {
        try {
            gitlabServiceClient.disableUser(userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public GitLabUserDTO queryUserById(Integer userId) {
        ResponseEntity<GitLabUserDTO> userDTOResponseEntity;
        try {
            userDTOResponseEntity = gitlabServiceClient.queryUserById(userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return userDTOResponseEntity.getBody();
    }

    public Boolean checkEmail(String email) {
        return gitlabServiceClient.checkEmail(email).getBody();
    }


    public MemberDTO queryGroupMember(Integer groupId, Integer userId) {
        MemberDTO memberDTO = gitlabServiceClient.queryGroupMember(
                groupId, userId).getBody();
        if (memberDTO.getId() == null) {
            return null;
        }
        return memberDTO;
    }

    public void deleteGroupMember(Integer groupId, Integer userId) {
        gitlabServiceClient.deleteMember(groupId, userId);
    }

    public int createGroupMember(Integer groupId, MemberDTO memberDTO) {
        return gitlabServiceClient.createGroupMember(groupId, memberDTO).getStatusCodeValue();
    }


    public void updateGroupMember(Integer groupId, MemberDTO memberDTO) {
        gitlabServiceClient.updateGroupMember(groupId, memberDTO);
    }


    public void createVariable(Integer gitlabProjectId, String key, String value, Boolean protecteds, Integer userId) {
        gitlabServiceClient.addProjectVariable(gitlabProjectId, key, value, protecteds, userId);
    }

    public void batchAddProjectVariable(Integer gitlabProjectId, Integer userId, List<VariableDTO> variableDTODTOS) {
        gitlabServiceClient.batchAddProjectVariable(gitlabProjectId, userId, variableDTODTOS);
    }

    public List<String> listProjectToken(Integer gitlabProjectId, String name, Integer userId) {
        ResponseEntity<List<ImpersonationTokenDTO>> impersonationTokens;
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

    public String createProjectToken(Integer gitlabProjectId, String name, Integer userId) {
        ResponseEntity<ImpersonationTokenDTO> impersonationToken;
        try {
            impersonationToken = gitlabServiceClient.createProjectToken(userId);
        } catch (FeignException e) {
            gitUtil.deleteWorkingDirectory(name);
            gitlabServiceClient.deleteProjectById(gitlabProjectId, userId);
            throw new CommonException(e);
        }
        return impersonationToken.getBody().getToken();
    }

    /**
     * 从gitlab项目创建access token
     *
     * @param gitlabProjectId gitlab 项目id
     * @param userId          用户id
     * @return access token
     */
    @Nullable
    public String createProjectToken(Integer userId) {
        ResponseEntity<ImpersonationTokenDTO> impersonationToken;
        try {
            impersonationToken = gitlabServiceClient.createProjectToken(userId);
        } catch (FeignException e) {
            return null;
        }
        return impersonationToken.getBody().getToken();
    }

    public GroupDTO queryGroupByName(String groupName, Integer userId) {
        ResponseEntity<GroupDTO> groupDTOResponseEntity;
        try {
            groupDTOResponseEntity = gitlabServiceClient.queryGroupByName(groupName, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        if (groupDTOResponseEntity != null) {
            return groupDTOResponseEntity.getBody();
        } else {
            return null;
        }
    }

    public GroupDTO createGroup(GroupDTO groupDTO, Integer userId) {
        ResponseEntity<GroupDTO> groupDTOResponseEntity;
        try {
            groupDTOResponseEntity = gitlabServiceClient.createGroup(groupDTO, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return groupDTOResponseEntity.getBody();
    }

    public void createFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        try {
            ResponseEntity<RepositoryFileDTO> result = gitlabServiceClient
                    .createFile(projectId, path, content, commitMessage, userId);
            if (result.getBody().getFilePath() == null) {
                throw new CommonException("error.file.create");
            }
        } catch (RetryableException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }


    public void createFile(Integer projectId, String path, String content, String commitMessage, Integer userId, String branch) {
        try {
            ResponseEntity<RepositoryFileDTO> result = gitlabServiceClient
                    .createFile(projectId, path, content, commitMessage, userId, branch);
            if (result.getBody().getFilePath() == null) {
                throw new CommonException("error.file.create");
            }
        } catch (RetryableException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    public void updateFile(Integer projectId, String path, String content, String commitMessage, Integer userId) {
        try {
            ResponseEntity<RepositoryFileDTO> result = gitlabServiceClient
                    .updateFile(projectId, path, content, commitMessage, userId);
            if (result.getBody().getFilePath() == null) {
                throw new CommonException("error.file.update");
            }
        } catch (RetryableException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    public void deleteFile(Integer projectId, String path, String commitMessage, Integer userId) {
        try {
            gitlabServiceClient.deleteFile(projectId, path, commitMessage, userId);
        } catch (FeignException e) {
            throw new CommonException("error.file.delete", e);
        }
    }

    public void deleteProjectByName(String groupName, String projectName, Integer userId) {
        try {
            gitlabServiceClient.deleteProjectByName(groupName, projectName, userId);
        } catch (FeignException e) {
            throw new CommonException("error.app.delete", e);
        }
    }

    public Boolean getFile(Integer projectId, String branch, String filePath) {
        try {
            gitlabServiceClient.getFile(projectId, branch, filePath);
        } catch (FeignException e) {
            return false;
        }
        return true;
    }

    public void createProtectBranch(Integer projectId, String name, String mergeAccessLevel, String pushAccessLevel,
                                    Integer userId) {
        try {
            gitlabServiceClient.createProtectedBranch(
                    projectId, name, mergeAccessLevel, pushAccessLevel, userId);
        } catch (FeignException e) {
            throw new CommonException("error.branch.create", e);
        }
    }

    public void deleteProjectById(Integer projectId, Integer userId) {
        try {
            gitlabServiceClient.deleteProjectById(projectId, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public void updateGroup(Integer projectId, Integer userId, GroupDTO groupDTO) {
        gitlabServiceClient.updateGroup(projectId, userId, groupDTO);
    }


    public ProjectHookDTO createWebHook(Integer projectId, Integer userId, ProjectHookDTO projectHookDTO) {
        try {
            return gitlabServiceClient.createProjectHook(projectId, userId, projectHookDTO).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.projecthook.create", e);

        }
    }

    public ProjectHookDTO updateProjectHook(Integer projectId, Integer hookId, Integer userId) {
        ResponseEntity<ProjectHookDTO> projectHookResponseEntity;
        try {
            projectHookResponseEntity = gitlabServiceClient
                    .updateProjectHook(projectId, hookId, userId);
        } catch (FeignException e) {
            throw new CommonException(e.getMessage(), e);
        }
        return projectHookResponseEntity.getBody();
    }

    public GitlabProjectDTO createProject(Integer groupId, String projectName, Integer userId, boolean visibility) {
        try {
            return gitlabServiceClient
                    .createProject(groupId, projectName, userId, visibility).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.gitlab.project.create", e);

        }
    }

    public GitlabProjectDTO queryProjectById(Integer projectId) {
        try {
            return gitlabServiceClient.queryProjectById(projectId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public GitlabProjectDTO queryProjectByName(String groupName, String projectName, Integer userId) {
        try {
            return gitlabServiceClient.queryProjectByName(userId, groupName, projectName).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public List<ProjectHookDTO> listProjectHook(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.listProjectHook(projectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public List<VariableDTO> listVariable(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.listVariable(projectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public List<DeployKeyDTO> listDeployKey(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.listDeploykey(projectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public void createDeployKey(Integer projectId, String title, String key, boolean canPush, Integer userId) {
        try {
            gitlabServiceClient.createDeploykey(projectId, title, key, canPush, userId);
        } catch (FeignException e) {
            throw new CommonException("error.deploykey.create", e);
        }
    }

    public void createProjectMember(Integer projectId, MemberDTO memberDTO) {
        try {
            gitlabServiceClient.createProjectMember(projectId, memberDTO);
        } catch (Exception e) {
            throw new CommonException("error.member.add", e);
        }
    }

    public void updateProjectMember(Integer projectId, List<MemberDTO> memberDTOS) {
        try {
            gitlabServiceClient.updateProjectMember(projectId, memberDTOS);
        } catch (Exception e) {
            throw new CommonException("error.member.update", e);
        }
    }

    public void deleteProjectMember(Integer groupId, Integer userId) {
        try {
            gitlabServiceClient.deleteProjectMember(groupId, userId);
        } catch (Exception e) {
            throw new CommonException("error.member.remove", e);
        }
    }


    public List<GitlabProjectDTO> listProjectByUser(Integer userId) {
        try {
            return gitlabServiceClient.listProjectByUser(userId).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.project.get.by.userId", e);
        }
    }


    public MergeRequestDTO createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String title, String description, Integer userId) {
        try {
            return gitlabServiceClient.createMergeRequest(projectId, sourceBranch, targetBranch, title, description, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }


    public void acceptMergeRequest(Integer projectId, Integer mergeRequestId, String mergeCommitMessage, Boolean shouldRemoveSourceBranch, Boolean mergeWhenPipelineSucceeds, Integer userId) {
        gitlabServiceClient.acceptMergeRequest(projectId, mergeRequestId, mergeCommitMessage, shouldRemoveSourceBranch, mergeWhenPipelineSucceeds, userId);
    }

    public void createTag(Integer gitLabProjectId, String tag, String ref, String msg, String releaseNotes, Integer userId) {
        try {
            if (msg == null) {
                msg = "No ReleaseNote";
            }
            if (releaseNotes == null) {
                releaseNotes = "No ReleaseNote";
            }
            gitlabServiceClient.createTag(gitLabProjectId, tag, ref, msg, releaseNotes, userId);
        } catch (FeignException e) {
            throw new CommonException("create gitlab tag failed: " + e.getMessage(), e);
        }
    }

    public TagDTO updateTag(Integer gitLabProjectId, String tag, String releaseNotes, Integer userId) {
        try {
            if (releaseNotes == null) {
                releaseNotes = "";
            }
            return gitlabServiceClient.updateTag(gitLabProjectId, tag, releaseNotes, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException("update gitlab tag failed: " + e.getMessage(), e);
        }
    }

    public void deleteTag(Integer gitLabProjectId, String tag, Integer userId) {
        try {
            gitlabServiceClient.deleteTag(gitLabProjectId, tag, userId);
        } catch (FeignException e) {
            throw new CommonException("delete gitlab tag failed: " + e.getMessage(), e);
        }
    }


    public BranchDTO createBranch(Integer projectId, String branchName, String baseBranch, Integer userId) {
        ResponseEntity<BranchDTO> responseEntity;
        try {
            responseEntity =
                    gitlabServiceClient.createBranch(projectId, branchName, baseBranch, userId);
        } catch (FeignException e) {
            throw new CommonException("error.branch.create", e);
        }
        return responseEntity.getBody();
    }

    public List<BranchDTO> listBranch(Integer projectId, String path, Integer userId) {
        ResponseEntity<List<BranchDTO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listBranch(projectId, userId);
        } catch (FeignException e) {
            throw new CommonException("error.branch.get", e);

        }
        List<BranchDTO> branches = responseEntity.getBody();
        branches.forEach(t -> t.getCommit().setUrl(
                String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId())));
        return branches;
    }


    public PageInfo<TagDTO> pageTag(ProjectDTO projectDTO, Integer gitlabProjectId, String path, Integer page, String params, Integer size, Integer userId) {

        if (!baseServiceClientOperator.isProjectOwner(TypeUtil.objToLong(GitUserNameUtil.getUserId()), projectDTO)) {
            MemberDTO memberDTO = getProjectMember(
                    gitlabProjectId,
                    userId);
            if (memberDTO == null) {
                throw new CommonException("error.user.not.in.project");
            }
        }

        List<TagDTO> tagTotalList = listTag(gitlabProjectId, userId);
        PageInfo<TagDTO> tagsPage = new PageInfo<>();
        List<TagDTO> tagList = tagTotalList.stream()
                .filter(t -> filterTag(t, params))
                .collect(Collectors.toCollection(ArrayList::new));
        List<TagDTO> tagVOS = tagList.stream()
                .sorted(this::sortTag)
                .map(TagDTO::new)
                .parallel()
                .peek(t -> {
                    IamUserDTO userDTO = baseServiceClientOperator.queryByEmail(TypeUtil.objToLong(gitlabProjectId), t.getCommit().getAuthorEmail());
                    if (userDTO != null) {
                        t.setCommitUserImage(userDTO.getImageUrl());
                    }
                    t.getCommit().setUrl(String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId()));
                })
                .collect(Collectors.toCollection(ArrayList::new));

        if (tagVOS.size() < size * page) {
            tagsPage.setSize(TypeUtil.objToInt(tagVOS.size()) - (size * (page - 1)));
        } else {
            tagsPage.setSize(size);
        }

        tagsPage.setPageSize(size);
        tagsPage.setTotal(tagList.size());
        tagsPage.setPageNum(page);
        tagsPage.setList(tagVOS);
        return tagsPage;
    }

    private Boolean filterTag(TagDTO tagDTO, String params) {
        Integer index = 0;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = TypeUtil.castMapParams(params);
            List<String> paramList = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
            if (!CollectionUtils.isEmpty(paramList)) {
                for (String param : paramList) {
                    if (tagDTO.getName().contains(param) || tagDTO.getCommit().getShortId().contains(param)
                            || tagDTO.getCommit().getCommitterName().contains(param)
                            || tagDTO.getCommit().getMessage().contains(param)) {
                        index = 1;
                        break;
                    }
                }
            }
            Map<String, Object> searchParam = TypeUtil.cast(maps.get(TypeUtil.PARAMS));
            if (searchParam != null) {
                index = getTagName(index, tagDTO, searchParam);
                index = getShortId(index, tagDTO, searchParam);
                index = getCommitterName(index, tagDTO, searchParam);
                index = getMessage(index, tagDTO, searchParam);
            }
        } else {
            return true;
        }
        return index >= 0;
    }

    private Integer getTagName(Integer index, TagDTO tagDTO, Map<String, Object> mapSearch) {
        String tagName = "tagName";
        if (index >= 0 && mapSearch.containsKey(tagName)
                && !StringUtils.isEmpty(mapSearch.get(tagName))) {
            index = tagDTO.getName().contains(String.valueOf(mapSearch.get(tagName))) ? 1 : -1;
        }
        return index;
    }

    private Integer getShortId(Integer index, TagDTO tagDTO, Map<String, Object> mapSearch) {
        String shortId = "shortId";
        if (index >= 0 && mapSearch.containsKey(shortId)
                && !StringUtils.isEmpty(mapSearch.get(shortId))) {
            index = tagDTO.getCommit().getId()
                    .contains(String.valueOf(mapSearch.get(shortId))) ? 1 : -1;
        }
        return index;
    }

    private Integer getCommitterName(Integer index, TagDTO tagDTO, Map<String, Object> mapSearch) {
        String committerName = "committerName";
        if (index >= 0 && mapSearch.containsKey(committerName)
                && !StringUtils.isEmpty(mapSearch.get(committerName))) {
            index = tagDTO.getCommit().getCommitterName()
                    .contains(String.valueOf(mapSearch.get(committerName))) ? 1 : -1;
        }
        return index;
    }

    private Integer getMessage(Integer index, TagDTO tagDTO, Map<String, Object> mapSearch) {
        String msg = "message";
        if (index >= 0 && mapSearch.containsKey(msg)
                && !StringUtils.isEmpty(mapSearch.get(msg))) {
            index = tagDTO.getCommit().getMessage().contains(String.valueOf(mapSearch.get(msg))) ? 1 : -1;
        }
        return index;
    }

    public List<TagDTO> listTag(Integer projectId, Integer userId) {
        ResponseEntity<List<TagDTO>> tagResponseEntity;
        try {
            tagResponseEntity = gitlabServiceClient.getTags(projectId, userId);
        } catch (FeignException e) {
            throw new CommonException("error.tags.get", e);
        }
        return tagResponseEntity.getBody();
    }


    public BranchDTO queryBranch(Integer gitlabProjectId, String branch) {
        try {
            return gitlabServiceClient.queryBranch(gitlabProjectId, branch).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.branch.get", e);

        }
    }


    public CompareResultDTO queryCompareResult(Integer gitlabProjectId, String from, String to) {
        try {
            return gitlabServiceClient.queryCompareResult(gitlabProjectId, from, to).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.diffs.get", e);
        }
    }


    private Integer sortTag(TagDTO a, TagDTO b) {
        TagNodeDO tagA = TagNodeDO.tagNameToTagNode(a.getName());
        TagNodeDO tagB = TagNodeDO.tagNameToTagNode(b.getName());
        if (tagA != null && tagB != null) {
            return tagA.compareTo(tagB) * -1;
        } else if (tagA == null && tagB != null) {
            return 1;
        } else if (tagA != null) {
            return -1;
        } else {
            return a.getName().compareToIgnoreCase(b.getName());
        }
    }

    public List<CommitDTO> getCommits(Integer gitLabProjectId, String branchName, String date) {
        try {
            return gitlabServiceClient.getCommits(gitLabProjectId, branchName, date).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }


    public List<BranchDTO> listBranch(Integer gitlabProjectId, Integer userId) {
        try {
            return gitlabServiceClient.listBranch(gitlabProjectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }


    public List<GitlabPipelineDTO> listPipeline(Integer projectId, Integer userId) {
        ResponseEntity<List<GitlabPipelineDTO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listPipeline(projectId, userId);
        } catch (FeignException e) {
            return new ArrayList<>();
        }
        return responseEntity.getBody();
    }


    public List<GitlabPipelineDTO> pagePipeline(Integer projectId, Integer page, Integer size, Integer userId) {
        ResponseEntity<List<GitlabPipelineDTO>> responseEntity;
        try {
            responseEntity =
                    gitlabServiceClient.pagePipeline(projectId, page, size, userId);
        } catch (FeignException e) {
            return new ArrayList<>();
        }
        return responseEntity.getBody();
    }


    public GitlabPipelineDTO queryPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<GitlabPipelineDTO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryPipeline(projectId, pipelineId, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return responseEntity.getBody();
    }


    public CommitDTO queryCommit(Integer projectId, String sha, Integer userId) {
        ResponseEntity<CommitDTO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryCommit(projectId, sha, userId);
        } catch (FeignException e) {
            return null;
        }
        return responseEntity.getBody();
    }


    public List<JobDTO> listJobs(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<List<JobDTO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listJobs(projectId, pipelineId, userId);
        } catch (FeignException e) {
            return new ArrayList<>();
        }
        return responseEntity.getBody();
    }


    public Boolean retryPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        try {
            gitlabServiceClient.retryPipeline(projectId, pipelineId, userId);
        } catch (FeignException e) {
            return false;
        }
        return true;
    }


    public Boolean cancelPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        try {
            gitlabServiceClient.cancelPipeline(projectId, pipelineId, userId);
        } catch (FeignException e) {
            return false;
        }
        return true;
    }


    public List<CommitStatusDTO> listCommitStatus(Integer projectId, String sha, Integer useId) {
        ResponseEntity<List<CommitStatusDTO>> commitStatuse;
        try {
            commitStatuse = gitlabServiceClient.listCommitStatus(projectId, sha, useId);
        } catch (FeignException e) {
            return Collections.emptyList();
        }
        return commitStatuse.getBody();
    }


    public List<CommitDTO> listCommits(Integer projectId, Integer userId, Integer page, Integer size) {
        try {
            List<CommitDTO> commitDTOS = new LinkedList<>();
            commitDTOS.addAll(gitlabServiceClient.listCommits(projectId, page, size, userId).getBody());
            return commitDTOS;
        } catch (FeignException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    public List<CommitDTO> listCommits(Integer projectId, Integer mergeRequestId, Integer userId) {
        try {
            List<CommitDTO> commitDTOS = new LinkedList<>();
            commitDTOS.addAll(gitlabServiceClient.listCommits(projectId, mergeRequestId, userId).getBody());
            return commitDTOS;
        } catch (FeignException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }


    public MemberDTO getProjectMember(Integer projectId, Integer userId) {

        MemberDTO memberDTO = gitlabServiceClient.getProjectMember(
                projectId, userId).getBody();
        if (memberDTO.getId() == null) {
            return null;
        }
        return memberDTO;
    }


    public void deleteBranch(Integer projectId, String branchName, Integer userId) {
        try {
            gitlabServiceClient.deleteBranch(projectId, branchName, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public List<MemberDTO> listMemberByProject(Integer projectId) {
        try {
            return gitlabServiceClient.listMemberByProject(projectId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public String getAdminToken() {
        try {
            return gitlabServiceClient.getAdminToken().getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }

    }
}
