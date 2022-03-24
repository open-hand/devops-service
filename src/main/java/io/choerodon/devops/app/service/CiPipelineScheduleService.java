package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.CiPipelineScheduleVO;
import io.choerodon.devops.infra.dto.CiPipelineScheduleDTO;

/**
 * devops_ci_pipeline_schedule(CiPipelineSchedule)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:27
 */
public interface CiPipelineScheduleService {

    CiPipelineScheduleDTO create(CiPipelineScheduleVO ciPipelineScheduleVO);

    List<CiPipelineScheduleVO> listByAppServiceId(Long projectId, Long appServiceId);

    void enableSchedule(Long projectId, Long id);
}

