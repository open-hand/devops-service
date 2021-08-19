package io.choerodon.devops.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsDeployAppCenterVO;
import io.choerodon.devops.app.service.DevopsDeployAppCenterService;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.enums.DevopsDeployAppCenterTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterEnvMapper;
import io.choerodon.devops.infra.mapper.DevopsDeployAppCenterHostMapper;
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper;
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
    @Autowired
    DevopsEnvironmentMapper devopsEnvironmentMapper;

    @Override
    public Page<DevopsDeployAppCenterVO> listApp(Long projectId, Long envId, Long hostId, String name, String rdupmType, String pattern, PageRequest pageable) {
        Page<DevopsDeployAppCenterVO> devopsDeployAppCenterVOS = PageHelper.doPageAndSort(pageable, () ->
                pattern.equals(DevopsDeployAppCenterTypeEnum.ENV.getValue()) ? listAppFromEnv(projectId, envId, name, rdupmType) : listAppFromHost(projectId, hostId, name, rdupmType));
        UserDTOFillUtil.fillUserInfo(devopsDeployAppCenterVOS.getContent(), "createdBy", "iamUserDTO");
        return devopsDeployAppCenterVOS;
    }

    private List<DevopsDeployAppCenterVO> listAppFromEnv(Long projectId, Long envId, String name, String rdupmType) {
        List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList = devopsDeployAppCenterEnvMapper.listAppFromEnv(projectId, envId, name, rdupmType);
         devopsDeployAppCenterVOList.forEach(devopsDeployAppCenterVO -> {
            DevopsEnvironmentDTO devopsEnvAppServiceDTO = new DevopsEnvironmentDTO();
            devopsEnvAppServiceDTO.setId(devopsDeployAppCenterVO.getEnvId());
            devopsDeployAppCenterVO.setEnvName(devopsEnvironmentMapper.selectByPrimaryKey(devopsEnvAppServiceDTO).getName());
        });
        return devopsDeployAppCenterVOList;
    }

    private List<DevopsDeployAppCenterVO> listAppFromHost(Long projectId, Long hostId, String name, String rdupmType) {
        return devopsDeployAppCenterHostMapper.listAppFromHost(projectId, hostId, name, rdupmType);
    }

}
