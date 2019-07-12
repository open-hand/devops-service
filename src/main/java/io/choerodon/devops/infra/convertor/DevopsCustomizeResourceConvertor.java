package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsCustomizeResourceVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceContentVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceE;
<<<<<<< HEAD
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsCustomizeResourceConvertor.java
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsCustomizeResourceDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsCustomizeResourceConvertor.java
=======
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandVO;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsCustomizeResourceConvertor.java
import io.choerodon.devops.infra.dataobject.DevopsCustomizeResourceDO;
=======
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/DevopsCustomizeResourceConvertor.java
>>>>>>> [IMP] 修改repository重构
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Created by Sheep on 2019/6/27.
 */


@Component
public class DevopsCustomizeResourceConvertor implements ConvertorI<DevopsCustomizeResourceE, DevopsCustomizeResourceDTO, DevopsCustomizeResourceVO> {


    @Override
    public DevopsCustomizeResourceE doToEntity(DevopsCustomizeResourceDTO devopsCustomizeResourceDO) {
        DevopsCustomizeResourceE devopsCustomizeResourceE = new DevopsCustomizeResourceE();
        if (devopsCustomizeResourceDO.getCommandId() != null) {
            devopsCustomizeResourceE.setDevopsEnvCommandE(new DevopsEnvCommandVO(devopsCustomizeResourceDO.getCommandId(), devopsCustomizeResourceDO.getCommandStatus(), devopsCustomizeResourceDO.getCommandError()));
        }
        if (devopsCustomizeResourceDO.getContentId() != null) {
            DevopsCustomizeResourceContentVO devopsCustomizeResourceContentE = new DevopsCustomizeResourceContentVO(devopsCustomizeResourceDO.getContentId());
            if (devopsCustomizeResourceDO.getResourceContent() != null) {
                devopsCustomizeResourceContentE.setContent(devopsCustomizeResourceDO.getResourceContent());
            }
            devopsCustomizeResourceE.setDevopsCustomizeResourceContentE(devopsCustomizeResourceContentE);
        }
        BeanUtils.copyProperties(devopsCustomizeResourceDO, devopsCustomizeResourceE);
        return devopsCustomizeResourceE;
    }

    @Override
    public DevopsCustomizeResourceDTO entityToDo(DevopsCustomizeResourceE devopsCustomizeResourceE) {
        DevopsCustomizeResourceDTO devopsCustomizeResourceDO = new DevopsCustomizeResourceDTO();
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
    public DevopsCustomizeResourceVO entityToDto(DevopsCustomizeResourceE devopsCustomizeResourceE) {
        DevopsCustomizeResourceVO devopsCustomizeResourceDTO = new DevopsCustomizeResourceVO();
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
