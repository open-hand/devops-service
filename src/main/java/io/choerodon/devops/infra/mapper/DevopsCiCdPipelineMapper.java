package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.CiCdPipelineVO;
import io.choerodon.devops.api.vo.PipelineInstanceReferenceVO;
import io.choerodon.devops.infra.dto.CiCdPipelineDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 〈功能简述〉
 * 〈Ci流水线Mapper〉
 *
 * @author wanghao
 * @since 2020/4/2 18:01
 */
public interface DevopsCiCdPipelineMapper extends BaseMapper<CiCdPipelineDTO> {

    /**
     * 根据id查询流水线（包含关联应用服务name,gitlab_project_id）
     */
    CiCdPipelineVO queryById(@Param("ciPipelineId") Long ciPipelineId);

    /**
     * 停用流水线
     */
    int disablePipeline(@Param("ciPipelineId") Long ciPipelineId);

    /**
     * 启用流水线
     */
    int enablePipeline(@Param("ciPipelineId") Long ciPipelineId);

    /**
     * 根据token查询流水线
     *
     * @param token 流水线的token
     * @return 流水线数据
     */
    CiCdPipelineDTO queryByToken(@Param("token") String token);

    List<CiCdPipelineDTO> selectPipelineByProjectId(@Param("project_id") Long projectId);

    List<PipelineInstanceReferenceVO> listChartEnvReferencePipelineInfo(@Param("projectId") Long projectId,
                                                                        @Param("envId") Long envId);

    List<PipelineInstanceReferenceVO> listDeployEnvReferencePipelineInfo(@Param("projectId") Long projectId,
                                                                         @Param("envId") Long envId);

    List<PipelineInstanceReferenceVO> listConfigFileReferencePipelineInfo(@Param("projectId") Long projectId,
                                                                          @Param("configFileId") Long configFileId);
}
