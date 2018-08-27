package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.BranchDTO;
import io.choerodon.devops.api.dto.DevopsBranchDTO;
import io.choerodon.devops.api.dto.PushWebHookDTO;
import io.choerodon.devops.api.dto.TagDTO;
import io.choerodon.devops.infra.dataobject.gitlab.TagDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: Runge
 * Date: 2018/7/2
 * Time: 14:39
 * Description:
 */
public interface DevopsGitService {

    String getUrl(Long projectId, Long appId);

    void createTag(Long projectId, Long appId, String tag, String ref);

    void deleteTag(Long projectId, Long appId, String tag);

    /**
     * 创建分支
     *
     * @param projectId       项目ID
     * @param applicationId   应用ID
     * @param devopsBranchDTO 分支
     */
    void createBranch(Long projectId, Long applicationId, DevopsBranchDTO devopsBranchDTO);

    /**
     * 获取工程下所有分支名
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @param pageRequest   分页参数
     * @param params        search param
     * @return Page
     */
    Page<BranchDTO> listBranches(Long projectId, PageRequest pageRequest, Long applicationId, String params);

    /**
     * 查询单个分支
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @param branchName    分支名
     * @return BranchUpdateDTO
     */
    DevopsBranchDTO queryBranch(Long projectId, Long applicationId, String branchName);

    /**
     * 更新分支关联的问题
     *
     * @param projectId       项目 ID
     * @param applicationId   应用ID
     * @param devopsBranchDTO 分支
     */
    void updateBranch(Long projectId, Long applicationId, DevopsBranchDTO devopsBranchDTO);

    /**
     * 删除分支
     *
     * @param projectId     项目 ID
     * @param applicationId 应用ID
     * @param branchName    分支名
     */
    void deleteBranch(Long projectId, Long applicationId, String branchName);

    Map<String, Object> getMergeRequestList(Long projectId, Long aplicationId, String state, PageRequest pageRequest);

    Page<TagDTO> getTags(Long projectId, Long applicationId, String params, Integer page, Integer size);

    List<TagDO> getTags(Long projectId, Long applicationId);

    Boolean checkTag(Long projectId, Long applicationId, String tagName);

    void branchSync(PushWebHookDTO pushWebHookDTO, String token);

    void fileResourceSync(PushWebHookDTO pushWebHookDTO);

    void fileResourceSyncSaga(PushWebHookDTO pushWebHookDTO, String token);

    void initMockService(SagaClient sagaClient, ApplicationInstanceService applicationInstanceService,DevopsServiceService devopsServiceService, DevopsIngressService devopsIngressService);
}
