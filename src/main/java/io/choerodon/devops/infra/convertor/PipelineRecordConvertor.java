package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineRecordVO;
import io.choerodon.devops.api.vo.iam.entity.PipelineRecordE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineRecordConvertor.java
import io.choerodon.devops.infra.dto.PipelineRecordDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineRecordDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineRecordConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineRecordConvertor.java
import io.choerodon.devops.infra.dataobject.PipelineRecordDO;
=======
import io.choerodon.devops.infra.dto.PipelineRecordDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/PipelineRecordConvertor.java
>>>>>>> [IMP] 重构部分Repository
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:45 2019/4/16
 * Description:
 */
@Component
public class PipelineRecordConvertor implements ConvertorI<PipelineRecordE, PipelineRecordDTO, PipelineRecordVO> {
    @Override
    public PipelineRecordE doToEntity(PipelineRecordDTO pipelineRecordDO) {
        PipelineRecordE pipelineRecordE = new PipelineRecordE();
        BeanUtils.copyProperties(pipelineRecordDO, pipelineRecordE);
        return pipelineRecordE;
    }

    @Override
    public PipelineRecordDTO entityToDo(PipelineRecordE pipelineRecordE) {
        PipelineRecordDTO pipelineRecordDO = new PipelineRecordDTO();
        BeanUtils.copyProperties(pipelineRecordE, pipelineRecordDO);
        return pipelineRecordDO;
    }

    @Override
    public PipelineRecordVO entityToDto(PipelineRecordE pipelineRecordE) {
        PipelineRecordVO pipelineRecordDTO = new PipelineRecordVO();
        BeanUtils.copyProperties(pipelineRecordE, pipelineRecordDTO);
        return pipelineRecordDTO;
    }
}
