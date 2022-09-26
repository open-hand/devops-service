package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiPipelineMavenDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author scp
 * @date 2020/7/22
 * @description
 */
public interface CiPipelineMavenMapper extends BaseMapper<CiPipelineMavenDTO> {

    CiPipelineMavenDTO queryPipelineLatestMaven(@Param("appServiceId") Long appServiceId,
                                                @Param("gitlabPipelineId") Long gitlabPipelineId);
}
