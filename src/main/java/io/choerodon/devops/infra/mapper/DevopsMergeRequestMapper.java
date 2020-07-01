package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.LatestAppServiceVO;
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO;
import io.choerodon.mybatis.common.BaseMapper;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

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
}
