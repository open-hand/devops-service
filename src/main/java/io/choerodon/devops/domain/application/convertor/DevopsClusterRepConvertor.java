package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsClusterRepDTO;
import io.choerodon.devops.api.vo.iam.entity.DevopsClusterE;

@Component
public class DevopsClusterRepConvertor implements ConvertorI<DevopsClusterE, Object, DevopsClusterRepDTO> {

    @Override
    public DevopsClusterRepDTO entityToDto(DevopsClusterE devopsClusterE) {
        DevopsClusterRepDTO devopsClusterRepDTO = new DevopsClusterRepDTO();
        BeanUtils.copyProperties(devopsClusterE, devopsClusterRepDTO);
        return devopsClusterRepDTO;
    }

}
