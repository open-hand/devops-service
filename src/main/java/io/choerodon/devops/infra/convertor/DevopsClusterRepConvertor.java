package io.choerodon.devops.infra.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.DevopsClusterRepVO;
import io.choerodon.devops.api.vo.iam.entity.DevopsClusterE;

@Component
public class DevopsClusterRepConvertor implements ConvertorI<DevopsClusterE, Object, DevopsClusterRepVO> {

    @Override
    public DevopsClusterRepVO entityToDto(DevopsClusterE devopsClusterE) {
        DevopsClusterRepVO devopsClusterRepVO = new DevopsClusterRepVO();
        BeanUtils.copyProperties(devopsClusterE, devopsClusterRepVO);
        return devopsClusterRepVO;
    }

}
