package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsIngressPathVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsIngressPathE;
import io.choerodon.devops.infra.dto.DevopsIngressPathDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/DevopsIngerssPathConvertor.java
=======
import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsIngressPathDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsIngressPathE;
=======
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsIngerssPathConvertor.java
>>>>>>> [IMP]重构后端断码
import io.choerodon.devops.infra.dataobject.DevopsIngressPathDO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/DevopsIngerssPathConvertor.java

/**
 * Created by younger on 2018/4/28.
 */
@Component
public class DevopsIngerssPathConvertor implements ConvertorI<DevopsIngressPathE, DevopsIngressPathDTO, DevopsIngressPathVO> {

    @Override
    public DevopsIngressPathE dtoToEntity(DevopsIngressPathVO dto) {
        DevopsIngressPathE devopsIngressPathE = new DevopsIngressPathE();
        BeanUtils.copyProperties(dto, devopsIngressPathE);
        return devopsIngressPathE;
    }

    @Override
    public DevopsIngressPathVO entityToDto(DevopsIngressPathE entity) {
        DevopsIngressPathVO devopsIngressPathVO = new DevopsIngressPathVO();
        BeanUtils.copyProperties(entity, devopsIngressPathVO);
        return devopsIngressPathVO;
    }

    @Override
    public DevopsIngressPathE doToEntity(DevopsIngressPathDTO dataObject) {
        DevopsIngressPathE devopsIngressPathE = new DevopsIngressPathE();
        BeanUtils.copyProperties(dataObject, devopsIngressPathE);
        devopsIngressPathE.initDevopsIngressE(dataObject.getIngressId());
        return devopsIngressPathE;
    }

    @Override
    public DevopsIngressPathDTO entityToDo(DevopsIngressPathE entity) {
        DevopsIngressPathDTO devopsIngressPathDTO = new DevopsIngressPathDTO();
        BeanUtils.copyProperties(entity, devopsIngressPathDTO);
        devopsIngressPathDTO.setIngressId(entity.getDevopsIngressE().getId());
        return devopsIngressPathDTO;
    }
}
