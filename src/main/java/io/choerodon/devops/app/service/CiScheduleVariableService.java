package io.choerodon.devops.app.service;

import io.choerodon.devops.infra.dto.CiScheduleVariableDTO;

/**
 * devops_ci_schedule_variable(CiScheduleVariable)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:52
 */
public interface CiScheduleVariableService {

    void baseCreate(CiScheduleVariableDTO ciScheduleVariableDTO);

    void deleteByPipelineScheduleId(Long id);
}

