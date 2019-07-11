package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvApplicationDTO;
import io.choerodon.devops.domain.application.entity.DevopsEnvApplicationE;
import io.choerodon.devops.infra.dataobject.DevopsEnvApplicationDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author lizongwei
 * @date 2019/7/1
 */
@Component
public class DevopsEnvApplicationConvertor implements ConvertorI<DevopsEnvApplicationE, DevopsEnvApplicationDO, DevopsEnvApplicationDTO> {

    @Override
    public DevopsEnvApplicationE doToEntity(DevopsEnvApplicationDO devopsEnvApplicationDO) {
        DevopsEnvApplicationE devopsEnvApplicationE = new DevopsEnvApplicationE();
        BeanUtils.copyProperties(devopsEnvApplicationDO, devopsEnvApplicationE);
        return devopsEnvApplicationE;
    }

    @Override
    public DevopsEnvApplicationDO entityToDo(DevopsEnvApplicationE devopsEnvApplicationE) {
        DevopsEnvApplicationDO devopsEnvApplicationDO = new DevopsEnvApplicationDO();
        BeanUtils.copyProperties(devopsEnvApplicationE, devopsEnvApplicationDO);
        return devopsEnvApplicationDO;
    }

    @Override
    public DevopsEnvApplicationE dtoToEntity(DevopsEnvApplicationDTO devopsEnvApplicationDTO) {
        DevopsEnvApplicationE devopsEnvApplicationE = new DevopsEnvApplicationE();
        BeanUtils.copyProperties(devopsEnvApplicationDTO, devopsEnvApplicationE);
        return devopsEnvApplicationE;
    }

    @Override
    public DevopsEnvApplicationDTO entityToDto(DevopsEnvApplicationE devopsEnvApplicationE) {
        DevopsEnvApplicationDTO devopsEnvApplicationDTO = new DevopsEnvApplicationDTO();
        BeanUtils.copyProperties(devopsEnvApplicationE, devopsEnvApplicationDTO);
        return devopsEnvApplicationDTO;
    }

}
