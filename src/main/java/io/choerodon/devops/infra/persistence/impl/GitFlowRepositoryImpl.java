package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.GitFlowRepository;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;
import io.choerodon.devops.infra.dataobject.gitlab.*;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.devops.infra.mapper.ApplicationMapper;
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 14:54
 * Description:
 */
@Component
public class GitFlowRepositoryImpl implements GitFlowRepository {
    private static final Logger logger = LoggerFactory.getLogger(GitFlowRepository.class);

    private static final String BRANCH_MASTER = "master";
    private ApplicationMapper applicationMapper;
    private DevopsMergeRequestMapper devopsMergeRequestMapper;
    private GitlabServiceClient gitlabServiceClient;

    /**
     * 构造方法
     */
    public GitFlowRepositoryImpl(ApplicationMapper applicationMapper,
                                 DevopsMergeRequestMapper devopsMergeRequestMapper,
                                 GitlabServiceClient gitlabServiceClient) {
        this.applicationMapper = applicationMapper;
        this.devopsMergeRequestMapper = devopsMergeRequestMapper;
        this.gitlabServiceClient = gitlabServiceClient;
    }

    private String mergeRequestDescription(String sourceBranch, String targetBranch, String username) {
        return "Created by @" + username + " right after creating branch `" + sourceBranch + "`.  \n"
                + "Changes will be merged into the branch `" + targetBranch + "`.";
    }

    @Override
    public Integer getGitLabId(Long applicationId) {
        ApplicationDO applicationDO = applicationMapper.selectByPrimaryKey(applicationId);
        if (applicationDO != null) {
            return applicationDO.getGitlabProjectId();
        } else {
            throw new CommonException("error.application.select");
        }
    }

    @Override
    public void createMergeRequest(Integer projectId, String sourceBranch, String targetBranch, String username) {
        ResponseEntity<MergeRequestDO> requestResponseEntity = gitlabServiceClient.createMergeRequest(
                projectId,
                sourceBranch,
                targetBranch,
                sourceBranch,
                mergeRequestDescription(sourceBranch, targetBranch, username),
                username);
        MergeRequestDO mergeRequest = requestResponseEntity.getBody();
        Long mergeRequestId = mergeRequest.getIid().longValue();
        DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO(
                projectId.longValue(), sourceBranch, targetBranch);
        devopsMergeRequestMapper.delete(devopsMergeRequestDO);
        devopsMergeRequestDO.setMergeRequestId(mergeRequestId);
        devopsMergeRequestMapper.insert(devopsMergeRequestDO);
    }


    @Override
    public MergeRequestDO getMergeRequest(Integer projectId, Long mergeRequestId, String username) {
        return gitlabServiceClient
                .getMergeRequest(projectId, mergeRequestId.intValue(), username)
                .getBody();
    }

    @Override
    public void deleteMergeRequest(Integer projectId, Integer mergeRequestId) {
        gitlabServiceClient.deleteMergeRequest(projectId, mergeRequestId);
    }


    /**
     * 搜索数据库 devops_merge_request 表中对应记录
     *
     * @param projectId    应用 ID
     * @param sourceBranch 源分支名称
     * @param targetBranch 目标分支
     * @return 记录
     */
    @Override
    public DevopsMergeRequestDO getDevOpsMergeRequest(Integer projectId, String sourceBranch, String targetBranch) {
        DevopsMergeRequestDO mergeRequestDO = devopsMergeRequestMapper.selectOne(
                new DevopsMergeRequestDO(projectId.longValue(), sourceBranch, targetBranch));
        if (mergeRequestDO == null) {
            throw new CommonException("error.mergeRequest.notExist");
        }
        return mergeRequestDO;
    }

    @Override
    public void deleteDevOpsMergeRequest(Long id) {
        devopsMergeRequestMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void updateMergeRequest(Integer projectId, Long mergeRequestId, String username) {
        gitlabServiceClient.updateMergeRequest(projectId, mergeRequestId.intValue(), username);
    }

    @Override
    public Boolean checkMergeRequestCommit(Integer projectId, Long mergeRequestId) {
        return gitlabServiceClient.listCommits(projectId, mergeRequestId.intValue()).getBody().isEmpty();
    }

    @Override
    public void createBranch(Integer projectId, String branchName, String baseBranch) {
        ResponseEntity<BranchDO> responseEntity =
                gitlabServiceClient.createBranch(projectId, branchName, baseBranch);
        if ("create branch message:Branch already exists".equals(responseEntity.getBody().getName())) {
            throw new CommonException("error.feature.exist");
        }
    }

    @Override
    public List<BranchDO> listBranches(Integer projectId, String path) {
        ResponseEntity<List<BranchDO>> responseEntity = gitlabServiceClient.listBranches(projectId);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new CommonException("error.branch.get");
        }
        List<BranchDO> branches = responseEntity.getBody();
        branches.forEach(t -> t.getCommit().setUrl(
                String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId())));
        return branches;
    }

    @Override
    public void deleteBranch(Integer projectId, String branchName, String username) {
        gitlabServiceClient.deleteBranch(projectId, branchName, username);
    }

    @Override
    public MergeRequestDO acceptMergeRequest(Integer projectId, Integer mergeRequestId, String message, String username) {
        ResponseEntity<MergeRequestDO> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        try {
            responseEntity = gitlabServiceClient
                    .acceptMergeRequest(
                            projectId,
                            mergeRequestId,
                            message,
                            false,
                            false,
                            username);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        MergeRequestDO mergeRequest;
        if (responseEntity.getStatusCodeValue() != 400) {
            mergeRequest = responseEntity.getBody();
        } else {
            throw new CommonException("error.gitlab.acceptMR");
        }

        return mergeRequest;
    }

    @Override
    public Optional<TagNodeDO> getMaxTagNode(Long applicationId, String username) {
        Integer projectId = getGitLabId(applicationId);
        if (username == null) {
            username = GitUserNameUtil.getUsername();
        }
        ResponseEntity<List<TagDO>> tagsResponseEntity = gitlabServiceClient.getTags(projectId, username);
        if (tagsResponseEntity.getStatusCode() != HttpStatus.OK) {
            throw new CommonException("error.tags.get");
        }
        return tagsResponseEntity.getBody().stream()
                .map(t -> TagNodeDO.tagNameToTagNode(t.getName()))
                .filter(Objects::nonNull)
                .max(TagNodeDO::compareTo);

    }

    @Override
    public void createTag(Integer projectId, String tag, String username) {
        gitlabServiceClient.createTag(projectId, tag, BRANCH_MASTER, username);
    }

    @Override
    public TagsDO getTags(Long serviceId, String path, Integer page, Integer size) {
        Integer projectId = getGitLabId(serviceId);
        String username = GitUserNameUtil.getUsername();
        ResponseEntity<List<TagDO>> tagResponseEntity = gitlabServiceClient.getTags(projectId, username);
        ResponseEntity<List<TagDO>> tags = gitlabServiceClient.getPageTags(projectId, page + 1, size);
        if (tagResponseEntity.getStatusCode() != HttpStatus.OK) {
            throw new CommonException("error.tags.get");
        }
        List<TagDO> tagList = tagResponseEntity.getBody();
        tagList.forEach(t -> t.getCommit().setUrl(
                String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId())));
        int totalPageSizes = tagList.size() / size + (tagList.size() % size == 0 ? 0 : 1);
        return new TagsDO(tags.getBody(), totalPageSizes, tagList.size());
    }
}
