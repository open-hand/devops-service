package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsIngressPathDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsIngressPathE;
import io.choerodon.devops.infra.dto.DevopsIngressPathDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsIngerssPathConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsIngressPathDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsIngressPathE;
import io.choerodon.devops.infra.dataobject.DevopsIngressPathDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsIngerssPathConvertor.java

/**
 * Created by younger on 2018/4/28.
 */
@Component
public class DevopsIngerssPathConvertor implements ConvertorI<DevopsIngressPathE, DevopsIngressPathDO, DevopsIngressPathDTO> {

    @Override
    public DevopsIngressPathE dtoToEntity(DevopsIngressPathDTO dto) {
        DevopsIngressPathE devopsIngressPathE = new DevopsIngressPathE();
        BeanUtils.copyProperties(dto, devopsIngressPathE);
        return devopsIngressPathE;
    }

    @Override
    public DevopsIngressPathDTO entityToDto(DevopsIngressPathE entity) {
        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        BeanUtils.copyProperties(entity, devopsIngressPathDTO);
        return devopsIngressPathDTO;
    }

    @Override
    public DevopsIngressPathE doToEntity(DevopsIngressPathDO dataObject) {
        DevopsIngressPathE devopsIngressPathE = new DevopsIngressPathE();
        BeanUtils.copyProperties(dataObject, devopsIngressPathE);
        devopsIngressPathE.initDevopsIngressE(dataObject.getIngressId());
        return devopsIngressPathE;
    }

    @Override
    public DevopsIngressPathDO entityToDo(DevopsIngressPathE entity) {
        DevopsIngressPathDO devopsIngressPathDO = new DevopsIngressPathDO();
        BeanUtils.copyProperties(entity, devopsIngressPathDO);
        devopsIngressPathDO.setIngressId(entity.getDevopsIngressE().getId());
        return devopsIngressPathDO;
    }
}
