package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.api.vo.template.CiTemplateStepVO;
import io.choerodon.devops.infra.dto.CiTemplateJobStepRelDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * 流水线任务模板与步骤模板关系表(CiTemplateJobStepRel)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 15:58:17
 */
public interface CiTemplateJobStepRelMapper extends BaseMapper<CiTemplateJobStepRelDTO> {

    List<CiTemplateStepVO> listByJobIds(@Param("jobIds") Set<Long> jobIds);
}

