package io.choerodon.devops.domain.application.convertor;

import java.util.Map;

import com.google.gson.Gson;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.DevopsConfigMapRepDTO;
import io.choerodon.devops.domain.application.entity.DevopsConfigMapE;

@Component
public class DevopConfigMapRepConvertor implements ConvertorI<DevopsConfigMapE, Object, DevopsConfigMapRepDTO> {

    Gson gson = new Gson();

    @Override
    public DevopsConfigMapRepDTO entityToDto(DevopsConfigMapE devopsConfigMapE) {
        DevopsConfigMapRepDTO devopsConfigMapRepDTO = new DevopsConfigMapRepDTO();
        devopsConfigMapRepDTO.setValue(gson.fromJson(devopsConfigMapE.getValue(), Map.class));
        BeanUtils.copyProperties(devopsConfigMapE, devopsConfigMapRepDTO);
        if (devopsConfigMapE.getDevopsEnvCommandE() != null) {
            devopsConfigMapRepDTO.setCommandId(devopsConfigMapE.getDevopsEnvCommandE().getId());
        }
        if (devopsConfigMapE.getDevopsEnvironmentE() != null) {
            devopsConfigMapRepDTO.setEnvId(devopsConfigMapE.getDevopsEnvironmentE().getId());
        }
        return devopsConfigMapRepDTO;
    }
}
