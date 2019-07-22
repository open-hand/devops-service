package io.choerodon.devops.infra.persistence.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.PipelineUserRecordRelE;
import io.choerodon.devops.domain.application.repository.PipelineUserRelRecordRepository;
import io.choerodon.devops.infra.dto.PipelineUserRecordRelationshipDTO;
import io.choerodon.devops.infra.mapper.PipelineUserRecordRelMapper;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:26 2019/4/9
 * Description:
 */
@Component
public class PipelineUserRelRecordRepositoryImpl implements PipelineUserRelRecordRepository {
    @Autowired
    private PipelineUserRecordRelMapper recordRelMapper;

    @Override
    public PipelineUserRecordRelE baseCreate(PipelineUserRecordRelE recordRelE) {
        PipelineUserRecordRelationshipDTO recordRelDO = ConvertHelper.convert(recordRelE, PipelineUserRecordRelationshipDTO.class);
        if (recordRelMapper.insert(recordRelDO) != 1) {
            throw new CommonException("error.insert.pipeline.user.record");
        }
        return ConvertHelper.convert(recordRelMapper.selectOne(recordRelDO), PipelineUserRecordRelE.class);
    }

    @Override
    public List<PipelineUserRecordRelE> baseListByOptions(Long pipelineRecordId, Long stageRecordId, Long taskRecordId) {
        PipelineUserRecordRelationshipDTO recordRelDO = new PipelineUserRecordRelationshipDTO(pipelineRecordId, stageRecordId, taskRecordId);
        return ConvertHelper.convertList(recordRelMapper.select(recordRelDO), PipelineUserRecordRelE.class);
    }

    @Override
    public void baseDelete(Long pipelineRecordId, Long stageRecordId, Long taskRecordId) {
        PipelineUserRecordRelationshipDTO recordRelDO = new PipelineUserRecordRelationshipDTO(pipelineRecordId, stageRecordId, taskRecordId);
        recordRelMapper.delete(recordRelDO);
    }
}
