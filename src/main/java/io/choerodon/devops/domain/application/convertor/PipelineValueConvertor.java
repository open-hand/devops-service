package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.PipelineValueDTO;
import io.choerodon.devops.domain.application.entity.PipelineValueE;
import io.choerodon.devops.infra.dataobject.PipelineValueDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:45 2019/4/10
 * Description:
 */
@Component
public class PipelineValueConvertor implements ConvertorI<PipelineValueE, PipelineValueDO, PipelineValueDTO> {
    @Override
    public PipelineValueE doToEntity(PipelineValueDO pipelineValueDO) {
        PipelineValueE pipelineValueE = new PipelineValueE();
        BeanUtils.copyProperties(pipelineValueDO, pipelineValueE);
        return pipelineValueE;
    }

    @Override
    public PipelineValueDO entityToDo(PipelineValueE pipelineValueE) {
        PipelineValueDO pipelineValueDO = new PipelineValueDO();
        BeanUtils.copyProperties(pipelineValueE, pipelineValueDO);
        return pipelineValueDO;
    }


    @Override
    public PipelineValueE dtoToEntity(PipelineValueDTO pipelineValueDTO) {
        PipelineValueE pipelineValueE = new PipelineValueE();
        BeanUtils.copyProperties(pipelineValueDTO, pipelineValueE);
        return pipelineValueE;
    }

    @Override
    public PipelineValueDTO entityToDto(PipelineValueE pipelineValueE) {
        PipelineValueDTO pipelineValueDTO = new PipelineValueDTO();
        BeanUtils.copyProperties(pipelineValueE, pipelineValueDTO);
        return pipelineValueDTO;
    }
}
