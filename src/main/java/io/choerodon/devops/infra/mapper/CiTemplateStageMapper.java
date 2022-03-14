package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.CiTemplateStageDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线模阶段(CiTemplateStage)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:20
 */
public interface CiTemplateStageMapper extends BaseMapper<CiTemplateStageDTO> {

    List<CiTemplateStageDTO> listByPipelineTemplateIds(@Param("pipelineTemplateIds") Set<Long> pipelineTemplateIds);
}

