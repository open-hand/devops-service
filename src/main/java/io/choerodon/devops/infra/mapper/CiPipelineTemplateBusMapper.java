package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.template.CiTemplatePipelineVO;
import io.choerodon.devops.infra.dto.CiTemplatePipelineDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线任务模板分组(CiTemplateJobGroup)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:16
 */
public interface CiPipelineTemplateBusMapper extends BaseMapper<CiTemplatePipelineDTO> {

    List<CiTemplatePipelineVO> queryDevopsPipelineTemplateByParams(@Param("sourceId") Long sourceId,
                                                                   @Param("sourceType") String sourceType,
                                                                   @Param("organizationId") Long organizationId,
                                                                   @Param("name") String name,
                                                                   @Param("categoryName") String categoryName,
                                                                   @Param("categoryId") Long categoryId,
                                                                   @Param("builtIn") Boolean builtIn,
                                                                   @Param("enable") Boolean enable,
                                                                   @Param("params") String params);

    Integer checkPipelineName(@Param("sourceId") Long sourceId,
                              @Param("name") String name,
                              @Param("ciPipelineTemplateId") Long ciPipelineTemplateId);

}

