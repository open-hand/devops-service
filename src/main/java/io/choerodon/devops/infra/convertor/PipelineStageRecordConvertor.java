package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineStageRecordVO;
import io.choerodon.devops.api.vo.iam.entity.PipelineStageRecordE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineStageRecordConvertor.java
import io.choerodon.devops.infra.dto.PipelineStageRecordDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineStageRecordDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineStageRecordConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineStageRecordConvertor.java
import io.choerodon.devops.infra.dataobject.PipelineStageRecordDO;
=======
import io.choerodon.devops.infra.dto.PipelineStageRecordDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/PipelineStageRecordConvertor.java
>>>>>>> [IMP]修改后端结构
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  11:49 2019/4/14
 * Description:
 */
@Component
public class PipelineStageRecordConvertor implements ConvertorI<PipelineStageRecordE, PipelineStageRecordDTO, PipelineStageRecordVO> {
    @Override
    public PipelineStageRecordE dtoToEntity(PipelineStageRecordVO stageRecordDTO) {
        PipelineStageRecordE stageRecordE = new PipelineStageRecordE();
        BeanUtils.copyProperties(stageRecordDTO, stageRecordE);
        return stageRecordE;
    }

    @Override
    public PipelineStageRecordE doToEntity(PipelineStageRecordDTO stageRecordDO) {
        PipelineStageRecordE stageRecordE = new PipelineStageRecordE();
        BeanUtils.copyProperties(stageRecordDO, stageRecordE);
        return stageRecordE;
    }

    @Override
    public PipelineStageRecordVO entityToDto(PipelineStageRecordE stageRecordE) {
        PipelineStageRecordVO stageRecordDTO = new PipelineStageRecordVO();
        BeanUtils.copyProperties(stageRecordE, stageRecordDTO);
        return stageRecordDTO;
    }

    @Override
    public PipelineStageRecordDTO entityToDo(PipelineStageRecordE stageRecordE) {
        PipelineStageRecordDTO stageRecordDO = new PipelineStageRecordDTO();
        BeanUtils.copyProperties(stageRecordE, stageRecordDO);
        return stageRecordDO;
    }
}