package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsDeployValueVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsDeployValueE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsDeployValueConvertor.java
import io.choerodon.devops.infra.dto.DevopsDeployValueDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsDeployValueDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsDeployValueConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsDeployValueConvertor.java
import io.choerodon.devops.infra.dataobject.DevopsDeployValueDO;
=======
import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/DevopsDeployValueConvertor.java
>>>>>>> [IMP] 修改repository重构
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:45 2019/4/10
 * Description:
 */
@Component
public class DevopsDeployValueConvertor implements ConvertorI<DevopsDeployValueE, DevopsDeployValueDTO, DevopsDeployValueVO> {
    @Override
    public DevopsDeployValueE doToEntity(DevopsDeployValueDTO pipelineValueDO) {
        DevopsDeployValueE pipelineValueE = new DevopsDeployValueE();
        BeanUtils.copyProperties(pipelineValueDO, pipelineValueE);
        return pipelineValueE;
    }

    @Override
    public DevopsDeployValueDTO entityToDo(DevopsDeployValueE pipelineValueE) {
        DevopsDeployValueDTO pipelineValueDO = new DevopsDeployValueDTO();
        BeanUtils.copyProperties(pipelineValueE, pipelineValueDO);
        return pipelineValueDO;
    }


    @Override
    public DevopsDeployValueE dtoToEntity(DevopsDeployValueVO pipelineValueDTO) {
        DevopsDeployValueE pipelineValueE = new DevopsDeployValueE();
        BeanUtils.copyProperties(pipelineValueDTO, pipelineValueE);
        return pipelineValueE;
    }

    @Override
    public DevopsDeployValueVO entityToDto(DevopsDeployValueE pipelineValueE) {
        DevopsDeployValueVO pipelineValueDTO = new DevopsDeployValueVO();
        BeanUtils.copyProperties(pipelineValueE, pipelineValueDTO);
        return pipelineValueDTO;
    }
}
