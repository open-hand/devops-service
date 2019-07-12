package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvCommandLogVO;
import io.choerodon.devops.domain.application.factory.DevopsInstanceResourceLogFactory;
import io.choerodon.devops.infra.dto.DevopsEnvCommandLogDO;

/**
 * Created by younger on 2018/4/24.
 */
@Component
public class DevopsEnvCommandLogConvertor implements ConvertorI<DevopsEnvCommandLogVO, DevopsEnvCommandLogDO, Object> {

    @Override
    public DevopsEnvCommandLogVO doToEntity(DevopsEnvCommandLogDO devopsEnvCommandLogDO) {
        DevopsEnvCommandLogVO devopsEnvCommandLogE =
                DevopsInstanceResourceLogFactory.createDevopsInstanceResourceLogE();
        BeanUtils.copyProperties(devopsEnvCommandLogDO, devopsEnvCommandLogE);
        if (devopsEnvCommandLogDO.getCommandId() != null) {
            devopsEnvCommandLogE.initDevopsEnvCommandE(devopsEnvCommandLogDO.getCommandId());
        }
        return devopsEnvCommandLogE;
    }

    @Override
    public DevopsEnvCommandLogDO entityToDo(DevopsEnvCommandLogVO devopsEnvCommandLogE) {
        DevopsEnvCommandLogDO devopsEnvCommandLogDO = new DevopsEnvCommandLogDO();
        BeanUtils.copyProperties(devopsEnvCommandLogE, devopsEnvCommandLogDO);
        if (devopsEnvCommandLogE.getDevopsEnvCommandVO() != null) {
            devopsEnvCommandLogDO.setCommandId(devopsEnvCommandLogE.getDevopsEnvCommandVO().getId());
        }
        return devopsEnvCommandLogDO;
    }

}
