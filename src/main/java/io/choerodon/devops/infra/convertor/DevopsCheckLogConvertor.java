package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsCheckLogE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsCheckLogConvertor.java
import io.choerodon.devops.infra.dto.DevopsCheckLogDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsCheckLogDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsCheckLogConvertor.java
=======
import io.choerodon.devops.infra.dto.DevopsCheckLogDTO;
>>>>>>> [REF] refactor DevopsCheckLogRepository

@Component
public class DevopsCheckLogConvertor implements ConvertorI<DevopsCheckLogE, DevopsCheckLogDTO, Object> {


    @Override
    public DevopsCheckLogDTO entityToDo(DevopsCheckLogE devopsCheckLogE) {
        DevopsCheckLogDTO devopsCheckLogDTO = new DevopsCheckLogDTO();
        BeanUtils.copyProperties(devopsCheckLogE, devopsCheckLogDTO);
        return devopsCheckLogDTO;
    }

    @Override
    public DevopsCheckLogE doToEntity(DevopsCheckLogDTO devopsCheckLogDTO) {
        DevopsCheckLogE devopsCheckLogE = new DevopsCheckLogE();
        BeanUtils.copyProperties(devopsCheckLogDTO, devopsCheckLogE);
        return devopsCheckLogE;
    }

}
