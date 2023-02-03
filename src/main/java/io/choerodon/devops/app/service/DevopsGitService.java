package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.BranchSagaPayLoad;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;


/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:39
 * Description:
 */
public interface DevopsGitService {

    /**
     * 获取工程下地址
     *
     * @param projectId
     * @param appServiceId
     * @return
     */
    String queryUrl(Long projectId, Long appServiceId);

    /**
     * 创建标签
     *
     * @param projectId
     * @param appServiceId
     * @param tag
     * @param ref
     * @param msg
     * @param releaseNotes
     */
    void createTag(Long projectId, Long appServiceId, String tag, String ref, String msg, String releaseNotes);

    /**
     * 更新标签
     *
     * @param projectId
     * @param appServiceId
     * @param tag
     * @param releaseNotes
     * @return
     */
    void updateTag(Long projectId, Long appServiceId, String tag, String releaseNotes);

    /**
     * 删除标签
     *
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     * @param tag          tag名称
     */
    void deleteTag(Long projectId, Long appServiceId, String tag);

    /**
     * 创建分支
     *
     * @param projectId      项目ID
     * @param appServiceId   应用服务ID
     * @param devopsBranchVO 分支
     */
    void createBranch(Long projectId, Long appServiceId, DevopsBranchVO devopsBranchVO);

    /**
     * 获取工程下所有分支名
     *
     * @param projectId        项目 ID
     * @param appServiceId     应用服务ID
     * @param pageable         分页参数
     * @param params           search param
     * @param currentProjectId 当前所处项目id
     * @return Page
     */
    Page<BranchVO> pageBranchByOptions(Long projectId, PageRequest pageable, Long appServiceId, String params, Long currentProjectId);

    /**
     * 查询单个分支
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @param branchName    分支名
     * @return BranchUpdateDTO
     */
    DevopsBranchVO queryBranch(Long projectId, Long applicationId, String branchName);

    /**
     * 更新分支关联的问题
     *
     * @param projectId            项目ID
     * @param appServiceId         应用服务ID
     * @param devopsBranchUpdateVO 分支更新信息
     * @param onlyInsert           表示此次操作是否为插入
     */
    void updateBranchIssue(Long projectId, Long appServiceId, DevopsBranchUpdateVO devopsBranchUpdateVO, boolean onlyInsert);

    /**
     * 删除分支
     *
     * @param projectId    项目id
     * @param appServiceId 应用服务ID
     * @param branchName   分支名
     */
    void deleteBranch(Long projectId, Long appServiceId, String branchName);

    /**
     * 判断分支名唯一性
     *
     * @param projectId     项目id
     * @param applicationId 应用id
     * @param branchName    分支名
     * @return true表示通过
     */
    Boolean isBranchNameUnique(Long projectId, Long applicationId, String branchName);

    /**
     * 查看所有合并请求
     *
     * @param projectId
     * @param appServiceId
     * @param state
     * @param pageable
     * @return
     */
    MergeRequestTotalVO listMergeRequest(Long projectId, Long appServiceId, String state, PageRequest pageable);

    /**
     * 分页获取标签列表
     *
     * @param projectId
     * @param applicationId
     * @param params
     * @param page
     * @param size
     * @param checkMember   是否校验gitlab项目角色
     * @return
     */
    Page<TagVO> pageTagsByOptions(Long projectId, Long applicationId, String params, Integer page, Integer size, Boolean checkMember);

    /**
     * 获取标签列表
     *
     * @param projectId
     * @param applicationId
     * @return
     */
    List<TagVO> listTags(Long projectId, Long applicationId);

    /**
     * 检查标签
     *
     * @param projectId
     * @param applicationId
     * @param tagName
     * @return
     */
    Boolean checkTag(Long projectId, Long applicationId, String tagName);

    /**
     * @param pushWebHookVO
     * @param token
     */
    void branchSync(PushWebHookVO pushWebHookVO, String token);

    /**
     * @param pushWebHookVO
     */
    void fileResourceSync(PushWebHookVO pushWebHookVO);

    /**
     * @param pushWebHookVO
     * @param token
     */
    void fileResourceSyncSaga(PushWebHookVO pushWebHookVO, String token);

    /**
     * @param branchSagaDTO
     */
    void createBranchBySaga(BranchSagaPayLoad branchSagaDTO);

    /**
     * 查询
     *
     * @param projectId
     * @param pageable
     * @param appServiceId
     * @param params
     * @param issueId
     * @return
     */
    Page<BranchVO> pageBranchFilteredByIssueId(Long projectId, PageRequest pageable, Long appServiceId, String params, Long issueId);

    /**
     * 删除分支和问题关联关系
     *
     * @param projectId    项目id
     * @param appServiceId 应用id
     * @param branchId     分支id
     * @param issueId      关联问题id
     */
    void removeAssociation(Long projectId, Long appServiceId, Long branchId, Long issueId);


    /**
     * 获取服务两个tag之间的issueId列表
     *
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     * @param from         前一个tag
     * @param to           后一个tag
     * @return 所有的issueId
     */
    List<IssueIdAndBranchIdsVO> getIssueIdsBetweenTags(Long projectId, Long appServiceId, String from, String to);

    List<GroupDTO> listOwnedGroupExpectCurrent(Long projectId, String search);

    Page<GitlabProjectDTO> listOwnedProjectByGroupId(Long projectId, Integer gitlabGroupId, String search, PageRequest pageRequest);

    Page<BranchVO> pageBranchBasicInfoByOptions(Long projectId, PageRequest pageable, Long appServiceId, String params);

    Integer syncBranch(Long projectId, Long appServiceId, Boolean sync);

    Integer syncOpenMergeRequest(Long projectId, Long appServiceId, Boolean sync);
}
