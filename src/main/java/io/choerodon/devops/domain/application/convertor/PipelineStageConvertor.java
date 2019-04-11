package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.PipelineStageDTO;
import io.choerodon.devops.domain.application.entity.PipelineStageE;
import io.choerodon.devops.infra.dataobject.PipelineStageDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:28 2019/4/8
 * Description:
 */
@Component
public class PipelineStageConvertor implements ConvertorI<PipelineStageE, PipelineStageDO, PipelineStageDTO> {
    @Override
    public PipelineStageE dtoToEntity(PipelineStageDTO pipelineStageDTO) {
        PipelineStageE pipelineStageE = new PipelineStageE();
        BeanUtils.copyProperties(pipelineStageDTO, pipelineStageE);
        return pipelineStageE;
    }

    @Override
    public PipelineStageDO entityToDo(PipelineStageE pipelineStageE) {
        PipelineStageDO pipelineStageDO = new PipelineStageDO();
        BeanUtils.copyProperties(pipelineStageE, pipelineStageDO);
        return pipelineStageDO;
    }

    @Override
    public PipelineStageE doToEntity(PipelineStageDO pipelineStageDO) {
        PipelineStageE pipelineStageE = new PipelineStageE();
        BeanUtils.copyProperties(pipelineStageDO, pipelineStageE);
        return pipelineStageE;
    }
}
