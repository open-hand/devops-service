package io.choerodon.devops.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.cd.PipelineScheduleVO;
import io.choerodon.devops.app.service.PipelineScheduleService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.PipelineScheduleDTO;
import io.choerodon.devops.infra.mapper.PipelineScheduleMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GenerateUUID;
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
    private static final String DEVOPS_UPDATE_SCHEDULE_FAILED = "devops.update.schedule.failed";

    @Autowired
    private PipelineScheduleMapper pipelineScheduleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PipelineScheduleDTO create(Long pipelineId, PipelineScheduleVO pipelineScheduleVO) {
        ScheduleUtil.validate(pipelineScheduleVO);

        PipelineScheduleDTO pipelineScheduleDTO = ConvertUtils.convertObject(pipelineScheduleVO, PipelineScheduleDTO.class);
        pipelineScheduleDTO.setId(null);
        pipelineScheduleDTO.setPipelineId(pipelineId);
        pipelineScheduleDTO.setToken(GenerateUUID.generateUUID());

        // 保存记录
        MapperUtil.resultJudgedInsertSelective(pipelineScheduleMapper, pipelineScheduleDTO, DEVOPS_SAVE_SCHEDULE_FAILED);
        return pipelineScheduleMapper.selectByPrimaryKey(pipelineScheduleDTO.getId());

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

    @Override
    public List<PipelineScheduleDTO> listByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        PipelineScheduleDTO pipelineScheduleDTO = new PipelineScheduleDTO();
        pipelineScheduleDTO.setPipelineId(pipelineId);
        return pipelineScheduleMapper.select(pipelineScheduleDTO);
    }

    @Override
    public List<PipelineScheduleVO> listVOByPipelineId(Long pipelineId) {
        List<PipelineScheduleDTO> pipelineScheduleDTOS = listByPipelineId(pipelineId);
        if (CollectionUtils.isEmpty(pipelineScheduleDTOS)) {
            return new ArrayList<>();
        }
        return ConvertUtils.convertList(pipelineScheduleDTOS, PipelineScheduleVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseUpdate(PipelineScheduleDTO pipelineScheduleDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(pipelineScheduleMapper, pipelineScheduleDTO, DEVOPS_UPDATE_SCHEDULE_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);
        PipelineScheduleDTO pipelineScheduleDTO = new PipelineScheduleDTO();
        pipelineScheduleDTO.setPipelineId(pipelineId);

        pipelineScheduleMapper.delete(pipelineScheduleDTO);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        pipelineScheduleMapper.deleteByPrimaryKey(id);
    }
}

