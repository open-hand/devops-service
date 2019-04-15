package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.PipelineAppDeployDTO;
import io.choerodon.devops.api.dto.PipelineTaskDTO;
import io.choerodon.devops.domain.application.entity.PipelineTaskE;
import io.choerodon.devops.infra.dataobject.PipelineTaskDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:28 2019/4/8
 * Description:
 */
@Component
public class PipelineTaskConvertor implements ConvertorI<PipelineTaskE, PipelineTaskDO, PipelineTaskDTO> {
    @Override
    public PipelineTaskE dtoToEntity(PipelineTaskDTO pipelineTaskDTO) {
        PipelineTaskE pipelineTaskE = new PipelineTaskE();
        BeanUtils.copyProperties(pipelineTaskDTO, pipelineTaskE);
        return pipelineTaskE;
    }

    @Override
    public PipelineTaskDO entityToDo(PipelineTaskE pipelineTaskE) {
        PipelineTaskDO pipelineTaskDO = new PipelineTaskDO();
        BeanUtils.copyProperties(pipelineTaskE, pipelineTaskDO);
        return pipelineTaskDO;
    }

    @Override
    public PipelineTaskE doToEntity(PipelineTaskDO pipelineTaskDO) {
        PipelineTaskE pipelineTaskE = new PipelineTaskE();
        BeanUtils.copyProperties(pipelineTaskDO, pipelineTaskE);
        return pipelineTaskE;
    }
}
