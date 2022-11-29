package io.choerodon.devops.app.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.cd.PipelineScheduleVO;
import io.choerodon.devops.app.service.PipelineScheduleService;
import io.choerodon.devops.infra.dto.PipelineScheduleDTO;
import io.choerodon.devops.infra.mapper.PipelineScheduleMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;
import io.choerodon.devops.infra.util.ScheduleUtil;

/**
 * 流水线定时配置表(PipelineSchedule)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-11-29 16:05:20
 */
@Service
public class PipelineScheduleServiceImpl implements PipelineScheduleService {

    private static final String DEVOPS_SAVE_SCHEDULE_FAILED = "devops.save.schedule.failed";

    @Autowired
    private PipelineScheduleMapper pipelineScheduleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineScheduleDTO create(Long pipelineId, PipelineScheduleVO pipelineScheduleVO) {
        ScheduleUtil.validate(pipelineScheduleVO);

        PipelineScheduleDTO pipelineScheduleDTO = ConvertUtils.convertObject(pipelineScheduleVO, PipelineScheduleDTO.class);
        pipelineScheduleDTO.setId(null);
        pipelineScheduleDTO.setPipelineId(pipelineId);

        // 保存记录
        return MapperUtil.resultJudgedInsertSelective(pipelineScheduleMapper, pipelineScheduleDTO, DEVOPS_SAVE_SCHEDULE_FAILED);

    }

    @Override
    public PipelineScheduleDTO queryByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        PipelineScheduleDTO pipelineScheduleDTO = new PipelineScheduleDTO();
        pipelineScheduleDTO.setToken(token);
        return pipelineScheduleMapper.selectOne(pipelineScheduleDTO);
    }
}

