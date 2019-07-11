package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineReqDTO;
import io.choerodon.devops.domain.application.entity.PipelineE;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:42 2019/4/8
 * Description:
 */
@Component
public class PipelineReqConvertor implements ConvertorI<PipelineE, Object, PipelineReqDTO> {
    @Override
    public PipelineE dtoToEntity(PipelineReqDTO pipelineReqDTO) {
        PipelineE pipelineE = new PipelineE();
        BeanUtils.copyProperties(pipelineReqDTO, pipelineE);
        return pipelineE;
    }

    @Override
    public PipelineReqDTO entityToDto(PipelineE pipelineE) {
        PipelineReqDTO pipelineReqDTO = new PipelineReqDTO();
        BeanUtils.copyProperties(pipelineE, pipelineReqDTO);
        return pipelineReqDTO;
    }
}
