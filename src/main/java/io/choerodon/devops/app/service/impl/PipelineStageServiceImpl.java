package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.PipelineStageService;
import io.choerodon.devops.infra.dto.PipelineStageDTO;
import io.choerodon.devops.infra.mapper.PipelineStageMapper;

/**
 * Created by Sheep on 2019/7/15.
 */

@Service
public class PipelineStageServiceImpl implements PipelineStageService {


    @Autowired
    private PipelineStageMapper pipelineStageMapper;


    @Override
    public PipelineStageDTO baseCreate(PipelineStageDTO pipelineStageDTO) {

        if (pipelineStageMapper.insert(pipelineStageDTO) != 1) {
            throw new CommonException("error.insert.pipeline.stage");
        }
        return pipelineStageDTO;
    }

    @Override
    public PipelineStageDTO baseUpdate(PipelineStageDTO pipelineStageDTO) {
        if (pipelineStageMapper.updateByPrimaryKey(pipelineStageDTO) != 1) {
            throw new CommonException("error.update.pipeline.stage");
        }
        return pipelineStageDTO;
    }

    @Override
    public List<PipelineStageDTO> baseListByPipelineId(Long pipelineId) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setPipelineId(pipelineId);
        return pipelineStageMapper.select(pipelineStageDTO);
    }

    @Override
    public void baseDelete(Long stageId) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setId(stageId);
        pipelineStageMapper.deleteByPrimaryKey(pipelineStageDTO);
    }

    @Override
    public PipelineStageDTO baseQueryById(Long stageId) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setId(stageId);
        return pipelineStageMapper.selectByPrimaryKey(pipelineStageDTO);
    }


}
