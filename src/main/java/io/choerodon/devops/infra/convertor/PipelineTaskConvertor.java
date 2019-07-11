package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineTaskDTO;
import io.choerodon.devops.api.vo.iam.entity.PipelineTaskE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineTaskConvertor.java
import io.choerodon.devops.infra.dto.PipelineTaskDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineTaskDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineTaskConvertor.java
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

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
    public PipelineTaskDTO entityToDto(PipelineTaskE pipelineTaskE) {
        PipelineTaskDTO pipelineTaskDTO = new PipelineTaskDTO();
        BeanUtils.copyProperties(pipelineTaskE, pipelineTaskDTO);
        return pipelineTaskDTO;
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
