package io.choerodon.devops.infra.mapper;

import java.util.Date;
import java.util.List;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDO;

public interface DevopsGitlabPipelineMapper extends Mapper<DevopsGitlabPipelineDO> {

    List<DevopsGitlabPipelineDO> listDevopsGitlabPipeline(@Param("appId") Long appId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    void deleteWithoutCommit();

    List<DevopsGitlabPipelineDO> listByBranch(@Param("appId") Long appId, @Param("branch") String branch);

}
