package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsServiceAppInstanceE;
import io.choerodon.devops.infra.dto.DevopsServiceAppInstanceDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsServiceAppInstanceConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsServiceAppInstanceE;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsServiceAppInstanceConvertor.java
>>>>>>> [IMP]重构后端断码
import io.choerodon.devops.infra.dataobject.DevopsServiceAppInstanceDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsServiceAppInstanceConvertor.java

/**
 * Created by Zenger on 2018/4/19.
 */
@Service
public class DevopsServiceAppInstanceConvertor implements ConvertorI<DevopsServiceAppInstanceE, DevopsServiceAppInstanceDTO, Object> {

    @Override
    public DevopsServiceAppInstanceE doToEntity(DevopsServiceAppInstanceDTO dataObject) {
        DevopsServiceAppInstanceE devopsServiceAppInstanceE = new DevopsServiceAppInstanceE();
        BeanUtils.copyProperties(dataObject, devopsServiceAppInstanceE);
        return devopsServiceAppInstanceE;
    }

    @Override
    public DevopsServiceAppInstanceDTO entityToDo(DevopsServiceAppInstanceE entity) {
        DevopsServiceAppInstanceDTO devopsServiceAppInstanceDTO = new DevopsServiceAppInstanceDTO();
        BeanUtils.copyProperties(entity, devopsServiceAppInstanceDTO);
        return devopsServiceAppInstanceDTO;
    }
}
