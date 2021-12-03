package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.DevopsCiPipelineVariableService;
import io.choerodon.devops.infra.dto.DevopsCiPipelineVariableDTO;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineVariableMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线配置的CI变量(DevopsCiPipelineVariable)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-03 16:18:15
 */
@Service
public class DevopsCiPipelineVariableServiceImpl implements DevopsCiPipelineVariableService {
    @Autowired
    private DevopsCiPipelineVariableMapper devopsCiPipelineVariableMapper;


    @Override
    @Transactional
    public void baseCreate(DevopsCiPipelineVariableDTO devopsCiPipelineVariableDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiPipelineVariableMapper,
                devopsCiPipelineVariableDTO,
                "error.save.pipeline.variable.failed");
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, "error.pipeline.id.is.null");

        DevopsCiPipelineVariableDTO devopsCiPipelineVariableDTO = new DevopsCiPipelineVariableDTO();
        devopsCiPipelineVariableDTO.setDevopsPipelineId(pipelineId);
        devopsCiPipelineVariableMapper.delete(devopsCiPipelineVariableDTO);
    }

    @Override
    public List<DevopsCiPipelineVariableDTO> listByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, "error.pipeline.id.is.null");

        DevopsCiPipelineVariableDTO devopsCiPipelineVariableDTO = new DevopsCiPipelineVariableDTO();
        devopsCiPipelineVariableDTO.setDevopsPipelineId(pipelineId);
        return devopsCiPipelineVariableMapper.select(devopsCiPipelineVariableDTO);
    }
}

