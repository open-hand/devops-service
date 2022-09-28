package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.PipelineTemplateVO;
import io.choerodon.devops.infra.dto.PipelineTemplateDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线模板表(PipelineTemplate)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 16:54:19
 */
public interface PipelineTemplateMapper extends BaseMapper<PipelineTemplateDTO> {

    List<PipelineTemplateVO> listTemplateForProject(@Param("projectId") Long projectId, @Param("organizationId") Long organizationId);
}

