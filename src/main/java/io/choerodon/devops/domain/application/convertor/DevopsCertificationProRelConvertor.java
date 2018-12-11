package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.domain.application.entity.DevopsCertificationProRelE;
import io.choerodon.devops.infra.dataobject.DevopsCertificationProRelDO;

@Component
public class DevopsCertificationProRelConvertor implements ConvertorI<DevopsCertificationProRelE, DevopsCertificationProRelDO, Object> {

    @Override
    public DevopsCertificationProRelE doToEntity(DevopsCertificationProRelDO devopsCertificationProRelDO) {
        DevopsCertificationProRelE devopsCertificationProRelE = new DevopsCertificationProRelE();
        BeanUtils.copyProperties(devopsCertificationProRelDO, devopsCertificationProRelE);
        return devopsCertificationProRelE;
    }

    @Override
    public DevopsCertificationProRelDO entityToDo(DevopsCertificationProRelE devopsCertificationProRelE) {
        DevopsCertificationProRelDO devopsCertificationProRelDO = new DevopsCertificationProRelDO();
        BeanUtils.copyProperties(devopsCertificationProRelE, devopsCertificationProRelDO);
        return devopsCertificationProRelDO;
    }
}
