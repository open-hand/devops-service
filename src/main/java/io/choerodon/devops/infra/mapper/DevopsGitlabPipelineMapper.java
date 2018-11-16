package io.choerodon.devops.infra.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsGitlabPipelineMapper extends BaseMapper<DevopsGitlabPipelineDO> {

    List<DevopsGitlabPipelineDO> listDevopsGitlabPipeline(@Param("appId") Long appId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    void deleteWithoutCommit();

}
