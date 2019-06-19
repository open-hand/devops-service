package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;

import io.choerodon.devops.infra.dataobject.DevopsGitlabCommitDO;
import org.apache.ibatis.annotations.Param;

import io.choerodon.mybatis.common.Mapper;


public interface DevopsGitlabCommitMapper extends Mapper<DevopsGitlabCommitDO> {
    List<DevopsGitlabCommitDO> listCommits(@Param("projectId") Long projectId,
                                           @Param("appIds") List<Long> appId,
                                           @Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate);


    List<DevopsGitlabCommitDO> queryByAppIdAndBranch(
            @Param("appId") Long appId,
            @Param("branchName") String branchName,
            @Param("startDate") Date startDate);
}
