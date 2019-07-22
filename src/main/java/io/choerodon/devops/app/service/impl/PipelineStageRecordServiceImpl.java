package io.choerodon.devops.app.service.impl;

import java.util.List;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.PipelineStageRecordE;
import io.choerodon.devops.app.service.PipelineStageRecordService;
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
import io.choerodon.devops.infra.mapper.PipelineStageRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Sheep on 2019/7/15.
 */

@Service
public class PipelineStageRecordServiceImpl implements PipelineStageRecordService {

    @Autowired
    private PipelineStageRecordMapper pipelineStageRecordMapper;

    @Override
    public PipelineStageRecordDTO baseCreateOrUpdate(PipelineStageRecordDTO pipelineStageRecordDTO) {
        if (pipelineStageRecordDTO.getId() == null) {
            if (pipelineStageRecordMapper.insert(pipelineStageRecordDTO) != 1) {
                throw new CommonException("error.insert.pipeline.stage.record");
            }
        } else {
            pipelineStageRecordDTO.setObjectVersionNumber(pipelineStageRecordMapper.selectByPrimaryKey(pipelineStageRecordDTO).getObjectVersionNumber());
            if (pipelineStageRecordMapper.updateByPrimaryKeySelective(pipelineStageRecordDTO) != 1) {
                throw new CommonException("error.update.pipeline.stage.record");
            }
        }
        return pipelineStageRecordDTO;
    }

    @Override
    public List<PipelineStageRecordDTO> baseListByRecordAndStageId(Long pipelineRecordId, Long stageId) {
        PipelineStageRecordDTO pipelineStageRecordDTO = new PipelineStageRecordDTO();
        pipelineStageRecordDTO.setPipelineRecordId(pipelineRecordId);
        pipelineStageRecordDTO.setStageId(stageId);
        return pipelineStageRecordMapper.select(pipelineStageRecordDTO);
    }

    @Override
    public PipelineStageRecordDTO baseQueryById(Long recordId) {
        PipelineStageRecordDTO pipelineStageRecordDTO = new PipelineStageRecordDTO();
        pipelineStageRecordDTO.setId(recordId);
        return pipelineStageRecordMapper.selectByPrimaryKey(pipelineStageRecordDTO);
    }

    @Override
    public PipelineStageRecordDTO baseUpdate(PipelineStageRecordDTO pipelineStageRecordDTO) {
        if (pipelineStageRecordMapper.updateByPrimaryKey(pipelineStageRecordDTO) != 1) {
            throw new CommonException("error.update.pipeline.stage.record");
        }
        return pipelineStageRecordMapper.selectByPrimaryKey(pipelineStageRecordDTO);
    }

    public PipelineStageRecordDTO baseQueryByPendingCheckStatus(Long pipelineRecordId) {
        return pipelineStageRecordMapper.queryByPendingCheckStatus(pipelineRecordId);
    }
}
