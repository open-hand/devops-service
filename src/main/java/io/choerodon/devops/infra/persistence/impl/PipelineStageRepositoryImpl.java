package io.choerodon.devops.infra.persistence.impl;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.iam.entity.PipelineStageE;
import io.choerodon.devops.domain.application.repository.PipelineStageRepository;
import io.choerodon.devops.infra.dataobject.PipelineStageDO;
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

        PipelineStageDO pipelineStageDO = ConvertHelper.convert(pipelineStageE, PipelineStageDO.class);
        if (stageMapper.insert(pipelineStageDO) != 1) {
            throw new CommonException("error.insert.pipeline.stage");
        }
        return ConvertHelper.convert(pipelineStageDO, PipelineStageE.class);
    }

    @Override
    public PipelineStageE update(PipelineStageE pipelineStageE) {
        PipelineStageDO pipelineStageDO = ConvertHelper.convert(pipelineStageE, PipelineStageDO.class);
        if (stageMapper.updateByPrimaryKey(pipelineStageDO) != 1) {
            throw new CommonException("error.update.pipeline.stage");
        }
        return ConvertHelper.convert(pipelineStageDO, PipelineStageE.class);
    }

    @Override
    public List<PipelineStageE> queryByPipelineId(Long pipelineId) {
        PipelineStageDO pipelineStageDO = new PipelineStageDO();
        pipelineStageDO.setPipelineId(pipelineId);
        return ConvertHelper.convertList(stageMapper.select(pipelineStageDO), PipelineStageE.class);
    }

    @Override
    public void delete(Long stageId) {
        PipelineStageDO pipelineStageDO = new PipelineStageDO();
        pipelineStageDO.setId(stageId);
        stageMapper.deleteByPrimaryKey(pipelineStageDO);
    }

    @Override
    public PipelineStageE queryById(Long stageId) {
        PipelineStageDO pipelineStageDO = new PipelineStageDO();
        pipelineStageDO.setId(stageId);
        return ConvertHelper.convert(stageMapper.selectByPrimaryKey(pipelineStageDO), PipelineStageE.class);
    }
}
