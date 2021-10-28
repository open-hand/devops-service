package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCdStageService;
import io.choerodon.devops.infra.constant.PipelineCheckConstant;
import io.choerodon.devops.infra.dto.DevopsCdStageDTO;
import io.choerodon.devops.infra.mapper.DevopsCdStageMapper;

@Service
public class DevopsCdStageServiceImpl implements DevopsCdStageService {
    private static final String CREATE_STAGE_FAILED = "create.stage.failed";


    @Autowired
    private DevopsCdStageMapper devopsCdStageMapper;

    @Override
    public List<DevopsCdStageDTO> queryByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.ERROR_PIPELINE_IS_NULL);
        DevopsCdStageDTO devopsCdStageDTO = new DevopsCdStageDTO();
        devopsCdStageDTO.setPipelineId(pipelineId);
        return devopsCdStageMapper.select(devopsCdStageDTO);
    }

    @Override
    @Transactional
    public DevopsCdStageDTO create(DevopsCdStageDTO devopsCdStageDTO) {
        if (devopsCdStageMapper.insert(devopsCdStageDTO) != 1) {
            throw new CommonException(CREATE_STAGE_FAILED);
        }
        return devopsCdStageMapper.selectByPrimaryKey(devopsCdStageDTO.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long stageId) {
        devopsCdStageMapper.deleteByPrimaryKey(stageId);
    }

    @Override
    @Transactional
    public void deleteByPipelineId(Long pipelineId) {
        Assert.notNull(pipelineId, PipelineCheckConstant.ERROR_PIPELINE_IS_NULL);
        DevopsCdStageDTO cdStageDTO = new DevopsCdStageDTO();
        cdStageDTO.setProjectId(pipelineId);
        List<DevopsCdStageDTO> devopsCdStageDTOS = devopsCdStageMapper.select(cdStageDTO);
        if (CollectionUtils.isEmpty(devopsCdStageDTOS)) {
            devopsCdStageDTOS.forEach(devopsCdStageDTO -> deleteById(devopsCdStageDTO.getId()));
        }
    }

    @Override
    public DevopsCdStageDTO queryById(Long stageId) {
        Assert.notNull(stageId, PipelineCheckConstant.ERROR_STAGE_ID_IS_NULL);
        return devopsCdStageMapper.selectByPrimaryKey(stageId);
    }
}
