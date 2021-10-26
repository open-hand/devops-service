package io.choerodon.devops.infra.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.LatestAppServiceVO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsBranchMapper extends BaseMapper<DevopsBranchDTO> {

    DevopsBranchDTO queryByAppAndBranchName(@Param("appServiceId") Long appServiceId, @Param("branchName") String name);

    List<DevopsBranchDTO> list(@Param("appServiceId") Long appServiceId,
                               @Param("sortString") String sortString,
                               @Param("searchParam") Map<String, Object> searchParam,
                               @Param("params") List<String> params,
                               @Param("issueId") Long issueId);


    void deleteByIsDelete();

    void deleteDuplicateBranch();

    void deleteByAppServiceId(@Param("appServiceId") Long appServiceId);

    void updateBranchById(@Param("devopsBranchDTO") DevopsBranchDTO devopsBranchDTO);

    List<LatestAppServiceVO> listLatestUseAppServiceIdAndDate(@Param("projectIds") List<Long> projectIds,
                                                              @Param("userId") Long userId,
                                                              @Param("time") Date time);

    List<DevopsBranchDTO> listByIssueIdAndOrderByProjectId(@Param("issueId") Long issueId);

    List<DevopsBranchDTO> listBranchBoundWithIssue();

    int countBranchBoundWithIssue();

    DevopsBranchDTO queryByAppAndBranchNameWithIssueIds(@Param("appServiceId") Long appServiceId, @Param("branchName") String branchName);

    DevopsBranchDTO queryByAppAndBranchIdWithIssueId(@Param("appServiceId") Long appServiceId, @Param("branchId") Long branchId);

    List<DevopsBranchDTO> listByCommitIds(@Param("commitIds") List<Long> commitIds);

    List<Long> listExistBranchIds(@Param("branchIds") Set<Long> branchIds);

    List<DevopsBranchDTO> listByIds(@Param("branchIds") List<Long> branchIds);
}
