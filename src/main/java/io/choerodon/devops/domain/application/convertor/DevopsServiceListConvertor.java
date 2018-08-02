package io.choerodon.devops.domain.application.convertor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsServiceDTO;
import io.choerodon.devops.domain.application.entity.PortMapE;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;
import io.choerodon.devops.infra.dataobject.DevopsServiceQueryDO;

/**
 * Created by Zenger on 2018/4/20.
 */
@Component
public class DevopsServiceListConvertor implements ConvertorI<DevopsServiceV, DevopsServiceQueryDO, DevopsServiceDTO> {

    @Override
    public DevopsServiceDTO entityToDto(DevopsServiceV entity) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        BeanUtils.copyProperties(entity, devopsServiceDTO);
        devopsServiceDTO.setPorts(
                Arrays.stream(entity.getPorts().split(","))
                        .map(PortMapE::new).collect(Collectors.toList()));
        devopsServiceDTO.setExternalIps(new ArrayList<>(
                Arrays.asList(entity.getExternalIp().split(","))));
        return devopsServiceDTO;
    }

    @Override
    public DevopsServiceV doToEntity(DevopsServiceQueryDO dataObject) {
        DevopsServiceV devopsServiceV = new DevopsServiceV();
        BeanUtils.copyProperties(dataObject, devopsServiceV);
        return devopsServiceV;
    }
}
