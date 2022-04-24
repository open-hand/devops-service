package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.CiScheduleVariableService;
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
    @Autowired
    private CiScheduleVariableMapper ciScheduleVariableMapper;


    @Override
    @Transactional
    public void baseCreate(CiScheduleVariableDTO ciScheduleVariableDTO) {
        MapperUtil.resultJudgedInsertSelective(ciScheduleVariableMapper, ciScheduleVariableDTO, "error.save.variable.failed");
    }

    @Override
    public void deleteByPipelineScheduleId(Long id) {
        Assert.notNull(id, "error.ci.pipeline.id.is.null");
        CiScheduleVariableDTO ciScheduleVariableDTO = new CiScheduleVariableDTO();
        ciScheduleVariableDTO.setCiPipelineScheduleId(id);
        ciScheduleVariableMapper.delete(ciScheduleVariableDTO);
    }

    @Override
    public List<CiScheduleVariableDTO> listByScheduleId(Long id) {
        Assert.notNull(id, "error.ci.pipeline.id.is.null");
        CiScheduleVariableDTO ciScheduleVariableDTO = new CiScheduleVariableDTO();
        ciScheduleVariableDTO.setCiPipelineScheduleId(id);
        return ciScheduleVariableMapper.select(ciScheduleVariableDTO);
    }
}

