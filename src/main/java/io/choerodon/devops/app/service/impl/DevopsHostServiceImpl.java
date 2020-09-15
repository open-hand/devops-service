package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsHostService;
import io.choerodon.devops.infra.mapper.DevopsHostMapper;

/**
 * @author zmf
 * @since 2020/9/15
 */
@Service
public class DevopsHostServiceImpl implements DevopsHostService {
    @Autowired
    private DevopsHostMapper devopsHostMapper;


    @Override
    public void deleteHost(Long projectId, Long hostId) {
        // TODO 校验可删除
        devopsHostMapper.deleteByPrimaryKey(hostId);
    }
}
