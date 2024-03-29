package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

import io.choerodon.devops.app.service.DevopsCiPipelineFunctionService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCiPipelineFunctionDTO;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineFunctionMapper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsCiPipelineFunctionServiceImpl implements DevopsCiPipelineFunctionService {

    @Autowired
    private DevopsCiPipelineFunctionMapper devopsCiPipelineFunctionMapper;


    @Override
    @Transactional
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.ERROR_PIPELINE_IS_NULL);

        DevopsCiPipelineFunctionDTO devopsCiPipelineFunctionDTO = new DevopsCiPipelineFunctionDTO();
        devopsCiPipelineFunctionDTO.setDevopsPipelineId(pipelineId);
        devopsCiPipelineFunctionMapper.delete(devopsCiPipelineFunctionDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(DevopsCiPipelineFunctionDTO devopsCiPipelineFunctionDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiPipelineFunctionMapper, devopsCiPipelineFunctionDTO, "error.save.pipeline.function.failed");
    }

    @Override
    public List<DevopsCiPipelineFunctionDTO> listFunctionsByDevopsPipelineId(Long pipelineId) {
        DevopsCiPipelineFunctionDTO devopsCiPipelineFunctionDTO = new DevopsCiPipelineFunctionDTO();
        devopsCiPipelineFunctionDTO.setDevopsPipelineId(pipelineId);
        return devopsCiPipelineFunctionMapper.select(devopsCiPipelineFunctionDTO);
    }
}
