package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.PipelineHomeVO;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.infra.dto.PipelineDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线表(Pipeline)应用服务
 *
 * @author
 * @since 2022-11-24 15:50:12
 */
public interface PipelineMapper extends BaseMapper<PipelineDTO> {

    Page<PipelineHomeVO> pagingByProjectIdAndOptions(@Param("projectId") Long projectId,
                                                     @Param("name") String name,
                                                     @Param("enable") Boolean enable,
                                                     @Param("status") String status,
                                                     @Param("triggerType") String triggerType,
                                                     @Param("params") String params);

    Boolean checkName(@Param("projectId") Long projectId,
                      @Param("id") Long id,
                      @Param("name") String name);

    Set<Long> listAppAssociatedPipeline(@Param("appServiceId") Long appServiceId);

    List<PipelineInstanceReferenceVO> listDeployValuePipelineReference(@Param("projectId") Long projectId,
                                                                       @Param("valueId") Long valueId);

    List<PipelineInstanceReferenceVO> listChartEnvReferencePipelineInfo(@Param("projectId") Long projectId,
                                                                        @Param("envId") Long envId);

    int countAppServicePipelineReference(@Param("projectId") Long projectId,
                                         @Param("appServiceId") Long appServiceId);

    List<PipelineInstanceReferenceVO> listAppPipelineReference(@Param("projectId") Long projectId,
                                                               @Param("appId") Long appId);
}

