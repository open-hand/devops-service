package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.repository.PipelineStageRepository;
import io.choerodon.devops.infra.dto.PipelineStageDTO;
import io.choerodon.devops.infra.mapper.PipelineStageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:40 2019/4/8
 * Description:
 */
@Component
public class PipelineStageRepositoryImpl implements PipelineStageRepository {
    @Autowired
    private PipelineStageMapper stageMapper;




    @Override
    public PipelineStageE create(PipelineStageE pipelineStageE) {

        PipelineStageDTO pipelineStageDTO = ConvertHelper.convert(pipelineStageE, PipelineStageDTO.class);
        if (stageMapper.insert(pipelineStageDTO) != 1) {
            throw new CommonException("error.insert.pipeline.stage");
        }
        return ConvertHelper.convert(pipelineStageDTO, PipelineStageE.class);
    }

    @Override
    public PipelineStageE update(PipelineStageE pipelineStageE) {
        PipelineStageDTO pipelineStageDTO = ConvertHelper.convert(pipelineStageE, PipelineStageDTO.class);
        if (stageMapper.updateByPrimaryKey(pipelineStageDTO) != 1) {
            throw new CommonException("error.update.pipeline.stage");
        }
        return ConvertHelper.convert(pipelineStageDTO, PipelineStageE.class);
    }

    @Override
    public List<PipelineStageE> baseListByPipelineId(Long pipelineId) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setPipelineId(pipelineId);
        return ConvertHelper.convertList(stageMapper.select(pipelineStageDTO), PipelineStageE.class);
    }

    @Override
    public void delete(Long stageId) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setId(stageId);
        stageMapper.deleteByPrimaryKey(pipelineStageDTO);
    }

    @Override
    public PipelineStageE queryById(Long stageId) {
        PipelineStageDTO pipelineStageDTO = new PipelineStageDTO();
        pipelineStageDTO.setId(stageId);
        return ConvertHelper.convert(stageMapper.selectByPrimaryKey(pipelineStageDTO), PipelineStageE.class);
    }
}
