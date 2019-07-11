package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsServiceAppInstanceE;
import io.choerodon.devops.infra.dto.DevopsServiceAppInstanceDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsServiceAppInstanceConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsServiceAppInstanceE;
import io.choerodon.devops.infra.dataobject.DevopsServiceAppInstanceDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsServiceAppInstanceConvertor.java

/**
 * Created by Zenger on 2018/4/19.
 */
@Service
public class DevopsServiceAppInstanceConvertor implements ConvertorI<DevopsServiceAppInstanceE, DevopsServiceAppInstanceDO, Object> {

    @Override
    public DevopsServiceAppInstanceE doToEntity(DevopsServiceAppInstanceDO dataObject) {
        DevopsServiceAppInstanceE devopsServiceAppInstanceE = new DevopsServiceAppInstanceE();
        BeanUtils.copyProperties(dataObject, devopsServiceAppInstanceE);
        return devopsServiceAppInstanceE;
    }

    @Override
    public DevopsServiceAppInstanceDO entityToDo(DevopsServiceAppInstanceE entity) {
        DevopsServiceAppInstanceDO devopsServiceAppInstanceDO = new DevopsServiceAppInstanceDO();
        BeanUtils.copyProperties(entity, devopsServiceAppInstanceDO);
        return devopsServiceAppInstanceDO;
    }
}
