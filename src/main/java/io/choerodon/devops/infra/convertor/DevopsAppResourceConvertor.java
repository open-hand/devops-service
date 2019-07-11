package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsAppResourceE;
import io.choerodon.devops.infra.dto.DevopsAppResourceDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
@Component
public class DevopsAppResourceConvertor implements ConvertorI<DevopsAppResourceE, DevopsAppResourceDO, Object> {

    @Override
    public DevopsAppResourceE doToEntity(DevopsAppResourceDO resourceDO) {
        DevopsAppResourceE resourceE = new DevopsAppResourceE();
        BeanUtils.copyProperties(resourceDO, resourceE);
        return resourceE;
    }

    @Override
    public DevopsAppResourceDO entityToDo(DevopsAppResourceE resourceE) {
        DevopsAppResourceDO resourceDO = new DevopsAppResourceDO();
        BeanUtils.copyProperties(resourceE, resourceDO);
        return resourceDO;
    }
}
