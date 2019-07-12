package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceContentE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsCustomizeResourceContentConvertor.java
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsCustomizeResourceContentDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsCustomizeResourceContentConvertor.java
=======
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceContentVO;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsCustomizeResourceContentConvertor.java
import io.choerodon.devops.infra.dataobject.DevopsCustomizeResourceContentDO;
=======
import io.choerodon.devops.infra.dto.DevopsCustomizeResourceContentDTO;
>>>>>>> f7b3373a9ccceea0bbd4235a0e8f042f20369f6a:src/main/java/io/choerodon/devops/infra/convertor/DevopsCustomizeResourceContentConvertor.java
>>>>>>> [IMP] 修改repository重构
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Created by Sheep on 2019/6/27.
 */

@Component
public class DevopsCustomizeResourceContentConvertor implements ConvertorI<DevopsCustomizeResourceContentVO, DevopsCustomizeResourceContentDTO, Object> {


    @Override
    public DevopsCustomizeResourceContentVO doToEntity(DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDO) {
        DevopsCustomizeResourceContentVO devopsCustomizeResourceContentE = new DevopsCustomizeResourceContentVO();
        BeanUtils.copyProperties(devopsCustomizeResourceContentDO, devopsCustomizeResourceContentE);
        return devopsCustomizeResourceContentE;
    }

    @Override
    public DevopsCustomizeResourceContentDTO entityToDo(DevopsCustomizeResourceContentVO devopsCustomizeResourceContentE) {
        DevopsCustomizeResourceContentDTO devopsCustomizeResourceContentDO = new DevopsCustomizeResourceContentDTO();
        BeanUtils.copyProperties(devopsCustomizeResourceContentE, devopsCustomizeResourceContentDO);
        return devopsCustomizeResourceContentDO;
    }


}
