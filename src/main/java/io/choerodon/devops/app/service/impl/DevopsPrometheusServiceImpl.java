package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsPrometheusService;
import io.choerodon.devops.infra.dto.DevopsPrometheusDTO;
import io.choerodon.devops.infra.mapper.DevopsPrometheusMapper;

/**
 * @author: 25499
 * @date: 2019/10/28 15:53
 * @description:
 */
@Service
public class DevopsPrometheusServiceImpl implements DevopsPrometheusService {
    @Autowired
    private DevopsPrometheusMapper devopsPrometheusMapper;
    @Override
    public void create(DevopsPrometheusDTO devopsPrometheusDTO) {
        devopsPrometheusMapper.insertSelective(devopsPrometheusDTO);
    }
}
