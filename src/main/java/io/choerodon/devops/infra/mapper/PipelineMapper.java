package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.PipelineHomeVO;
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
                                                     @Param("enable") Boolean enable,
                                                     @Param("status") String status,
                                                     @Param("triggerType") String triggerType,
                                                     @Param("param") String param);

    Boolean checkName(@Param("projectId") Long projectId,
                      @Param("id") Long id,
                      @Param("name") String name);
}

