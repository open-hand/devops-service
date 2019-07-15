package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;

import io.choerodon.devops.infra.dto.DevopsGitlabCommitDTO;
import org.apache.ibatis.annotations.Param;

import io.choerodon.mybatis.common.Mapper;


public interface DevopsGitlabCommitMapper extends Mapper<DevopsGitlabCommitDTO> {
    List<DevopsGitlabCommitDTO> listCommits(@Param("projectId") Long projectId,
                                            @Param("appIds") List<Long> appId,
                                            @Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate);


    List<DevopsGitlabCommitDTO> queryByAppIdAndBranch(
            @Param("appId") Long appId,
            @Param("branchName") String branchName,
            @Param("startDate") Date startDate);
}
