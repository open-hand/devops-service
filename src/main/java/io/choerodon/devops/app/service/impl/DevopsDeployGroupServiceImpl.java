package io.choerodon.devops.app.service.impl;

import io.choerodon.devops.api.vo.DevopsDeployGroupVO;
import io.choerodon.devops.app.service.DevopsDeployGroupService;
import io.choerodon.devops.infra.dto.DevopsDeployGroupDTO;
import io.choerodon.devops.infra.mapper.DevopsDeployGroupMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-19 18:46
 **/
@Service
public class DevopsDeployGroupServiceImpl implements DevopsDeployGroupService {

    @Autowired
    DevopsDeployGroupMapper devopsDeployGroupMapper;

    @Override
    public DevopsDeployGroupVO appConfigDetail(Long projectId, Long devopsConfigGroupId) {
        DevopsDeployGroupVO devopsDeployGroupVO = new DevopsDeployGroupVO();
        DevopsDeployGroupDTO devopsDeployGroupDTO = devopsDeployGroupMapper.queryById(projectId, devopsConfigGroupId);
        if (ObjectUtils.isEmpty(devopsDeployGroupDTO)) {
            return devopsDeployGroupVO;
        }
        BeanUtils.copyProperties(devopsDeployGroupDTO, devopsDeployGroupVO);
        return devopsDeployGroupVO;
    }
}
