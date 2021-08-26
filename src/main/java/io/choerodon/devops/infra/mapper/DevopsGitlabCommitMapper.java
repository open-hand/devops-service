package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.LatestAppServiceVO;
import io.choerodon.devops.infra.dto.DevopsBranchDTO;
import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;
import io.choerodon.mybatis.common.BaseMapper;


public interface DevopsGitlabCommitMapper extends BaseMapper<DevopsGitlabCommitDTO> {
    List<DevopsGitlabCommitDTO> listCommits(@Param("projectId") Long projectId,
                                            @Param("appServiceIds") List<Long> appServiceIds,
                                            @Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate);


    List<DevopsGitlabCommitDTO> queryByAppIdAndBranch(
            @Param("appServiceId") Long appServiceId,
            @Param("branchName") String branchName,
            @Param("startDate") Date startDate);

    void deleteByAppServiceId(@Param("appServiceId") Long appServiceId);

    List<LatestAppServiceVO> listLatestUseAppServiceIdAndDate(@Param("projectIds") List<Long> projectIds,
                                                              @Param("userId") Long userId,
                                                              @Param("time") java.util.Date time);

    List<java.util.Date> queryCountByProjectIdAndDate(@Param("projectId") Long projectId,
                                                      @Param("startDate") java.util.Date startDate,
                                                      @Param("endDate") Date endDate);

    List<DevopsGitlabCommitDTO> listUserRecentCommits(@Param("projectIds") List<Long> projectIds,
                                                      @Param("userId") Long userId,
                                                      @Param("date") Date date);

    Set<Long> listIdsByCommitSha(@Param("commitSha") Set<String> commitSha);

    void removeIssueAssociation(@Param("appServiceId") Long appServiceId, @Param("branchName") String branchName, @Param("issueId") Long issueId);


    int countBranchBoundWithIssue();

    List<DevopsGitlabCommitDTO> listCommitBoundWithIssue();

    List<DevopsBranchDTO> baseListDevopsBranchesByIssueId(Long issueId);
}
