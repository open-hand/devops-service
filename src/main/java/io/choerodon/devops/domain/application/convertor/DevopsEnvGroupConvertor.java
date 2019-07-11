package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvGroupDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvGroupE;
import io.choerodon.devops.infra.dataobject.DevopsEnvGroupDO;

@Component
public class DevopsEnvGroupConvertor implements ConvertorI<DevopsEnvGroupE, DevopsEnvGroupDO, DevopsEnvGroupDTO> {

    @Override
    public DevopsEnvGroupE doToEntity(DevopsEnvGroupDO devopsEnvGroupDO) {
        DevopsEnvGroupE devopsEnvGroupE = new DevopsEnvGroupE();
        BeanUtils.copyProperties(devopsEnvGroupDO, devopsEnvGroupE);
        devopsEnvGroupE.initProject(devopsEnvGroupDO.getProjectId());
        return devopsEnvGroupE;
    }

    @Override
    public DevopsEnvGroupDO entityToDo(DevopsEnvGroupE devopsEnvGroupE) {
        DevopsEnvGroupDO devopsEnvGroupDO = new DevopsEnvGroupDO();
        BeanUtils.copyProperties(devopsEnvGroupE, devopsEnvGroupDO);
        if (devopsEnvGroupE.getProjectE() != null) {
            devopsEnvGroupDO.setProjectId(devopsEnvGroupE.getProjectE().getId());
        }
        return devopsEnvGroupDO;
    }


    @Override
    public DevopsEnvGroupDTO entityToDto(DevopsEnvGroupE devopsEnvGroupE) {
        DevopsEnvGroupDTO devopsEnvGroupDTO = new DevopsEnvGroupDTO();
        BeanUtils.copyProperties(devopsEnvGroupE, devopsEnvGroupDTO);
        devopsEnvGroupDTO.setProjectId(devopsEnvGroupE.getProjectE().getId());
        return devopsEnvGroupDTO;
    }

    @Override
    public DevopsEnvGroupE dtoToEntity(DevopsEnvGroupDTO devopsEnvGroupDTO) {
        DevopsEnvGroupE devopsEnvGroupE = new DevopsEnvGroupE();
        BeanUtils.copyProperties(devopsEnvGroupDTO, devopsEnvGroupE);
        devopsEnvGroupE.initProject(devopsEnvGroupDTO.getProjectId());
        return devopsEnvGroupE;
    }
}
