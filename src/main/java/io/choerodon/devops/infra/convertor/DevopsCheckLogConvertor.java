package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsCheckLogE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsCheckLogConvertor.java
import io.choerodon.devops.infra.dto.DevopsCheckLogDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsCheckLogDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsCheckLogConvertor.java

@Component
public class DevopsCheckLogConvertor implements ConvertorI<DevopsCheckLogE, DevopsCheckLogDO, Object> {


    @Override
    public DevopsCheckLogDO entityToDo(DevopsCheckLogE devopsCheckLogE) {
        DevopsCheckLogDO devopsCheckLogDO = new DevopsCheckLogDO();
        BeanUtils.copyProperties(devopsCheckLogE, devopsCheckLogDO);
        return devopsCheckLogDO;
    }

    @Override
    public DevopsCheckLogE doToEntity(DevopsCheckLogDO devopsCheckLogDO) {
        DevopsCheckLogE devopsCheckLogE = new DevopsCheckLogE();
        BeanUtils.copyProperties(devopsCheckLogDO, devopsCheckLogE);
        return devopsCheckLogE;
    }

}
