package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvResourceDetailE;
import io.choerodon.devops.domain.application.factory.DevopsInstanceResourceMessageFactory;
import io.choerodon.devops.infra.dto.DevopsEnvResourceDetailDTO;

/**
 * Created by younger on 2018/4/24.
 */
@Component
public class DevopsEnvResourceDetailConvertor implements ConvertorI<DevopsEnvResourceDetailE, DevopsEnvResourceDetailDTO, Object> {


    @Override
    public DevopsEnvResourceDetailE doToEntity(DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO) {
        DevopsEnvResourceDetailE devopsEnvResourceDetailE =
                DevopsInstanceResourceMessageFactory.createDevopsInstanceResourceMessageE();
        BeanUtils.copyProperties(devopsEnvResourceDetailDO, devopsEnvResourceDetailE);
        return devopsEnvResourceDetailE;
    }

    @Override
    public DevopsEnvResourceDetailDTO entityToDo(DevopsEnvResourceDetailE devopsEnvResourceDetailE) {
        DevopsEnvResourceDetailDTO devopsEnvResourceDetailDO = new DevopsEnvResourceDetailDTO();
        BeanUtils.copyProperties(devopsEnvResourceDetailE, devopsEnvResourceDetailDO);
        return devopsEnvResourceDetailDO;
    }
}
