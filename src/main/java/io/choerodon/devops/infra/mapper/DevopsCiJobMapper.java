package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.DevopsCiJobVO;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:20
 */
public interface DevopsCiJobMapper extends BaseMapper<DevopsCiJobDTO> {

    List<DevopsCiJobVO> listCustomByPipelineId(@Param("ciPipelineId") Long ciPipelineId);

    void updateImageByIds(@Param("ids") List<Long> longList, @Param("image") String sonarImage);

    Boolean doesApiTestSuiteRelatedWithPipeline(@Param("projectId") Long projectId, @Param("suiteId") Long suiteId);

    List<PipelineInstanceReferenceVO> listApiTestTaskReferencePipelineInfo(@Param("projectId") Long projectId, @Param("taskIds") Set<Long> taskIds);

    PipelineInstanceReferenceVO queryPipelineReferenceEnvApp(@Param("projectId") Long projectId, @Param("appId") Long appId);

    PipelineInstanceReferenceVO queryChartPipelineReference(@Param("projectId") Long projectId, @Param("appId") Long appId);

    PipelineInstanceReferenceVO queryPipelineReferenceHostApp(@Param("appId") Long appId);

    PipelineInstanceReferenceVO queryDeployValuePipelineReference(@Param("projectId") Long projectId,
                                                                  @Param("valueId") Long valueId);
}
