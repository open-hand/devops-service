package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.DevopsProjectE;
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


/**
 * Created by younger on 2018/4/2.
 */
@Component
public class DevopsProjectConvertor implements ConvertorI<DevopsProjectE, DevopsProjectDTO, Object> {

    @Override
    public DevopsProjectE doToEntity(DevopsProjectDTO devopsProjectDO) {
        DevopsProjectE devopsProjectE = new DevopsProjectE();
        BeanUtils.copyProperties(devopsProjectDO, devopsProjectE);
        devopsProjectE.initProjectE(devopsProjectDO.getIamProjectId());
        return devopsProjectE;
    }

    @Override
    public DevopsProjectDTO entityToDo(DevopsProjectE devopsProjectE) {
        DevopsProjectDTO devopsProjectDO = new DevopsProjectDTO();
        BeanUtils.copyProperties(devopsProjectE, devopsProjectDO);
        devopsProjectDO.setIamProjectId(devopsProjectE.getProjectE().getId());
        return devopsProjectDO;
    }
}
