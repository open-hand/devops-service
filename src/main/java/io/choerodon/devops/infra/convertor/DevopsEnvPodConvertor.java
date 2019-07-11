package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvPodDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvPodE;
import io.choerodon.devops.infra.dto.DevopsEnvPodDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Created by Zenger on 2018/4/2.
 */
@Component
public class DevopsEnvPodConvertor implements ConvertorI<DevopsEnvPodE, DevopsEnvPodDO, DevopsEnvPodDTO> {

    @Override
    public DevopsEnvPodDTO entityToDto(DevopsEnvPodE entity) {
        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        BeanUtils.copyProperties(entity, devopsEnvPodDTO);
        return devopsEnvPodDTO;
    }

    @Override
    public DevopsEnvPodE doToEntity(DevopsEnvPodDO dataObject) {
        DevopsEnvPodE devopsEnvPodE = new DevopsEnvPodE();
        BeanUtils.copyProperties(dataObject, devopsEnvPodE);
        devopsEnvPodE.initApplicationInstanceE(dataObject.getAppInstanceId());
        return devopsEnvPodE;
    }

    @Override
    public DevopsEnvPodDO entityToDo(DevopsEnvPodE entity) {
        DevopsEnvPodDO devopsEnvPodDO = new DevopsEnvPodDO();
        if (entity.getApplicationInstanceE() != null) {
            devopsEnvPodDO.setAppInstanceId(entity.getApplicationInstanceE().getId());
        }
        BeanUtils.copyProperties(entity, devopsEnvPodDO);
        return devopsEnvPodDO;
    }


    @Override
    public DevopsEnvPodDTO doToDto(DevopsEnvPodDO devopsEnvPodDO) {
        DevopsEnvPodDTO devopsEnvPodDTO = new DevopsEnvPodDTO();
        BeanUtils.copyProperties(devopsEnvPodDO, devopsEnvPodDTO);
        devopsEnvPodDTO.setCreationDate(devopsEnvPodDO.getCreationDate());
        return devopsEnvPodDTO;
    }
}
