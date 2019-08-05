package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.PipelineDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:26 2019/4/4
 * Description:
 */
public interface PipelineMapper extends Mapper<PipelineDTO> {
    List<PipelineDTO> listByOptions(@Param("projectId") Long projectId,
                                    @Param("searchParam") Map<String, Object> searchParam,
                                    @Param("param") String param,
                                    @Param("index") String index,
                                    @Param("classifyParam") Map<String, Object> classifyParam);
}
