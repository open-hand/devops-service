package io.choerodon.devops.infra.convertor;


import io.choerodon.devops.infra.dto.DevopsEnvironmentPodDTO;
import org.springframework.stereotype.Component;

/**
 * Created by Zenger on 2018/4/2.
 */
@Component
public class DevopsEnvPodConvertor implements ConvertorI<DevopsEnvPodE, DevopsEnvironmentPodDTO, DevopsEnvironmentPodVO> {

    @Override
    public DevopsEnvironmentPodVO entityToDto(DevopsEnvPodE entity) {
        DevopsEnvironmentPodVO devopsEnvironmentPodVO = new DevopsEnvironmentPodVO();
        BeanUtils.copyProperties(entity, devopsEnvironmentPodVO);
        return devopsEnvironmentPodVO;
    }

    @Override
    public DevopsEnvPodE doToEntity(DevopsEnvironmentPodDTO dataObject) {
        DevopsEnvPodE devopsEnvPodE = new DevopsEnvPodE();
        BeanUtils.copyProperties(dataObject, devopsEnvPodE);
        devopsEnvPodE.initApplicationInstanceE(dataObject.getAppInstanceId());
        return devopsEnvPodE;
    }

    @Override
    public DevopsEnvironmentPodDTO entityToDo(DevopsEnvPodE entity) {
        DevopsEnvironmentPodDTO devopsEnvironmentPodDTO = new DevopsEnvironmentPodDTO();
        if (entity.getApplicationInstanceE() != null) {
            devopsEnvironmentPodDTO.setAppInstanceId(entity.getApplicationInstanceE().getId());
        }
        BeanUtils.copyProperties(entity, devopsEnvironmentPodDTO);
        return devopsEnvironmentPodDTO;
    }


    @Override
    public DevopsEnvironmentPodVO doToDto(DevopsEnvironmentPodDTO devopsEnvironmentPodDTO) {
        DevopsEnvironmentPodVO devopsEnvironmentPodVO = new DevopsEnvironmentPodVO();
        BeanUtils.copyProperties(devopsEnvironmentPodDTO, devopsEnvironmentPodVO);
        devopsEnvironmentPodVO.setCreationDate(devopsEnvironmentPodDTO.getCreationDate());
        return devopsEnvironmentPodVO;
    }
}
