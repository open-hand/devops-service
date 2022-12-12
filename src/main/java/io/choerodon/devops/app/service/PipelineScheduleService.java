package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.cd.PipelineScheduleVO;
import io.choerodon.devops.infra.dto.PipelineScheduleDTO;

/**
 * 流水线定时配置表(PipelineSchedule)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-29 16:05:20
 */
public interface PipelineScheduleService {

    PipelineScheduleDTO create(Long pipelineId, PipelineScheduleVO pipelineScheduleVO);

    PipelineScheduleDTO queryByToken(String scheduleToken);

    List<PipelineScheduleDTO> listByPipelineId(Long pipelineId);

    List<PipelineScheduleVO> listVOByPipelineId(Long pipelineId);

    void baseUpdate(PipelineScheduleDTO oldPipelineScheduleDTO);

    void deleteByPipelineId(Long pipelineId);

    void deleteById(Long id);
}

