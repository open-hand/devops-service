package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsCustomizeResourceDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceContentE;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsCustomizeResourceConvertor.java
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsCustomizeResourceDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsCustomizeResourceConvertor.java
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Created by Sheep on 2019/6/27.
 */


@Component
public class DevopsCustomizeResourceConvertor implements ConvertorI<DevopsCustomizeResourceE, DevopsCustomizeResourceDO, DevopsCustomizeResourceDTO> {


    @Override
    public DevopsCustomizeResourceE doToEntity(DevopsCustomizeResourceDO devopsCustomizeResourceDO) {
        DevopsCustomizeResourceE devopsCustomizeResourceE = new DevopsCustomizeResourceE();
        if (devopsCustomizeResourceDO.getCommandId() != null) {
            devopsCustomizeResourceE.setDevopsEnvCommandE(new DevopsEnvCommandE(devopsCustomizeResourceDO.getCommandId(), devopsCustomizeResourceDO.getCommandStatus(), devopsCustomizeResourceDO.getCommandError()));
        }
        if (devopsCustomizeResourceDO.getContentId() != null) {
            DevopsCustomizeResourceContentE devopsCustomizeResourceContentE = new DevopsCustomizeResourceContentE(devopsCustomizeResourceDO.getContentId());
            if (devopsCustomizeResourceDO.getResourceContent() != null) {
                devopsCustomizeResourceContentE.setContent(devopsCustomizeResourceDO.getResourceContent());
            }
            devopsCustomizeResourceE.setDevopsCustomizeResourceContentE(devopsCustomizeResourceContentE);
        }
        BeanUtils.copyProperties(devopsCustomizeResourceDO, devopsCustomizeResourceE);
        return devopsCustomizeResourceE;
    }

    @Override
    public DevopsCustomizeResourceDO entityToDo(DevopsCustomizeResourceE devopsCustomizeResourceE) {
        DevopsCustomizeResourceDO devopsCustomizeResourceDO = new DevopsCustomizeResourceDO();
        BeanUtils.copyProperties(devopsCustomizeResourceE, devopsCustomizeResourceDO);
        if (devopsCustomizeResourceE.getDevopsEnvCommandE() != null) {
            devopsCustomizeResourceDO.setCommandId(devopsCustomizeResourceE.getDevopsEnvCommandE().getId());
        }
        if (devopsCustomizeResourceE.getDevopsCustomizeResourceContentE() != null) {
            devopsCustomizeResourceDO.setContentId(devopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getId());
        }
        return devopsCustomizeResourceDO;
    }


    @Override
    public DevopsCustomizeResourceDTO entityToDto(DevopsCustomizeResourceE devopsCustomizeResourceE) {
        DevopsCustomizeResourceDTO devopsCustomizeResourceDTO = new DevopsCustomizeResourceDTO();
        BeanUtils.copyProperties(devopsCustomizeResourceE, devopsCustomizeResourceDTO);
        if (devopsCustomizeResourceE.getDevopsEnvCommandE() != null) {
            devopsCustomizeResourceDTO.setCommandStatus(devopsCustomizeResourceE.getDevopsEnvCommandE().getStatus());
            devopsCustomizeResourceDTO.setCommandErrors(devopsCustomizeResourceE.getDevopsEnvCommandE().getError());
        }
        if (devopsCustomizeResourceE.getDevopsCustomizeResourceContentE() != null) {
            devopsCustomizeResourceDTO.setResourceContent(devopsCustomizeResourceE.getDevopsCustomizeResourceContentE().getContent());
        }
        return devopsCustomizeResourceDTO;
    }


}
