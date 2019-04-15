package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.PipelineDTO;
import io.choerodon.devops.domain.application.entity.PipelineE;
import io.choerodon.devops.infra.dataobject.PipelineDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:48 2019/4/4
 * Description:
 */
@Component
public class PipelineConvertor implements ConvertorI<PipelineE, PipelineDO, PipelineDTO> {
    @Override
    public PipelineE doToEntity(PipelineDO pipelineDO) {
        PipelineE pipelineE = new PipelineE();
        BeanUtils.copyProperties(pipelineDO, pipelineE);
        return pipelineE;
    }

    @Override
    public PipelineDO entityToDo(PipelineE pipelineE) {
        PipelineDO pipelineDO = new PipelineDO();
        BeanUtils.copyProperties(pipelineE, pipelineDO);
        return pipelineDO;
    }

    @Override
    public PipelineDTO entityToDto(PipelineE pipelineE) {
        PipelineDTO pipelineDTO = new PipelineDTO();
        BeanUtils.copyProperties(pipelineE, pipelineDTO);
        return pipelineDTO;
    }
}
