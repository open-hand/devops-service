package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsCertificationProRelE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsCertificationProRelConvertor.java

import io.choerodon.devops.infra.dto.DevopsCertificationProRelDO;
=======
import io.choerodon.devops.infra.dataobject.DevopsCertificationProRelDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsCertificationProRelConvertor.java

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
