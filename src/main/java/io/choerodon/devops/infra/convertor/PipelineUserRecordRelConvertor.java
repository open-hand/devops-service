package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineUserRecordRelDTO;
import io.choerodon.devops.api.vo.iam.entity.PipelineUserRecordRelE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineUserRecordRelConvertor.java
import io.choerodon.devops.infra.dto.PipelineUserRecordRelDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineUserRecordRelDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineUserRecordRelConvertor.java
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:18 2019/4/16
 * Description:
 */
@Component
public class PipelineUserRecordRelConvertor implements ConvertorI<PipelineUserRecordRelE, PipelineUserRecordRelDO, PipelineUserRecordRelDTO> {
    @Override
    public PipelineUserRecordRelDO entityToDo(PipelineUserRecordRelE pipelineUserRelE) {
        PipelineUserRecordRelDO pipelineUserRelDO = new PipelineUserRecordRelDO();
        BeanUtils.copyProperties(pipelineUserRelE, pipelineUserRelDO);
        return pipelineUserRelDO;
    }

    @Override
    public PipelineUserRecordRelE doToEntity(PipelineUserRecordRelDO pipelineUserRelDO) {
        PipelineUserRecordRelE pipelineUserRelE = new PipelineUserRecordRelE();
        BeanUtils.copyProperties(pipelineUserRelDO, pipelineUserRelE);
        return pipelineUserRelE;
    }
}
