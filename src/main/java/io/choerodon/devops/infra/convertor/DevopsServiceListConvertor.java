package io.choerodon.devops.infra.convertor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.devops.api.vo.DevopsServiceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsServiceConfigDTO;
import io.choerodon.devops.api.vo.DevopsServiceTargetDTO;
import io.choerodon.devops.api.vo.EndPointPortDTO;
import io.choerodon.devops.infra.dto.PortMapDTO;
import io.choerodon.devops.domain.application.valueobject.DevopsServiceV;
import io.choerodon.devops.infra.dto.DevopsServiceQueryDTO;


/**
 * Created by Zenger on 2018/4/20.
 */
@Component
public class DevopsServiceListConvertor implements ConvertorI<DevopsServiceV, DevopsServiceQueryDTO, DevopsServiceVO> {
    private Gson gson = new Gson();

    @Override
    public DevopsServiceVO entityToDto(DevopsServiceV entity) {
        DevopsServiceVO devopsServiceVO = new DevopsServiceVO();
        BeanUtils.copyProperties(entity, devopsServiceVO);

        DevopsServiceConfigDTO devopsServiceConfigDTO = new DevopsServiceConfigDTO();
        devopsServiceConfigDTO.setPorts(entity.getPorts());
        if (entity.getExternalIp() != null) {
            devopsServiceConfigDTO.setExternalIps(new ArrayList<>(
                    Arrays.asList(entity.getExternalIp().split(","))));
        }
        devopsServiceVO.setConfig(devopsServiceConfigDTO);

        DevopsServiceTargetDTO devopsServiceTargetDTO = new DevopsServiceTargetDTO();
        devopsServiceTargetDTO.setAppInstance(entity.getAppInstance());
        devopsServiceTargetDTO.setLabels(entity.getLabels());
        devopsServiceTargetDTO.setEndPoints(entity.getEndPoinits());
        devopsServiceVO.setTarget(devopsServiceTargetDTO);

        return devopsServiceVO;
    }

    @Override
    public DevopsServiceV doToEntity(DevopsServiceQueryDTO dataObject) {
        DevopsServiceV devopsServiceV = new DevopsServiceV();
        BeanUtils.copyProperties(dataObject, devopsServiceV);
        devopsServiceV.setPorts(gson.fromJson(dataObject.getPorts(), new TypeToken<ArrayList<PortMapDTO>>() {
        }.getType()));
        if (dataObject.getEndPoints() != null) {
            devopsServiceV.setEndPoinits(gson.fromJson(dataObject.getEndPoints(), new TypeToken<Map<String, List<EndPointPortDTO>>>() {
            }.getType()));
        }
        if (dataObject.getLabels() != null) {
            devopsServiceV.setLabels(gson.fromJson(dataObject.getLabels(), new TypeToken<Map<String, String>>() {
            }.getType()));
        }
        return devopsServiceV;
    }
}
