package io.choerodon.devops.infra.convertor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.PipelineAppDeployVO;
import io.choerodon.devops.api.vo.iam.entity.PipelineAppDeployE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/PipelineAppDeployConvertor.java
import io.choerodon.devops.infra.dto.PipelineAppDeployDO;
=======
import io.choerodon.devops.infra.dataobject.PipelineAppDeployDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineAppDeployConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/PipelineAppDeployConvertor.java
import io.choerodon.devops.infra.dataobject.PipelineAppDeployDO;
=======
import io.choerodon.devops.infra.dto.PipelineAppDeployDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/PipelineAppDeployConvertor.java
>>>>>>> [IMP] 重构部分Repository
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  14:31 2019/4/8
 * Description:
 */
@Component
public class PipelineAppDeployConvertor implements ConvertorI<PipelineAppDeployE, PipelineAppDeployDTO, PipelineAppDeployVO> {

    @Override
    public PipelineAppDeployE dtoToEntity(PipelineAppDeployVO pipelineAppDeployDTO) {
        PipelineAppDeployE pipelineAppDeployE = new PipelineAppDeployE();
        BeanUtils.copyProperties(pipelineAppDeployDTO, pipelineAppDeployE);
        if (pipelineAppDeployDTO.getTriggerVersion() != null) {
            pipelineAppDeployE.setTriggerVersion(pipelineAppDeployDTO.getTriggerVersion().stream().collect(Collectors.joining(",")));
        }
        return pipelineAppDeployE;
    }

    @Override
    public PipelineAppDeployVO entityToDto(PipelineAppDeployE pipelineAppDeployE) {
        PipelineAppDeployVO pipelineAppDeployDTO = new PipelineAppDeployVO();
        BeanUtils.copyProperties(pipelineAppDeployE, pipelineAppDeployDTO);
        if (pipelineAppDeployE.getTriggerVersion() != null && !pipelineAppDeployE.getTriggerVersion().isEmpty()) {
            pipelineAppDeployDTO.setTriggerVersion(Arrays.asList(pipelineAppDeployE.getTriggerVersion().split(",")));
        } else {
            pipelineAppDeployDTO.setTriggerVersion(new ArrayList<>());
        }
        return pipelineAppDeployDTO;
    }


    @Override
    public PipelineAppDeployDTO entityToDo(PipelineAppDeployE pipelineAppDeployE) {
        PipelineAppDeployDTO pipelineAppDeployDO = new PipelineAppDeployDTO();
        BeanUtils.copyProperties(pipelineAppDeployE, pipelineAppDeployDO);
        return pipelineAppDeployDO;
    }

    @Override  dtoToVo
    public PipelineAppDeployE doToEntity(PipelineAppDeployDTO pipelineAppDeployDO) {
        PipelineAppDeployE pipelineAppDeployE = new PipelineAppDeployE();
        BeanUtils.copyProperties(pipelineAppDeployDO, pipelineAppDeployE);
        return pipelineAppDeployE;
    }
}
