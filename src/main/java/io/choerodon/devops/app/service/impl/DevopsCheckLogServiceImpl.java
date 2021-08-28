package io.choerodon.devops.app.service.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.DevopsCheckLogService;
import io.choerodon.devops.app.service.DevopsDeployAppCenterService;
import io.choerodon.devops.app.service.DevopsEnvApplicationService;
import io.choerodon.devops.infra.dto.DevopsCheckLogDTO;
import io.choerodon.devops.infra.mapper.DevopsCheckLogMapper;


@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);

    public static final String FIX_ENV_DATA = "fixEnvAppData";
    public static final String FIX_APP_CENTER_DATA = "fixAppCenterData";

    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;
    @Autowired
    private DevopsEnvApplicationService devopsEnvApplicationService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;

    @Override
    public void checkLog(String task) {
        DevopsCheckLogDTO devopsCheckLogDTO = new DevopsCheckLogDTO();
        devopsCheckLogDTO.setLog(task);
        DevopsCheckLogDTO existDevopsCheckLogDTO = devopsCheckLogMapper.selectOne(devopsCheckLogDTO);
        if (existDevopsCheckLogDTO != null) {
            LOGGER.info("fix data task {} has already been executed", task);
            return;
        }
        devopsCheckLogDTO.setBeginCheckDate(new Date());
        switch (task) {
            case FIX_ENV_DATA:
                devopsEnvApplicationService.fixData();
                break;
            case FIX_APP_CENTER_DATA:
                devopsDeployAppCenterService.fixData();
                break;
            default:
                LOGGER.info("version not matched");
                return;
        }
        devopsCheckLogDTO.setLog(task);
        devopsCheckLogDTO.setEndCheckDate(new Date());
        devopsCheckLogMapper.insert(devopsCheckLogDTO);
    }
}
