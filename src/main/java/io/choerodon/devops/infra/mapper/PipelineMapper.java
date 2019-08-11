package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.base.domain.Sort;
import io.choerodon.devops.api.vo.PipelineSearchVO;
import io.choerodon.devops.infra.dto.PipelineDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:26 2019/4/4
 * Description:
 */
public interface PipelineMapper extends Mapper<PipelineDTO> {
    List<PipelineDTO> listByOptions(@Param("projectId") Long projectId,
                                    @Param("pipelineSearchVO") PipelineSearchVO pipelineSearchVO,
                                    @Param("userId") Long userId,
                                    @Param("sort") String sort);
}
