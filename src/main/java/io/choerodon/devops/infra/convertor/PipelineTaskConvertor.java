package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineTaskVO;
import io.choerodon.devops.api.vo.iam.entity.PipelineTaskE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineTaskConvertor.java
import io.choerodon.devops.infra.dto.PipelineTaskDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineTaskDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineTaskConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineTaskConvertor.java
import io.choerodon.devops.infra.dataobject.PipelineTaskDO;
=======
import io.choerodon.devops.infra.dto.PipelineTaskDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/PipelineTaskConvertor.java
>>>>>>> [REF] refactor PipelineTaskRecordRepository and PipelineTaskRepository
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:28 2019/4/8
 * Description:
 */
@Component
public class PipelineTaskConvertor implements ConvertorI<PipelineTaskE, PipelineTaskDTO, PipelineTaskVO> {
    @Override
    public PipelineTaskE dtoToEntity(PipelineTaskVO pipelineTaskVO) {
        PipelineTaskE pipelineTaskE = new PipelineTaskE();
        BeanUtils.copyProperties(pipelineTaskVO, pipelineTaskE);
        return pipelineTaskE;
    }

    @Override
    public PipelineTaskVO entityToDto(PipelineTaskE pipelineTaskE) {
        PipelineTaskVO pipelineTaskVO = new PipelineTaskVO();
        BeanUtils.copyProperties(pipelineTaskE, pipelineTaskVO);
        return pipelineTaskVO;
    }

    @Override
    public PipelineTaskDTO entityToDo(PipelineTaskE pipelineTaskE) {
        PipelineTaskDTO pipelineTaskDTO = new PipelineTaskDTO();
        BeanUtils.copyProperties(pipelineTaskE, pipelineTaskDTO);
        return pipelineTaskDTO;
    }

    @Override
    public PipelineTaskE doToEntity(PipelineTaskDTO pipelineTaskDTO) {
        PipelineTaskE pipelineTaskE = new PipelineTaskE();
        BeanUtils.copyProperties(pipelineTaskDTO, pipelineTaskE);
        return pipelineTaskE;
    }
}
