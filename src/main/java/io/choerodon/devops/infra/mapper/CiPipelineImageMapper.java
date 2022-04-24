package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiPipelineImageDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * @author scp
 * @date 2020/7/21
 * @description
 */
public interface CiPipelineImageMapper extends BaseMapper<CiPipelineImageDTO> {

    CiPipelineImageDTO queryPipelineLatestImage(@Param("appServiceId") Long appServiceId, @Param("gitlabPipelineId") Long gitlabPipelineId);
}
