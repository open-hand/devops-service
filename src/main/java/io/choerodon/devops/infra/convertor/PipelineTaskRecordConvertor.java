package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineTaskRecordDTO;
import io.choerodon.devops.api.vo.iam.entity.PipelineTaskRecordE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineTaskRecordConvertor.java
import io.choerodon.devops.infra.dto.PipelineTaskRecordDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineTaskRecordDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineTaskRecordConvertor.java
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  12:05 2019/4/14
 * Description:
 */
@Component
public class PipelineTaskRecordConvertor implements ConvertorI<PipelineTaskRecordE, PipelineTaskRecordDO, PipelineTaskRecordDTO> {
    @Override
    public PipelineTaskRecordDTO entityToDto(PipelineTaskRecordE taskRecordE) {
        PipelineTaskRecordDTO taskRecordDTO = new PipelineTaskRecordDTO();
        BeanUtils.copyProperties(taskRecordE, taskRecordDTO);
        return taskRecordDTO;
    }

    @Override
    public PipelineTaskRecordE doToEntity(PipelineTaskRecordDO taskRecordDO) {
        PipelineTaskRecordE taskRecordE = new PipelineTaskRecordE();
        BeanUtils.copyProperties(taskRecordDO, taskRecordE);
        return taskRecordE;
    }

    @Override
    public PipelineTaskRecordDO entityToDo(PipelineTaskRecordE taskRecordE) {
        PipelineTaskRecordDO taskRecordDO = new PipelineTaskRecordDO();
        BeanUtils.copyProperties(taskRecordE, taskRecordDO);
        return taskRecordDO;
    }
}