package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.infra.constant.DevopsHostConstants.ERROR_SAVE_APP_HOST_REL_FAILED;

import org.hzero.boot.admin.bus.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.DevopsHostAppInstanceRelService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsHostAppInstanceRelDTO;
import io.choerodon.devops.infra.mapper.DevopsHostAppInstanceRelMapper;
import io.choerodon.devops.infra.util.MapperUtil;

@Service
public class DevopsHostAppInstanceRelServiceImpl implements DevopsHostAppInstanceRelService {

    @Autowired
    private DevopsHostAppInstanceRelMapper devopsHostAppInstanceRelMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveHostAppInstanceRel(Long projectId, Long hostId, Long appServiceId, String appSource, Long instanceId, String instanceType) {
        Assert.notNull(projectId, ResourceCheckConstant.ERROR_PROJECT_ID_IS_NULL);
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        Assert.notNull(appServiceId, ResourceCheckConstant.ERROR_APP_SERVICE_ID_IS_NULL);
        Assert.notNull(appSource, ResourceCheckConstant.ERROR_SOURCE_TYPE_IS_NULL);
        Assert.notNull(instanceId, ResourceCheckConstant.ERROR_HOST_INSTANCE_ID_IS_NULL);
        Assert.notNull(instanceType, ResourceCheckConstant.ERROR_HOST_INSTANCE_TYPE_IS_NULL);

        DevopsHostAppInstanceRelDTO record = new DevopsHostAppInstanceRelDTO(appServiceId, instanceId);
        DevopsHostAppInstanceRelDTO devopsHostAppInstanceRelDTO = devopsHostAppInstanceRelMapper.selectOne(record);
        if (devopsHostAppInstanceRelDTO == null) {
            devopsHostAppInstanceRelDTO = new DevopsHostAppInstanceRelDTO(projectId,
                    hostId,
                    appServiceId,
                    appSource,
                    instanceId,
                    instanceType);
            MapperUtil.resultJudgedInsertSelective(devopsHostAppInstanceRelMapper, devopsHostAppInstanceRelDTO, ERROR_SAVE_APP_HOST_REL_FAILED);
        }
    }

    @Override
    @Transactional
    public void deleteByHostIdAndInstanceInfo(Long hostId, Long instanceId, String instanceType) {
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        Assert.notNull(instanceId, ResourceCheckConstant.ERROR_HOST_INSTANCE_ID_IS_NULL);
        Assert.notNull(instanceType, ResourceCheckConstant.ERROR_HOST_INSTANCE_TYPE_IS_NULL);

        DevopsHostAppInstanceRelDTO devopsHostAppInstanceRelDTO = new DevopsHostAppInstanceRelDTO();
        devopsHostAppInstanceRelDTO.setHostId(hostId);
        devopsHostAppInstanceRelDTO.setInstanceId(instanceId);
        devopsHostAppInstanceRelDTO.setInstanceType(instanceType);

        devopsHostAppInstanceRelMapper.delete(devopsHostAppInstanceRelDTO);
    }
}
