package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineUserRelationshipVO;
import io.choerodon.devops.api.vo.iam.entity.PipelineUserRelE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineUserRelConvertor.java
import io.choerodon.devops.infra.dto.PipelineUserRelDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineUserRelDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineUserRelConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineUserRelConvertor.java
import io.choerodon.devops.infra.dataobject.PipelineUserRelDO;
=======
import io.choerodon.devops.infra.dto.PipelineUserRelationshipDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/PipelineUserRelConvertor.java
>>>>>>> [IMP] refactor PipelineController
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  17:11 2019/4/8
 * Description:
 */
@Component
public class PipelineUserRelConvertor implements ConvertorI<PipelineUserRelE, PipelineUserRelationshipDTO, PipelineUserRelationshipVO> {
    @Override
    public PipelineUserRelE dtoToEntity(PipelineUserRelationshipVO userRelDTO) {
        PipelineUserRelE pipelineUserRelE = new PipelineUserRelE();
        BeanUtils.copyProperties(userRelDTO, pipelineUserRelE);
        return pipelineUserRelE;
    }

    @Override
    public PipelineUserRelationshipDTO entityToDo(PipelineUserRelE pipelineUserRelE) {
        PipelineUserRelationshipDTO pipelineUserRelDO = new PipelineUserRelationshipDTO();
        BeanUtils.copyProperties(pipelineUserRelE, pipelineUserRelDO);
        return pipelineUserRelDO;
    }

    @Override
    public PipelineUserRelE doToEntity(PipelineUserRelationshipDTO pipelineUserRelDO) {
        PipelineUserRelE pipelineUserRelE = new PipelineUserRelE();
        BeanUtils.copyProperties(pipelineUserRelDO, pipelineUserRelE);
        return pipelineUserRelE;
    }
}
