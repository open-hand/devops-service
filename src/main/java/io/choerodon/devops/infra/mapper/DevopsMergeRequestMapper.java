package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

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
}
