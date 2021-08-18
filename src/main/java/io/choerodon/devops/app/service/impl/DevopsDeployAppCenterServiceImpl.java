package io.choerodon.devops.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsDeployAppCenterVO;
import io.choerodon.devops.app.service.DevopsDeployAppCenterService;
import io.choerodon.devops.infra.enums.DevopsDeployAppCenterTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterEnvMapper;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterHostMapper;
import io.choerodon.devops.infra.util.UserDTOFillUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-18 15:28
 **/
@Service
public class DevopsDeployAppCenterServiceImpl implements DevopsDeployAppCenterService {

    @Autowired
    DevopsDeployAppCenterEnvMapper devopsDeployAppCenterEnvMapper;
    @Autowired
    DevopsDeployAppCenterHostMapper devopsDeployAppCenterHostMapper;

    @Override
    public Page<DevopsDeployAppCenterVO> listApp(Long projectId, Long envId, Long hostId, String name, String rdupmType, String pattern, PageRequest pageable) {
        Page<DevopsDeployAppCenterVO> devopsDeployAppCenterVOS = PageHelper.doPageAndSort(pageable, () ->
                pattern.equals(DevopsDeployAppCenterTypeEnum.ENV.getValue()) ? listAppFromEnv(projectId, envId, name, rdupmType) : listAppFromHost(projectId, hostId, name, rdupmType));
        UserDTOFillUtil.fillUserInfo(devopsDeployAppCenterVOS.getContent(), "createdBy", "creatorInfo");
        return devopsDeployAppCenterVOS;
    }

    private List<DevopsDeployAppCenterVO> listAppFromEnv(Long projectId, Long envId, String name, String rdupmType) {
        return devopsDeployAppCenterEnvMapper.listAppFromEnv(projectId, envId, name, rdupmType);
    }

    private List<DevopsDeployAppCenterVO> listAppFromHost(Long projectId, Long hostId, String name, String rdupmType) {
        return devopsDeployAppCenterHostMapper.listAppFromHost(projectId, hostId, name, rdupmType);
    }

}
