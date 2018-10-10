package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import feign.FeignException;
import io.kubernetes.client.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.domain.application.entity.gitlab.CompareResultsE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.infra.common.util.GitUserNameUtil;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.devops.infra.dataobject.DevopsBranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO;
import io.choerodon.devops.infra.dataobject.gitlab.TagDO;
import io.choerodon.devops.infra.dataobject.gitlab.TagNodeDO;
import io.choerodon.devops.infra.feign.GitlabServiceClient;
import io.choerodon.devops.infra.mapper.ApplicationMapper;
import io.choerodon.devops.infra.mapper.DevopsBranchMapper;
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
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

    private JSON json = new JSON();

    @Value("${services.gitlab.url}")
    private String gitlabUrl;

    @Autowired
    private GitlabServiceClient gitlabServiceClient;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private DevopsBranchMapper devopsBranchMapper;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DevopsGitRepository devopsGitRepository;
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper;
    @Autowired
    private DevopsMergeRequestRepository devopsMergeRequestRepository;

    @Override
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

    @Override
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

    @Override
    public void deleteTag(Integer gitLabProjectId, String tag, Integer userId) {
        try {
            gitlabServiceClient.deleteTag(gitLabProjectId, tag, userId);
        } catch (FeignException e) {
            throw new CommonException("delete gitlab tag failed: " + e.getMessage(), e);
        }
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
    public Long getUserIdByGitlabUserId(Long gitLabUserId) {
        try {
            return userAttrRepository.queryUserIdByGitlabUserId(gitLabUserId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getGitlabUrl(Long projectId, Long appId) {
        ApplicationE applicationE = applicationRepository.query(appId);
        if (applicationE.getGitlabProjectE() != null && applicationE.getGitlabProjectE().getId() != null) {
            ProjectE projectE = iamRepository.queryIamProject(projectId);
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
            return gitlabUrl + urlSlash
                    + organization.getCode() + "-" + projectE.getCode() + "/"
                    + applicationE.getCode();
        }
        return "";
    }

    @Override
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

    @Override
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

    @Override
    public Page<DevopsBranchE> listBranches(Long appId, PageRequest pageRequest, String params) {

        Page<DevopsBranchDO> devopsBranchDOS;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = json.deserialize(params, Map.class);
            if (maps.get(TypeUtil.SEARCH_PARAM).equals("")) {
                devopsBranchDOS = PageHelper.doPageAndSort(
                        pageRequest, () -> devopsBranchMapper.list(
                                appId, null,
                                TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            } else {
                devopsBranchDOS = PageHelper.doPageAndSort(
                        pageRequest, () -> devopsBranchMapper.list(
                                appId, TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM)),
                                TypeUtil.cast(maps.get(TypeUtil.PARAM))));
            }
        } else {
            devopsBranchDOS = PageHelper.doPageAndSort(
                    pageRequest, () -> devopsBranchMapper.list(appId, null, null));
        }
        return ConvertPageHelper.convertPage(devopsBranchDOS, DevopsBranchE.class);
    }

    @Override
    public void deleteBranch(Integer projectId, String branchName, Integer userId) {
        try {
            gitlabServiceClient.deleteBranch(projectId, branchName, userId);
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public void deleteDevopsBranch(Long appId, String branchName) {
        DevopsBranchDO devopsBranchDO = devopsBranchMapper
                .queryByAppAndBranchName(appId, branchName);
        devopsBranchDO.setDeleted(true);
        devopsBranchMapper.updateByPrimaryKeySelective(devopsBranchDO);
    }

    @Override
    public Page<TagDTO> getTags(Long appId, String path, Integer page, String params, Integer size, Integer userId) {
        Integer projectId = getGitLabId(appId);
        List<TagDO> tagTotalList = getGitLabTags(projectId, userId);
        Page<TagDTO> tagsPage = new Page<>();
        List<TagDO> tagList = tagTotalList.stream()
                .filter(t -> filterTag(t, params))
                .collect(Collectors.toCollection(ArrayList::new));
        int totalPageSizes = tagList.size() / size + (tagList.size() % size == 0 ? 0 : 1);
        if (page > totalPageSizes - 1 && page > 0) {
            page = totalPageSizes - 1;
        }
        List<TagDTO> tagDTOS = tagList.stream()
                .sorted(this::sortTag)
                .skip(page.longValue() * size).limit(size)
                .map(TagDTO::new)
                .parallel()
                .peek(t -> {
                    UserE commitUserE = iamRepository.queryByLoginName(t.getCommit()
                            .getAuthorName().equals("root") ? "admin" : t.getCommit().getAuthorName());
                    t.setCommitUserImage(commitUserE.getImageUrl());
                    t.getCommit().setUrl(String.format("%s/commit/%s?view=parallel", path, t.getCommit().getId()));
                })
                .collect(Collectors.toCollection(ArrayList::new));
        tagsPage.setSize(size);
        tagsPage.setTotalElements(tagList.size());
        tagsPage.setTotalPages(totalPageSizes);
        tagsPage.setContent(tagDTOS);
        tagsPage.setNumber(page);
        tagsPage.setNumberOfElements(tagDTOS.size());
        return tagsPage;
    }

    private Boolean filterTag(TagDO tagDO, String params) {
        Integer index = 0;
        if (!StringUtils.isEmpty(params)) {
            Map<String, Object> maps = json.deserialize(params, Map.class);
            String param = TypeUtil.cast(maps.get(TypeUtil.PARAM)).toString();
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

    @Override
    public List<TagDO> getTagList(Long appId, Integer userId) {
        Integer projectId = getGitLabId(appId);
        return getGitLabTags(projectId, userId);
    }

    @Override
    public List<TagDO> getGitLabTags(Integer projectId, Integer userId) {
        ResponseEntity<List<TagDO>> tagResponseEntity;
        try {
            tagResponseEntity = gitlabServiceClient.getTags(projectId, userId);
        } catch (FeignException e) {
            throw new CommonException("error.tags.get", e);
        }
        return tagResponseEntity.getBody();
    }


    @Override
    public BranchDO getBranch(Integer gitlabProjectId, String branch) {
        try {
            return gitlabServiceClient.getBranch(gitlabProjectId, branch).getBody();
        } catch (FeignException e) {
            throw new CommonException("error.branch.get", e);

        }
    }

    @Override
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
    public void createDevopsBranch(DevopsBranchE devopsBranchE) {
        devopsBranchE.setDeleted(false);
        devopsBranchMapper.insert(ConvertHelper.convert(devopsBranchE, DevopsBranchDO.class));
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
        Page<DevopsMergeRequestE> page = devopsMergeRequestRepository
                .getByGitlabProjectId(gitLabProjectId, pageRequest);
        if (StringUtil.isNotEmpty(state)) {
            page = devopsMergeRequestRepository
                    .getMergeRequestList(gitLabProjectId, state, pageRequest);
        }
        List<MergeRequestDTO> pageContent = new ArrayList<>();
        List<DevopsMergeRequestE> content = page.getContent();
        if (content != null && !content.isEmpty()) {
            content.forEach(devopsMergeRequestE -> {
                MergeRequestDTO mergeRequestDTO = devopsMergeRequestToMergeRequest(
                        devopsMergeRequestE);
                pageContent.add(mergeRequestDTO);
            });
        }
        int total = count[0] + count[1] + count[2];
        Page<MergeRequestDTO> pageResult = new Page<>();
        BeanUtils.copyProperties(page, pageResult);
        pageResult.setContent(pageContent);
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
        List<CommitDO> commitDOS = gitlabServiceClient.listCommits(
                devopsMergeRequestE.getProjectId().intValue(),
                gitlabMergeRequestId.intValue(), gitlabUserId).getBody();
        mergeRequestDTO.setCommits(ConvertHelper.convertList(commitDOS, CommitDTO.class));
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

    @Override
    public DevopsBranchE queryByBranchNameAndCommit(String branchName, String commit) {
        DevopsBranchDO devopsBranchDO = new DevopsBranchDO();
        devopsBranchDO.setBranchName(branchName);
        devopsBranchDO.setCheckoutCommit(commit);
        return ConvertHelper.convert(devopsBranchMapper.selectOne(devopsBranchDO), DevopsBranchE.class);
    }

    @Override
    public CommitE getCommit(Integer gitLabProjectId, String commit, Integer userId) {
        CommitE commitE = new CommitE();
        BeanUtils.copyProperties(
                gitlabServiceClient.getCommit(gitLabProjectId, commit, userId).getBody(),
                commitE);
        return commitE;
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

    @Override
    public List<CommitDO> getCommits(Integer gitLabProjectId, String branchName, String date) {
        try {
            return gitlabServiceClient.getCommits(gitLabProjectId, branchName, date).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public List<BranchDO> listBranches(Integer gitlabProjectId, Integer userId) {
        try {
            return gitlabServiceClient.listBranches(gitlabProjectId, userId).getBody();
        } catch (FeignException e) {
            throw new CommonException(e);
        }
    }
}
