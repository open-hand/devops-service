package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.CommitDTO;
import io.choerodon.devops.api.dto.MergeRequestDTO;
import io.choerodon.devops.domain.application.entity.DevopsBranchE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
import io.choerodon.devops.domain.application.repository.DevopsGitRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.devops.infra.dataobject.DevopsBranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.*;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.devops.infra.mapper.ApplicationMapper;
import io.choerodon.devops.infra.mapper.DevopsBranchMapper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.util.StringUtil;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:02
 * Description:
 */
@Component
public class DevopsGitRepositoryImpl implements DevopsGitRepository {
    @Autowired
    private GitlabServiceClient gitlabServiceClient;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private DevopsBranchMapper devopsBranchMapper;
    @Autowired
    private DevopsGitRepository devopsGitRepository;

    @Override
    public void createTag(Integer gitLabProjectId, String tag, String ref, Integer userId) {
        gitlabServiceClient.createTag(gitLabProjectId, tag, ref, userId);
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
    public Integer getGitlabUserId() {
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        return TypeUtil.objToInteger(userAttrE.getGitlabUserId());
    }

    @Override
    public BranchDO createBranch(Integer projectId, String branchName, String baseBranch, Integer userId) {
        ResponseEntity<BranchDO> responseEntity =
                gitlabServiceClient.createBranch(projectId, branchName, baseBranch, userId);
        if ("create branch message:Branch already exists".equals(responseEntity.getBody().getName())) {
            throw new CommonException("error.branch.exist");
        }
        return responseEntity.getBody();
    }

    @Override
    public List<BranchDO> listBranches(Integer projectId, String path, Integer userId) {
        ResponseEntity<List<BranchDO>> responseEntity = gitlabServiceClient.listBranches(projectId, userId);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new CommonException("error.branch.get");
        }
        List<BranchDO> branches = responseEntity.getBody();
        branches.forEach(t -> t.getCommit().setUrl(
                String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId())));
        return branches;
    }

    @Override
    public void deleteBranch(Integer projectId, String branchName, Integer userId) {
        gitlabServiceClient.deleteBranch(projectId, branchName, userId);
    }

    @Override
    public Page<TagDO> getTags(Long appId, String path, Integer page, Integer size, Integer userId) {
        Integer projectId = getGitLabId(appId);
        List<TagDO> tagTotalList = getGitLabTags(projectId, userId);
        Integer totalSize = tagTotalList.size();
        int totalPageSizes = totalSize / size + (totalSize % size == 0 ? 0 : 1);
        if (page > totalPageSizes - 1 && page > 0) {
            page = totalPageSizes - 1;
        }

        List<TagDO> tagList = tagTotalList.stream()
                .sorted(this::sortTag)
                .skip(page.longValue() * size).limit(size)
                .peek(t -> t.getCommit().setUrl(
                        String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId())))
                .collect(Collectors.toCollection(ArrayList::new));
        Page<TagDO> tagsPage = new Page<>();
        tagsPage.setSize(size);
        tagsPage.setTotalElements(totalSize);
        tagsPage.setTotalPages(totalPageSizes);
        tagsPage.setContent(tagList);
        tagsPage.setNumber(page);
        tagsPage.setNumberOfElements(tagList.size());
        return tagsPage;
    }

    @Override
    public List<TagDO> getTagList(Long appId, Integer userId) {
        Integer projectId = getGitLabId(appId);
        return getGitLabTags(projectId, userId);
    }

    @Override
    public List<TagDO> getGitLabTags(Integer projectId, Integer userId) {
        ResponseEntity<List<TagDO>> tagResponseEntity = gitlabServiceClient.getTags(projectId, userId);
        if (tagResponseEntity.getStatusCode() != HttpStatus.OK) {
            throw new CommonException("error.tags.get");
        }
        return tagResponseEntity.getBody();
    }

    @Override
    public DevopsBranchE queryByAppAndBranchName(Long appId, String branchName) {
        return ConvertHelper.convert(devopsBranchMapper
                .queryByAppAndBranchName(appId, branchName), DevopsBranchE.class);
    }

    @Override
    public void updateBranch(Long appId, DevopsBranchE devopsBranchE) {
        DevopsBranchDO devopsBranchDO = devopsBranchMapper
                .queryByAppAndBranchName(appId, devopsBranchE.getBranchName());
        devopsBranchDO.setIssueId(devopsBranchE.getIssueId());
        devopsBranchMapper.updateByPrimaryKeySelective(devopsBranchDO);
    }

    @Override
    public void createDevopsBranch(DevopsBranchE devopsBranchE) {
        devopsBranchMapper.insert(ConvertHelper.convert(devopsBranchE, DevopsBranchDO.class));
    }

    @Override
    public Page<MergeRequestDTO> getMergeRequestList(Integer gitLabProjectId, String state, PageRequest pageRequest) {
        Page<MergeRequestDTO> pageResult = new Page<>();
        int page = pageRequest.getPage();
        int size = pageRequest.getSize() == 0 ? 10 : pageRequest.getSize();
        pageResult.setSize(size);
        List<MergeRequestDO> mergeRequestDOS = gitlabServiceClient.getMergeRequestList(gitLabProjectId).getBody();
        if (mergeRequestDOS != null && !mergeRequestDOS.isEmpty()) {
            if (StringUtil.isNotEmpty(state)) {
                mergeRequestDOS = mergeRequestDOS.stream().filter(mergeRequestDO ->
                        mergeRequestDO.getState().equals(state)).skip(page * size * 1L)
                        .limit(size * 1L).collect(Collectors.toList());
            }
            mergeRequestDOS.parallelStream()
                    .forEach(mergeRequestDO -> {
                        List<CommitDO> commitDOs = gitlabServiceClient.listCommits(gitLabProjectId,
                                mergeRequestDO.getIid(),
                                devopsGitRepository.getGitlabUserId()).getBody();
                        List<CommitDTO> commitDTOS = ConvertHelper.convertList(commitDOs, CommitDTO.class);
                        mergeRequestDO.setCommits(commitDTOS);
                    });
            int totalSize = mergeRequestDOS.size();
            int totalPage = totalSize % size == 0 ? totalSize / size : (totalSize / size) + 1;
            List<MergeRequestDTO> mergeRequestDTOS = ConvertHelper.convertList(mergeRequestDOS, MergeRequestDTO.class);
            pageResult.setContent(mergeRequestDTOS);
            pageResult.setTotalPages(totalPage);
            pageResult.setTotalElements(totalSize);
        }
        return pageResult;
    }


    @Override
    public List<DevopsBranchE> listDevopsBranchesByAppIdAndBranchName(Long appId, String branchName) {
        DevopsBranchDO devopsBranchDO = new DevopsBranchDO();
        devopsBranchDO.setAppId(appId);
        devopsBranchDO.setBranchName(branchName);
        return ConvertHelper.convertList(devopsBranchMapper.select(devopsBranchDO), DevopsBranchE.class);
    }

    @Override
    public List<DevopsBranchE> listDevopsBranchesByAppId(Long appId) {
        DevopsBranchDO devopsBranchDO = new DevopsBranchDO();
        devopsBranchDO.setAppId(appId);
        return ConvertHelper.convertList(devopsBranchMapper.select(devopsBranchDO), DevopsBranchE.class);
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
}
