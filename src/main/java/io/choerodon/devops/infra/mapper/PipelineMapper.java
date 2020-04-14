package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.PipelineSearchVO;
import io.choerodon.devops.infra.dto.PipelineDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:26 2019/4/4
 * Description:
 */
public interface PipelineMapper extends BaseMapper<PipelineDTO> {
    List<PipelineDTO> listByOptions(@Param("projectId") Long projectId,
                                    @Param("pipelineSearchVO") PipelineSearchVO pipelineSearchVO,
                                    @Param("userId") Long userId,
                                    @Param("sort") String sort);

    List<Long> listEnvIdByPipelineId(@Param("pipelineId") Long pipelineId);

    /**
     * 通过流水线id,查询该流水线中的部署任务属于哪个环境
     *
     * @param pipelineId
     * @return
     */
    List<PipelineDTO> selectByProjectId(@Param("pipelineId") Long pipelineId);
}
