package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.PipelineStageE;
import io.choerodon.devops.app.service.PipelineStageService;
import io.choerodon.devops.infra.dto.PipelineStageDTO;
import io.choerodon.devops.infra.mapper.PipelineStageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/15.
 */

@Service
public class PipelineStageServiceImpl implements PipelineStageService {


    @Autowired
    private PipelineStageMapper pipelineStageMapper;



    public PipelineStageDTO baseCreate(PipelineStageDTO pipelineStageDTO) {

        if (pipelineStageMapper.insert(pipelineStageDTO) != 1) {
            throw new CommonException("error.insert.pipeline.stage");
        }
        return pipelineStageDTO;
    }

    public PipelineStageDTO baseUpdate(PipelineStageDTO pipelineStageDTO) {
        if (pipelineStageMapper.updateByPrimaryKey(pipelineStageDTO) != 1) {
            throw new CommonException("error.update.pipeline.stage");
        }
        return pipelineStageDTO;
    }

    public List<PipelineStageDTO> baseListByPipelineId(Long pipelineId) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setPipelineId(pipelineId);
        return pipelineStageMapper.select(pipelineStageDTO);
    }

    public void baseDelete(Long stageId) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setId(stageId);
        pipelineStageMapper.deleteByPrimaryKey(pipelineStageDTO);
    }

    public PipelineStageE baseQueryById(Long stageId) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setId(stageId);
        return ConvertHelper.convert(pipelineStageMapper.selectByPrimaryKey(pipelineStageDTO), PipelineStageE.class);
    }



}
