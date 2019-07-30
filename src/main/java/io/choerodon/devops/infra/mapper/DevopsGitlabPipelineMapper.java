package io.choerodon.devops.infra.mapper;

import java.util.Date;
import java.util.List;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDTO;

public interface DevopsGitlabPipelineMapper extends Mapper<DevopsGitlabPipelineDTO> {

    List<DevopsGitlabPipelineDTO> listDevopsGitlabPipeline(@Param("appServiceId") Long appServiceId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    void deleteWithoutCommit();

    List<DevopsGitlabPipelineDTO> listByBranch(@Param("appServiceId") Long appServiceId, @Param("branch") String branch);

}
