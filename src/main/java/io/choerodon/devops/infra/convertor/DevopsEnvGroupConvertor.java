package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvGroupVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvGroupE;
import io.choerodon.devops.infra.dto.DevopsEnvGroupDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsEnvGroupConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsEnvGroupDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvGroupE;
import io.choerodon.devops.infra.dataobject.DevopsEnvGroupDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsEnvGroupConvertor.java
=======
>>>>>>> [IMP] 重构Repository

@Component
public class DevopsEnvGroupConvertor implements ConvertorI<DevopsEnvGroupE, DevopsEnvGroupDTO, DevopsEnvGroupVO> {

    @Override
    public DevopsEnvGroupE doToEntity(DevopsEnvGroupDTO devopsEnvGroupDO) {
        DevopsEnvGroupE devopsEnvGroupE = new DevopsEnvGroupE();
        BeanUtils.copyProperties(devopsEnvGroupDO, devopsEnvGroupE);
        devopsEnvGroupE.initProject(devopsEnvGroupDO.getProjectId());
        return devopsEnvGroupE;
    }

    @Override
    public DevopsEnvGroupDTO entityToDo(DevopsEnvGroupE devopsEnvGroupE) {
        DevopsEnvGroupDTO devopsEnvGroupDO = new DevopsEnvGroupDTO();
        BeanUtils.copyProperties(devopsEnvGroupE, devopsEnvGroupDO);
        if (devopsEnvGroupE.getProjectE() != null) {
            devopsEnvGroupDO.setProjectId(devopsEnvGroupE.getProjectE().getId());
        }
        return devopsEnvGroupDO;
    }


    @Override
    public DevopsEnvGroupVO entityToDto(DevopsEnvGroupE devopsEnvGroupE) {
        DevopsEnvGroupVO devopsEnvGroupDTO = new DevopsEnvGroupVO();
        BeanUtils.copyProperties(devopsEnvGroupE, devopsEnvGroupDTO);
        devopsEnvGroupDTO.setProjectId(devopsEnvGroupE.getProjectE().getId());
        return devopsEnvGroupDTO;
    }

    @Override
    public DevopsEnvGroupE dtoToEntity(DevopsEnvGroupVO devopsEnvGroupDTO) {
        DevopsEnvGroupE devopsEnvGroupE = new DevopsEnvGroupE();
        BeanUtils.copyProperties(devopsEnvGroupDTO, devopsEnvGroupE);
        devopsEnvGroupE.initProject(devopsEnvGroupDTO.getProjectId());
        return devopsEnvGroupE;
    }
}
