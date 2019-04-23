package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.PipelineTaskRecordDTO;
import io.choerodon.devops.domain.application.entity.PipelineTaskRecordE;
import io.choerodon.devops.infra.dataobject.PipelineTaskRecordDO;
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