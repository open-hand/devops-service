package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.BranchSagaPayLoad;
import io.choerodon.devops.infra.dto.gitlab.BranchDTO;


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
     * @param appId
     * @return
     */
    String queryUrl(Long projectId, Long appId);

    /**
     * 创建标签
     *
     * @param projectId
     * @param appId
     * @param tag
     * @param ref
     * @param msg
     * @param releaseNotes
     */
    void createTag(Long projectId, Long appId, String tag, String ref, String msg, String releaseNotes);

    /**
     * 更新标签
     *
     * @param projectId
     * @param appId
     * @param tag
     * @param releaseNotes
     * @return
     */
    TagVO updateTag(Long projectId, Long appId, String tag, String releaseNotes);

    /**
     * 删除标签
     *
     * @param projectId
     * @param appId
     * @param tag
     */
    void deleteTag(Long projectId, Long appId, String tag);

    /**
     * 创建分支
     *
     * @param projectId      项目ID
     * @param applicationId  应用ID
     * @param devopsBranchVO 分支
     */
    void createBranch(Long projectId, Long applicationId, DevopsBranchVO devopsBranchVO);

    /**
     * 获取工程下所有分支名
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @param pageRequest   分页参数
     * @param params        search param
     * @return Page
     */
    PageInfo<BranchVO> pageBranchByOptions(Long projectId, PageRequest pageRequest, Long applicationId, String params);

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
     * @param projectId      项目 ID
     * @param applicationId  应用ID
     * @param devopsBranchVO 分支
     */
    void updateBranchIssue(Long projectId, Long applicationId, DevopsBranchVO devopsBranchVO);

    /**
     * 删除分支
     *
     * @param applicationId 应用ID
     * @param branchName    分支名
     */
    void deleteBranch(Long applicationId, String branchName);

    /**
     * 校验分支名唯一性
     *
     * @param projectId     项目id
     * @param applicationId 应用id
     * @param branchName    分支名
     */
    void checkBranchName(Long projectId, Long applicationId, String branchName);

    /**
     * 查看所有合并请求
     *
     * @param projectId
     * @param applicationId
     * @param state
     * @param pageRequest
     * @return
     */
    MergeRequestTotalVO listMergeRequest(Long projectId, Long applicationId, String state, PageRequest pageRequest);

    /**
     * 分页获取标签列表
     *
     * @param projectId
     * @param applicationId
     * @param params
     * @param page
     * @param size
     * @return
     */
    PageInfo<TagVO> pageTagsByOptions(Long projectId, Long applicationId, String params, Integer page, Integer size);

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


    BranchDTO baseQueryBranch(Integer gitLabProjectId, String branchName);

}
