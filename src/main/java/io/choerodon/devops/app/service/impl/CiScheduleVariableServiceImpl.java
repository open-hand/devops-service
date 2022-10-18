package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiScheduleVariableService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.CiScheduleVariableDTO;
import io.choerodon.devops.infra.mapper.CiScheduleVariableMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * devops_ci_schedule_variable(CiScheduleVariable)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:52
 */
@Service
public class CiScheduleVariableServiceImpl implements CiScheduleVariableService {

    private static final String DEVOPS_SAVE_VARIABLE_FAILED = "devops.save.variable.failed";

    @Autowired
    private CiScheduleVariableMapper ciScheduleVariableMapper;


    @Override
    @Transactional
    public void baseCreate(CiScheduleVariableDTO ciScheduleVariableDTO) {
        MapperUtil.resultJudgedInsertSelective(ciScheduleVariableMapper, ciScheduleVariableDTO, DEVOPS_SAVE_VARIABLE_FAILED);
    }

    @Override
    public void deleteByPipelineScheduleId(Long id) {
        Assert.notNull(id, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        CiScheduleVariableDTO ciScheduleVariableDTO = new CiScheduleVariableDTO();
        ciScheduleVariableDTO.setCiPipelineScheduleId(id);
        ciScheduleVariableMapper.delete(ciScheduleVariableDTO);
    }

    @Override
    public List<CiScheduleVariableDTO> listByScheduleId(Long id) {
        Assert.notNull(id, PipelineCheckConstant.DEVOPS_PIPELINE_ID_IS_NULL);

        CiScheduleVariableDTO ciScheduleVariableDTO = new CiScheduleVariableDTO();
        ciScheduleVariableDTO.setCiPipelineScheduleId(id);
        return ciScheduleVariableMapper.select(ciScheduleVariableDTO);
    }
}

