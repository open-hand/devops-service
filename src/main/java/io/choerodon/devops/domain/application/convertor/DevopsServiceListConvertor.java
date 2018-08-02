package io.choerodon.devops.domain.application.convertor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
    private Gson gson = new Gson();

    @Override
    public DevopsServiceDTO entityToDto(DevopsServiceV entity) {
        DevopsServiceDTO devopsServiceDTO = new DevopsServiceDTO();
        BeanUtils.copyProperties(entity, devopsServiceDTO);
        if (entity.getExternalIp() != null) {
            devopsServiceDTO.setExternalIps(new ArrayList<>(
                    Arrays.asList(entity.getExternalIp().split(","))));
        }
        return devopsServiceDTO;
    }

    @Override
    public DevopsServiceV doToEntity(DevopsServiceQueryDO dataObject) {
        DevopsServiceV devopsServiceV = new DevopsServiceV();
        BeanUtils.copyProperties(dataObject, devopsServiceV);
        devopsServiceV.setPorts(gson.fromJson(dataObject.getPorts(), new TypeToken<ArrayList<PortMapE>>() {
        }.getType()));
        return devopsServiceV;
    }
}
