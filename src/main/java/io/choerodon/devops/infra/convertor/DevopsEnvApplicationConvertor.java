package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvApplicationVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvApplicationE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsEnvApplicationConvertor.java
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsEnvApplicationDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvApplicationConvertor.java
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvApplicationConvertor.java
import io.choerodon.devops.infra.dataobject.DevopsEnvApplicationDO;
=======
import io.choerodon.devops.infra.dto.DevopsEnvApplicationDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/DevopsEnvApplicationConvertor.java
>>>>>>> [IMP] 修改repository重构
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Component
public class DevopsEnvApplicationConvertor implements ConvertorI<DevopsEnvApplicationE, DevopsEnvApplicationDTO, DevopsEnvApplicationVO> {

    @Override
    public DevopsEnvApplicationE doToEntity(DevopsEnvApplicationDTO devopsEnvApplicationDO) {
        DevopsEnvApplicationE devopsEnvApplicationE = new DevopsEnvApplicationE();
        BeanUtils.copyProperties(devopsEnvApplicationDO, devopsEnvApplicationE);
        return devopsEnvApplicationE;
    }

    @Override
    public DevopsEnvApplicationDTO entityToDo(DevopsEnvApplicationE devopsEnvApplicationE) {
        DevopsEnvApplicationDTO devopsEnvApplicationDO = new DevopsEnvApplicationDTO();
        BeanUtils.copyProperties(devopsEnvApplicationE, devopsEnvApplicationDO);
        return devopsEnvApplicationDO;
    }

    @Override
    public DevopsEnvApplicationE dtoToEntity(DevopsEnvApplicationVO devopsEnvApplicationDTO) {
        DevopsEnvApplicationE devopsEnvApplicationE = new DevopsEnvApplicationE();
        BeanUtils.copyProperties(devopsEnvApplicationDTO, devopsEnvApplicationE);
        return devopsEnvApplicationE;
    }

    @Override
    public DevopsEnvApplicationVO entityToDto(DevopsEnvApplicationE devopsEnvApplicationE) {
        DevopsEnvApplicationVO devopsEnvApplicationDTO = new DevopsEnvApplicationVO();
        BeanUtils.copyProperties(devopsEnvApplicationE, devopsEnvApplicationDTO);
        return devopsEnvApplicationDTO;
    }

}
