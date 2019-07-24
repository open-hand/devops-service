package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.PipelineUserRecordRelationshipService;
import io.choerodon.devops.infra.dto.PipelineUserRecordRelationshipDTO;
import io.choerodon.devops.infra.mapper.PipelineUserRecordRelMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:03 2019/7/19
 * Description:
 */
@Service
public class PipelineUserRecordRelationshipServiceImpl implements PipelineUserRecordRelationshipService {
    @Autowired
    private PipelineUserRecordRelMapper recordRelMapper;

    @Override
    public PipelineUserRecordRelationshipDTO baseCreate(PipelineUserRecordRelationshipDTO userRecordRelationshipDTO) {
        if (recordRelMapper.insert(userRecordRelationshipDTO) != 1) {
            throw new CommonException("error.insert.pipeline.user.record");
        }
        return userRecordRelationshipDTO;
    }

    @Override
    public List<PipelineUserRecordRelationshipDTO> baseListByOptions(Long pipelineRecordId, Long stageRecordId, Long taskRecordId) {
        PipelineUserRecordRelationshipDTO pipelineUserRecordRelationshipDTO = new PipelineUserRecordRelationshipDTO(pipelineRecordId, stageRecordId, taskRecordId);
        return recordRelMapper.select(pipelineUserRecordRelationshipDTO);
    }

    @Override
    public void baseDelete(Long pipelineRecordId, Long stageRecordId, Long taskRecordId) {
        PipelineUserRecordRelationshipDTO userRecordRelationshipDTO = new PipelineUserRecordRelationshipDTO(pipelineRecordId, stageRecordId, taskRecordId);
        recordRelMapper.delete(userRecordRelationshipDTO);
    }
}
