package io.choerodon.devops.infra.feign.operator;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import feign.FeignException;
import feign.RetryableException;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.gitlab.MemberDTO;
import io.choerodon.devops.api.vo.gitlab.VariableDTO;
import io.choerodon.devops.app.eventhandler.payload.GitlabUserPayload;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.domain.application.entity.gitlab.CompareResultsE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.valueobject.*;
import io.choerodon.devops.infra.dto.DevopsBranchDO;
import io.choerodon.devops.infra.dto.gitlab.*;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.devops.infra.util.GitUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Created by Sheep on 2019/7/11.
 */

@Component
public class GitlabServiceClientOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabServiceClientOperator.class);



    @Autowired
    private GitlabServiceClient gitlabServiceClient;
    @Autowired
    private GitUtil gitUtil;


    public GitlabUserE createGitLabUser(String password, Integer projectsLimit, GitlabUserPayload gitlabUserPayload) {
        ResponseEntity<UserDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.createGitLabUser(
                    password, projectsLimit, gitlabUserPayload);
        } catch (FeignException e) {
            LOGGER.info("error.gitlab.user.create");
            throw new CommonException(e);
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    public GitlabUserE getUserByUserName(String userName) {
        ResponseEntity<UserDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryUserByUserName(userName);
        } catch (FeignException e) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    public GitlabUserE updateGitLabUser(Integer userId, Integer projectsLimit, GitlabUserPayload gitlabUserPayload) {
        ResponseEntity<UserDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.updateGitLabUser(
                    userId, projectsLimit, gitlabUserPayload);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    public void isEnabledGitlabUser(Integer userId) {

        try {
            gitlabServiceClient.enabledUserByUserId(userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public void disEnabledGitlabUser(Integer userId) {
        try {
            gitlabServiceClient.disEnabledUserByUserId(userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public GitlabUserE getGitlabUserByUserId(Integer userId) {
        ResponseEntity<UserDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.queryUserByUserId(userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabUserE.class);
    }

    public Boolean checkEmailIsExist(String email) {
        return gitlabServiceClient.checkEmailIsExist(email).getBody();
    }


    public GitlabMemberE getUserMemberByUserId(Integer groupId, Integer userId) {
        return ConvertHelper.convert(gitlabServiceClient.getUserMemberByUserId(
                groupId, userId).getBody(), GitlabMemberE.class);
    }

    public ResponseEntity deleteMember(Integer groupId, Integer userId) {
        return gitlabServiceClient.deleteMember(groupId, userId);
    }

    public int insertMember(Integer groupId, RequestMemberDO member) {
        return gitlabServiceClient.insertMember(groupId, member).getStatusCodeValue();
    }


    public ResponseEntity updateMember(Integer groupId, RequestMemberDO member) {
        return gitlabServiceClient.updateMember(groupId, member);
    }


    public void addVariable(Integer gitlabProjectId, String key, String value, Boolean protecteds, Integer userId) {
        gitlabServiceClient.addVariable(gitlabProjectId, key, value, protecteds, userId);
    }

    public void batchAddVariable(Integer gitlabProjectId, Integer userId, List<VariableDTO> variableDTOS) {
        gitlabServiceClient.batchAddVariable(gitlabProjectId, userId, variableDTOS);
    }

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

    public void deleteFile(Integer projectId, String path, String commitMessage, Integer userId) {
        try {
            gitlabServiceClient.deleteFile(projectId, path, commitMessage, userId);
        } catch (FeignException e) {
            throw new CommonException("error.file.delete", e);
        }
    }

    public void deleteDevOpsApp(String groupName, String projectName, Integer userId) {
        try {
            gitlabServiceClient.deleteProjectByProjectName(groupName, projectName, userId);
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
            gitlabServiceClient.createProtectedBranches(
                    projectId, name, mergeAccessLevel, pushAccessLevel, userId);
        } catch (FeignException e) {
            throw new CommonException("error.branch.create", e);
        }
    }

    public void deleteProject(Integer projectId, Integer userId) {
        try {
            gitlabServiceClient.deleteProject(projectId, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public void updateGroup(Integer projectId, Integer userId, GroupDO groupDO) {
        gitlabServiceClient.updateGroup(projectId, userId, groupDO);
    }


    public ProjectHook createWebHook(Integer projectId, Integer userId, ProjectHook projectHook) {
        try {
            return gitlabServiceClient.createProjectHook(projectId, userId, projectHook).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.projecthook.create", e);

        }
    }

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

    public GitlabProjectDO createProject(Integer groupId, String projectName, Integer userId, boolean visibility) {
        try {
            return gitlabServiceClient
                    .createProject(groupId, projectName, userId, visibility).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.gitlab.project.create", e);

        }
    }

    public GitlabProjectDO getProjectById(Integer projectId) {
        try {
            return gitlabServiceClient.getProjectById(projectId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public GitlabProjectDO getProjectByName(String groupName, String projectName, Integer userId) {
        try {
            return gitlabServiceClient.getProjectByName(userId, groupName, projectName).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public List<ProjectHook> getHooks(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.getProjectHook(projectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public List<Variable> getVariable(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.getVariable(projectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    public List<DeployKey> getDeployKeys(Integer projectId, Integer userId) {
        try {
            return gitlabServiceClient.getDeploykeys(projectId, userId).getBody();
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

    public void addMemberIntoProject(Integer projectId, MemberDTO memberDTO) {
        try {
            gitlabServiceClient.addMemberIntoProject(projectId, memberDTO);
        } catch (Exception e) {
            throw new CommonException("error.member.add", e);
        }
    }

    public void updateMemberIntoProject(Integer projectId, List<MemberDTO> list) {
        try {
            gitlabServiceClient.updateMemberIntoProject(projectId, list);
        } catch (Exception e) {
            throw new CommonException("error.member.update", e);
        }
    }

    public void removeMemberFromProject(Integer groupId, Integer userId) {
        try {
            gitlabServiceClient.removeMemberFromProject(groupId, userId);
        } catch (Exception e) {
            throw new CommonException("error.member.remove", e);
        }
    }


    public List<GitlabProjectDO> getProjectsByUserId(Integer userId) {
        try {
            return gitlabServiceClient.getProjectsByUserId(userId).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.project.get.by.userId", e);
        }
    }


    public MergeRequestDO createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String title, String description, Integer userId) {
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

    public TagDO updateTag(Integer gitLabProjectId, String tag, String releaseNotes, Integer userId) {
        try {
            if (releaseNotes == null) {
                releaseNotes = "";
            }
            return gitlabServiceClient.updateTagRelease(gitLabProjectId, tag, releaseNotes, userId).getBody();
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


    public BranchDO createBranch(Integer projectId, String branchName, String baseBranch, Integer userId) {
        ResponseEntity<BranchDO> responseEntity;
        try {
            responseEntity =
                    gitlabServiceClient.createBranch(projectId, branchName, baseBranch, userId);
        } catch (FeignException e) {
            throw new CommonException("error.branch.create", e);
        }
        return responseEntity.getBody();
    }

    public List<BranchDO> listGitLabBranches(Integer projectId, String path, Integer userId) {
        ResponseEntity<List<BranchDO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listBranches(projectId, userId);
        } catch (FeignException e) {
            throw new CommonException("error.branch.get", e);

        }
        List<BranchDO> branches = responseEntity.getBody();
        branches.forEach(t -> t.getCommit().setUrl(
                String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId())));
        return branches;
    }


    public PageInfo<TagDTO> getTags(Long appId, String path, Integer page, String params, Integer size, Integer userId) {
        ApplicationE applicationE = applicationRepository.query(appId);
        GitlabMemberE newGroupMemberE = gitlabProjectRepository.getProjectMember(
                TypeUtil.objToInteger(applicationE.getGitlabProjectE().getId()),
                userId);
        if (newGroupMemberE == null) {
            throw new CommonException("error.user.not.the.pro.authority");
        }
        Integer projectId = getGitLabId(appId);
        List<TagDO> tagTotalList = getGitLabTags(projectId, userId);
        PageInfo<TagDTO> tagsPage = new PageInfo<>();
        List<TagDO> tagList = tagTotalList.stream()
                .filter(t -> filterTag(t, params))
                .collect(Collectors.toCollection(ArrayList::new));
        List<TagDTO> tagDTOS = tagList.stream()
                .sorted(this::sortTag)
                .map(TagDTO::new)
                .parallel()
                .peek(t -> {
                    UserE userE = iamRepository.queryByEmail(TypeUtil.objToLong(projectId), t.getCommit().getAuthorEmail());
                    if (userE != null) {
                        t.setCommitUserImage(userE.getImageUrl());
                    }
                    t.getCommit().setUrl(String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId()));
                })
                .collect(Collectors.toCollection(ArrayList::new));

        if (tagDTOS.size() < size * page) {
            tagsPage.setSize(TypeUtil.objToInt(tagDTOS.size()) - (size * (page - 1)));
        } else {
            tagsPage.setSize(size);
        }

        tagsPage.setPageSize(size);
        tagsPage.setTotal(tagList.size());
        tagsPage.setPageNum(page);
        tagsPage.setList(tagDTOS);
        return tagsPage;
    }

    private Boolean filterTag(TagDO tagDO, String params) {
        Integer index = 0;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = json.deserialize(params, Map.class);
            String param = TypeUtil.cast(maps.get(TypeUtil.PARAM));
            param = param == null ? "" : param;
            if (!param.equals("")) {
                if (tagDO.getName().contains(param) || tagDO.getCommit().getShortId().contains(param)
                        || tagDO.getCommit().getCommitterName().contains(param)
                        || tagDO.getCommit().getMessage().contains(param)) {
                    index = 1;
                } else {
                    return false;
                }
            }
            Object obj = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
            if (obj != null) {
                Map<String, ArrayList<String>> mapSearch = (Map<String, ArrayList<String>>) obj;
                index = getTagName(index, tagDO, mapSearch);
                index = getShortId(index, tagDO, mapSearch);
                index = getCommitterName(index, tagDO, mapSearch);
                index = getMessage(index, tagDO, mapSearch);
            }
        }
        return index >= 0;
    }

    private Integer getTagName(Integer index, TagDO tagDO, Map<String, ArrayList<String>> mapSearch) {
        String tagName = "tagName";
        if (mapSearch.containsKey(tagName)
                && mapSearch.get(tagName) != null
                && !mapSearch.get(tagName).isEmpty()
                && mapSearch.get(tagName).get(0) != null) {
            index = tagDO.getName().contains(mapSearch.get(tagName).get(0)) ? 1 : -1;
        }
        return index;
    }

    private Integer getShortId(Integer index, TagDO tagDO, Map<String, ArrayList<String>> mapSearch) {
        String shortId = "shortId";
        if (index >= 0 && mapSearch.containsKey(shortId)
                && mapSearch.get(shortId) != null
                && !mapSearch.get(shortId).isEmpty()
                && mapSearch.get(shortId).get(0) != null) {
            index = tagDO.getCommit().getId()
                    .contains(mapSearch.get(shortId).get(0)) ? 1 : -1;
        }
        return index;
    }

    private Integer getCommitterName(Integer index, TagDO tagDO, Map<String, ArrayList<String>> mapSearch) {
        String committerName = "committerName";
        if (index >= 0 && mapSearch.containsKey(committerName)
                && mapSearch.get(committerName) != null
                && !mapSearch.get(committerName).isEmpty()
                && mapSearch.get(committerName).get(0) != null) {
            index = tagDO.getCommit().getCommitterName()
                    .contains(mapSearch.get(committerName).get(0)) ? 1 : -1;
        }
        return index;
    }

    private Integer getMessage(Integer index, TagDO tagDO, Map<String, ArrayList<String>> mapSearch) {
        String msg = "message";
        if (index >= 0 && mapSearch.containsKey(msg)
                && mapSearch.get(msg) != null
                && !mapSearch.get(msg).isEmpty()
                && mapSearch.get(msg).get(0) != null) {
            index = tagDO.getCommit().getMessage().contains(mapSearch.get(msg).get(0)) ? 1 : -1;
        }
        return index;
    }

    public List<TagDO> getGitLabTags(Integer projectId, Integer userId) {
        ResponseEntity<List<TagDO>> tagResponseEntity;
        try {
            tagResponseEntity = gitlabServiceClient.getTags(projectId, userId);
        } catch (FeignException e) {
            throw new CommonException("error.tags.get", e);
        }
        return tagResponseEntity.getBody();
    }



    public BranchDO getBranch(Integer gitlabProjectId, String branch) {
        try {
            return gitlabServiceClient.getBranch(gitlabProjectId, branch).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.branch.get", e);

        }
    }


    public CompareResultsE getCompareResults(Integer gitlabProjectId, String from, String to) {
        try {
            return gitlabServiceClient.getCompareResults(gitlabProjectId, from, to).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.diffs.get", e);
        }
    }



    @Override
    public DevopsBranchE queryByAppAndBranchName(Long appId, String branchName) {
        return ConvertHelper.convert(devopsBranchMapper
                .queryByAppAndBranchName(appId, branchName), DevopsBranchE.class);
    }

    @Override
    public void updateBranchIssue(Long appId, DevopsBranchE devopsBranchE) {
        DevopsBranchDO devopsBranchDO = devopsBranchMapper
                .queryByAppAndBranchName(appId, devopsBranchE.getBranchName());
        devopsBranchDO.setIssueId(devopsBranchE.getIssueId());
        devopsBranchMapper.updateByPrimaryKey(devopsBranchDO);
    }

    @Override
    public void updateBranchLastCommit(DevopsBranchE devopsBranchE) {
        DevopsBranchDO branchDO = devopsBranchMapper
                .queryByAppAndBranchName(devopsBranchE.getApplicationE().getId(), devopsBranchE.getBranchName());
        branchDO.setLastCommit(devopsBranchE.getLastCommit());
        branchDO.setLastCommitDate(devopsBranchE.getLastCommitDate());
        branchDO.setLastCommitMsg(devopsBranchE.getLastCommitMsg());
        branchDO.setLastCommitUser(devopsBranchE.getLastCommitUser());
        devopsBranchMapper.updateByPrimaryKey(branchDO);

    }

    @Override
    public DevopsBranchE createDevopsBranch(DevopsBranchE devopsBranchE) {
        devopsBranchE.setDeleted(false);
        DevopsBranchDO devopsBranchDO = ConvertHelper.convert(devopsBranchE, DevopsBranchDO.class);
        devopsBranchMapper.insert(devopsBranchDO);
        return ConvertHelper.convert(devopsBranchDO, DevopsBranchE.class);
    }

    @Override
    public DevopsBranchE qureyBranchById(Long devopsBranchId) {
        return ConvertHelper.convert(devopsBranchMapper.selectByPrimaryKey(devopsBranchId), DevopsBranchE.class);
    }

    @Override
    public void updateBranch(DevopsBranchE devopsBranchE) {
        DevopsBranchDO branchDO = ConvertHelper.convert(devopsBranchE, DevopsBranchDO.class);
        branchDO.setObjectVersionNumber(devopsBranchMapper.selectByPrimaryKey(devopsBranchE.getId()).getObjectVersionNumber());
        if (devopsBranchMapper.updateByPrimaryKey(branchDO) != 1) {
            throw new CommonException("error.branch.update");
        }
    }

    @Override
    public Map<String, Object> getMergeRequestList(Long projectId, Integer gitLabProjectId,
                                                   String state,
                                                   PageRequest pageRequest) {
        List<DevopsMergeRequestE> allMergeRequest = devopsMergeRequestRepository
                .getByGitlabProjectId(gitLabProjectId);
        final int[] count = {0, 0, 0};
        if (allMergeRequest != null && !allMergeRequest.isEmpty()) {
            allMergeRequest.forEach(devopsMergeRequestE -> {
                if ("merged".equals(devopsMergeRequestE.getState())) {
                    count[0]++;
                } else if ("opened".equals(devopsMergeRequestE.getState())) {
                    count[1]++;
                } else if ("closed".equals(devopsMergeRequestE.getState())) {
                    count[2]++;
                }
            });
        }
        PageInfo<DevopsMergeRequestE> page = devopsMergeRequestRepository
                .getByGitlabProjectId(gitLabProjectId, pageRequest);
        if (StringUtil.isNotEmpty(state)) {
            page = devopsMergeRequestRepository
                    .getMergeRequestList(gitLabProjectId, state, pageRequest);
        }
        List<MergeRequestDTO> pageContent = new ArrayList<>();
        List<DevopsMergeRequestE> content = page.getList();
        if (content != null && !content.isEmpty()) {
            content.forEach(devopsMergeRequestE -> {
                MergeRequestDTO mergeRequestDTO = devopsMergeRequestToMergeRequest(
                        devopsMergeRequestE);
                pageContent.add(mergeRequestDTO);
            });
        }
        int total = count[0] + count[1] + count[2];
        PageInfo<MergeRequestDTO> pageResult = new PageInfo<>();
        BeanUtils.copyProperties(page, pageResult);
        pageResult.setList(pageContent);
        Map<String, Object> result = new HashMap<>();
        result.put("mergeCount", count[0]);
        result.put("openCount", count[1]);
        result.put("closeCount", count[2]);
        result.put("totalCount", total);
        result.put("pageResult", pageResult);
        return result;
    }

    private MergeRequestDTO devopsMergeRequestToMergeRequest(DevopsMergeRequestE devopsMergeRequestE) {
        MergeRequestDTO mergeRequestDTO = new MergeRequestDTO();
        BeanUtils.copyProperties(devopsMergeRequestE, mergeRequestDTO);
        mergeRequestDTO.setProjectId(devopsMergeRequestE.getProjectId().intValue());
        mergeRequestDTO.setId(devopsMergeRequestE.getId().intValue());
        mergeRequestDTO.setIid(devopsMergeRequestE.getGitlabMergeRequestId().intValue());
        Long authorUserId = devopsGitRepository
                .getUserIdByGitlabUserId(devopsMergeRequestE.getAuthorId());
        Long assigneeId = devopsGitRepository
                .getUserIdByGitlabUserId(devopsMergeRequestE.getAssigneeId());
        Long gitlabMergeRequestId = devopsMergeRequestE.getGitlabMergeRequestId();
        Integer gitlabUserId = devopsGitRepository.getGitlabUserId();
        List<CommitDO> commitDOS = new ArrayList<>();
        try {
            commitDOS = gitlabServiceClient.listCommits(
                    devopsMergeRequestE.getProjectId().intValue(),
                    gitlabMergeRequestId.intValue(), gitlabUserId).getBody();
            mergeRequestDTO.setCommits(ConvertHelper.convertList(commitDOS, CommitDTO.class));
        } catch (FeignException e) {
            LOGGER.info(e.getMessage());
        }
        UserE authorUser = iamRepository.queryUserByUserId(authorUserId);
        if (authorUser != null) {
            AuthorDTO authorDTO = new AuthorDTO();
            authorDTO.setUsername(authorUser.getLoginName());
            authorDTO.setName(authorUser.getRealName());
            authorDTO.setId(authorUser.getId() == null ? null : authorUser.getId().intValue());
            authorDTO.setWebUrl(authorUser.getImageUrl());
            mergeRequestDTO.setAuthor(authorDTO);
        }
        UserE assigneeUser = iamRepository.queryUserByUserId(assigneeId);
        if (assigneeUser != null) {
            AssigneeDTO assigneeDTO = new AssigneeDTO();
            assigneeDTO.setUsername(assigneeUser.getLoginName());
            assigneeDTO.setName(assigneeUser.getRealName());
            assigneeDTO.setId(assigneeId.intValue());
            assigneeDTO.setWebUrl(assigneeUser.getImageUrl());
            mergeRequestDTO.setAssignee(assigneeDTO);
        }
        return mergeRequestDTO;
    }



    public CommitE getCommit(Integer gitLabProjectId, String commit, Integer userId) {
        CommitE commitE = new CommitE();
        BeanUtils.copyProperties(
                gitlabServiceClient.getCommit(gitLabProjectId, commit, userId).getBody(),
                commitE);
        return commitE;
    }


    private Integer sortTag(TagDO a, TagDO b) {
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

    public List<CommitDO> getCommits(Integer gitLabProjectId, String branchName, String date) {
        try {
            return gitlabServiceClient.getCommits(gitLabProjectId, branchName, date).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }


    public List<BranchDO> listBranches(Integer gitlabProjectId, Integer userId) {
        try {
            return gitlabServiceClient.listBranches(gitlabProjectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }



    public List<GitlabPipelineE> listPipeline(Integer projectId, Integer userId) {
        ResponseEntity<List<PipelineDO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listPipeline(projectId, userId);
        } catch (FeignException e) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(responseEntity.getBody(), GitlabPipelineE.class);
    }


    public List<GitlabPipelineE> listPipelines(Integer projectId, Integer page, Integer size, Integer userId) {
        ResponseEntity<List<PipelineDO>> responseEntity;
        try {
            responseEntity =
                    gitlabServiceClient.listPipelines(projectId, page, size, userId);
        } catch (FeignException e) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(responseEntity.getBody(), GitlabPipelineE.class);
    }


    public GitlabPipelineE getPipeline(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<PipelineDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.getPipeline(projectId, pipelineId, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabPipelineE.class);
    }


    public GitlabCommitE getCommit(Integer projectId, String sha, Integer userId) {
        ResponseEntity<CommitDO> responseEntity;
        try {
            responseEntity = gitlabServiceClient.getCommit(projectId, sha, userId);
        } catch (FeignException e) {
            return null;
        }
        return ConvertHelper.convert(responseEntity.getBody(), GitlabCommitE.class);
    }


    public List<GitlabJobE> listJobs(Integer projectId, Integer pipelineId, Integer userId) {
        ResponseEntity<List<JobDO>> responseEntity;
        try {
            responseEntity = gitlabServiceClient.listJobs(projectId, pipelineId, userId);
        } catch (FeignException e) {
            return new ArrayList<>();
        }
        return ConvertHelper.convertList(responseEntity.getBody(), GitlabJobE.class);
    }


    public Boolean retry(Integer projectId, Integer pipelineId, Integer userId) {
        try {
            gitlabServiceClient.retry(projectId, pipelineId, userId);
        } catch (FeignException e) {
            return false;
        }
        return true;
    }


    public Boolean cancel(Integer projectId, Integer pipelineId, Integer userId) {
        try {
            gitlabServiceClient.cancel(projectId, pipelineId, userId);
        } catch (FeignException e) {
            return false;
        }
        return true;
    }


    public List<CommitStatuseDO> getCommitStatus(Integer projectId, String sha, Integer useId) {
        ResponseEntity<List<CommitStatuseDO>> commitStatuse;
        try {
            commitStatuse = gitlabServiceClient.getCommitStatus(projectId, sha, useId);
        } catch (FeignException e) {
            return Collections.emptyList();
        }
        return commitStatuse.getBody();
    }


    public List<CommitDO> listCommits(Integer projectId, Integer userId, Integer page, Integer size) {
        try {
            List<CommitDO> commitDOS = new LinkedList<>();
            commitDOS.addAll(gitlabServiceClient.listCommits(projectId, page, size, userId).getBody());
            return commitDOS;
        } catch (FeignException e) {
            throw new CommonException(e.getMessage(), e);
        }
    }


    public GitlabMemberE getProjectMember(Integer projectId, Integer userId) {
        try {
            return ConvertHelper.convert(gitlabServiceClient.getProjectMember(
                    projectId, userId).getBody(), GitlabMemberE.class);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }


    public void deleteBranch(Integer projectId, String branchName, Integer userId) {
        try {
            gitlabServiceClient.deleteBranch(projectId, branchName, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }


    public List<GitlabMemberE> getAllMemberByProjectId(Integer projectId) {
        try {
            return ConvertHelper
                    .convertList(gitlabServiceClient.getAllMemberByProjectId(projectId).getBody(), GitlabMemberE.class);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }


}
