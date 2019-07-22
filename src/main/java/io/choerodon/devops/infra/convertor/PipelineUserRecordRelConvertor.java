package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineUserRecordRelationshipVO;
import io.choerodon.devops.api.vo.iam.entity.PipelineUserRecordRelE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineUserRecordRelConvertor.java
import io.choerodon.devops.infra.dto.PipelineUserRecordRelDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineUserRecordRelDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineUserRecordRelConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineUserRecordRelConvertor.java
import io.choerodon.devops.infra.dataobject.PipelineUserRecordRelDO;
=======
import io.choerodon.devops.infra.dto.PipelineUserRecordRelationshipDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/PipelineUserRecordRelConvertor.java
>>>>>>> [IMP] refactor PipelineController
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:18 2019/4/16
 * Description:
 */
@Component
public class PipelineUserRecordRelConvertor implements ConvertorI<PipelineUserRecordRelE, PipelineUserRecordRelationshipDTO, PipelineUserRecordRelationshipVO> {
    @Override
    public PipelineUserRecordRelationshipDTO entityToDo(PipelineUserRecordRelE pipelineUserRelE) {
        PipelineUserRecordRelationshipDTO pipelineUserRelDO = new PipelineUserRecordRelationshipDTO();
        BeanUtils.copyProperties(pipelineUserRelE, pipelineUserRelDO);
        return pipelineUserRelDO;
    }

    @Override
    public PipelineUserRecordRelE doToEntity(PipelineUserRecordRelationshipDTO pipelineUserRelDO) {
        PipelineUserRecordRelE pipelineUserRelE = new PipelineUserRecordRelE();
        BeanUtils.copyProperties(pipelineUserRelDO, pipelineUserRelE);
        return pipelineUserRelE;
    }
}
