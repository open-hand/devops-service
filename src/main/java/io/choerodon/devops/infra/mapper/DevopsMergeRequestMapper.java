package io.choerodon.devops.infra.mapper;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.LatestAppServiceVO;
import io.choerodon.devops.api.vo.MergeRequestVO;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/9
 * Time: 14:31
 * Description:
 */
public interface DevopsMergeRequestMapper extends BaseMapper<DevopsMergeRequestDTO> {
    List<DevopsMergeRequestDTO> getByProjectIdAndState(@Param("projectId") Integer gitLabProjectId,
                                                       @Param("state") String state);

    List<DevopsMergeRequestDTO> listBySourceBranch(@Param("projectId") Integer gitLabProjectId, @Param("branchName") String branchName);

    List<DevopsMergeRequestDTO> listToBeAuditedByThisUser(@Param("projectId") Integer gitlabProjectId,
                                                          @Param("iamUserId") Long iamUserId);

    DevopsMergeRequestDTO countMergeRequest(@Param("projectId") Integer gitlabProjectId,
                                            @Param("iamUserId") Long iamUserId);

    void deleteByProjectId(@Param("projectId") Integer projectId);

    List<DevopsMergeRequestDTO> listToBeAuditedByThisUserUnderProjectIds(@Param("gitlabProjectIds") List<Integer> gitlabProjectIds,
                                                                         @Param("iamUserId") Long iamUserId);

    List<LatestAppServiceVO> listLatestUseAppServiceIdAndDate(@Param("projectIds") List<Long> projectIds,
                                                              @Param("userId") Long userId,
                                                              @Param("time") Date time);

    /**
     * 删除请求id不在列表中的合并请求
     *
     * @param gitlabProjectId
     * @param mergeRequestIds
     */
    void deleteByGitlabProjectIdAndMergeRequestIdNotInIds(@Param("gitlabProjectId") Integer gitlabProjectId,
                                                          @Param("ids") List<Long> mergeRequestIds);

    /**
     * 根据合并请求id进行删除
     *
     * @param gitlabProjectId
     * @param mergeRequestIds
     */
    void deleteByGitlabProjectIdAndMergeRequestId(@Param("gitlabProjectId") Integer gitlabProjectId,
                                                  @Param("ids") Long mergeRequestIds);

    /**
     * 查询指定分支中，已合并的分支数量
     *
     * @param branchNames 所有的分支名称
     * @return
     */
    Integer countMergedBranchesByName(@Param("branchNames") Set<String> branchNames, @Param("gitlabProjectId") Long gitlabProjectId);

    List<MergeRequestVO> listMergeRequestToBeChecked(@Param("projectId") Long projectId, @Param("appServiceIds") Set<Long> appServiceIds, @Param("param") String param);
}
