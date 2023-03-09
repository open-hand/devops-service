package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

import io.choerodon.devops.api.vo.CiCdPipelineVO;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface CiCdPipelineMapper extends BaseMapper<CiCdPipelineDTO> {
    /**
     * 停用流水线
     */
    int disablePipeline(@Param("ciCdPipelineId") Long ciCdPipelineId);

    /**
     * 启用流水线
     */
    int enablePipeline(@Param("ciCdPipelineId") Long ciCdPipelineId);

    List<CiCdPipelineVO> queryByProjectIdAndName(@Param("projectId") Long projectId,
                                                 @Param("appServiceIds") Set<Long> appServiceIds,
                                                 @Param("searchParam") String searchParam,
                                                 @Param("enableFlag") Boolean enableFlag,
                                                 @Param("status") String status,
                                                 @Param("excludedPipelineId") Long excludedPipelineId,
                                                 @Param("sortStr") String sortStr);

    List<String> listPipelineNameByTaskConfigId(@Param("taskConfigId") Long taskConfigId);
}
