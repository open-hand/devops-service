package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.DevopsHostAppInstanceService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.DevopsHostAppInstanceDTO;
import io.choerodon.devops.infra.mapper.DevopsHostAppInstanceMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/9/3 15:51
 */
@Service
public class DevopsHostAppInstanceServiceImpl implements DevopsHostAppInstanceService {

    private static final String ERROR_SAVE_HOST_APP_INSTANCE_FAILED = "error.save.host.app.instance.failed";
    private static final String ERROR_UPDATE_HOST_APP_INSTANCE_FAILED = "error.update.host.app.instance.failed";

    @Autowired
    private DevopsHostAppInstanceMapper devopsHostAppInstanceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(DevopsHostAppInstanceDTO devopsHostAppInstanceDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsHostAppInstanceMapper, devopsHostAppInstanceDTO, ERROR_SAVE_HOST_APP_INSTANCE_FAILED);
    }

    @Override
    public List<DevopsHostAppInstanceDTO> listByAppId(Long appId) {
        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO();
        devopsHostAppInstanceDTO.setAppId(appId);
        return devopsHostAppInstanceMapper.select(devopsHostAppInstanceDTO);
    }

    @Override
    @Transactional
    public void baseUpdate(DevopsHostAppInstanceDTO devopsHostAppInstanceDTO) {
        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHostAppInstanceMapper, devopsHostAppInstanceDTO, ERROR_UPDATE_HOST_APP_INSTANCE_FAILED);

    }

    @Override
    public List<DevopsHostAppInstanceDTO> listByHostId(Long hostId) {
        Assert.notNull(hostId, ResourceCheckConstant.ERROR_HOST_ID_IS_NULL);
        DevopsHostAppInstanceDTO devopsHostAppInstanceDTO = new DevopsHostAppInstanceDTO();
        devopsHostAppInstanceDTO.setHostId(hostId);
        return devopsHostAppInstanceMapper.select(devopsHostAppInstanceDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseDelete(Long id) {
        devopsHostAppInstanceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public DevopsHostAppInstanceDTO baseQuery(Long id) {

        return devopsHostAppInstanceMapper.selectByPrimaryKey(id);
    }
}
