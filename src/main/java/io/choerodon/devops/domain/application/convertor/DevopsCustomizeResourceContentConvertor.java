package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsCustomizeResourceContentE;
import io.choerodon.devops.infra.dataobject.DevopsCustomizeResourceContentDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Created by Sheep on 2019/6/27.
 */

@Component
public class DevopsCustomizeResourceContentConvertor implements ConvertorI<DevopsCustomizeResourceContentE, DevopsCustomizeResourceContentDO, Object> {


    @Override
    public DevopsCustomizeResourceContentE doToEntity(DevopsCustomizeResourceContentDO devopsCustomizeResourceContentDO) {
        DevopsCustomizeResourceContentE devopsCustomizeResourceContentE = new DevopsCustomizeResourceContentE();
        BeanUtils.copyProperties(devopsCustomizeResourceContentDO, devopsCustomizeResourceContentE);
        return devopsCustomizeResourceContentE;
    }

    @Override
    public DevopsCustomizeResourceContentDO entityToDo(DevopsCustomizeResourceContentE devopsCustomizeResourceContentE) {
        DevopsCustomizeResourceContentDO devopsCustomizeResourceContentDO = new DevopsCustomizeResourceContentDO();
        BeanUtils.copyProperties(devopsCustomizeResourceContentE, devopsCustomizeResourceContentDO);
        return devopsCustomizeResourceContentDO;
    }


}
