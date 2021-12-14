package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.infra.dto.CiTemplateStepDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线步骤模板(CiTemplateStep)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:21
 */
public interface CiTemplateStepMapper extends BaseMapper<CiTemplateStepDTO> {

    List<CiTemplateStepVO> listByJobIds(@Param("jobIds") Set<Long> jobIds);

    /**
     * 根据
     */
    CiTemplateStepVO queryCiTemplateStepById(@Param("ciTemplateStepId") Long ciTemplateStepId);
}

