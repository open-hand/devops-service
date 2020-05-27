package io.choerodon.devops.infra.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsGitlabPipelineDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsGitlabPipelineMapper extends BaseMapper<DevopsGitlabPipelineDTO> {

    List<DevopsGitlabPipelineDTO> listDevopsGitlabPipeline(@Param("appServiceId") Long appServiceId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    void deleteWithoutCommit();

    List<DevopsGitlabPipelineDTO> listByBranch(@Param("appServiceId") Long appServiceId, @Param("branch") String branch);

    void deleteByAppServiceId(@Param("appServiceId") Long appServiceId);


    DevopsGitlabPipelineDTO selectLatestPipline(@Param("appServiceId") Long appServiceId, @Param("key") String key);
}
