package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.choerodon.devops.api.vo.pipeline.DevopsDeployInfoVO;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.DevopsCdEnvDeployInfoDTO;
import io.choerodon.devops.infra.dto.DevopsCdJobDTO;
import io.choerodon.devops.infra.dto.DevopsCheckLogDTO;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
import io.choerodon.devops.infra.enums.JobTypeEnum;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.mapper.DevopsCheckLogMapper;
import io.choerodon.devops.infra.util.JsonHelper;


@Service
public class DevopsCheckLogServiceImpl implements DevopsCheckLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsCheckLogServiceImpl.class);

    public static final String FIX_ENV_DATA = "fixEnvAppData";
    public static final String FIX_APP_CENTER_DATA = "fixAppCenterData";
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Autowired
    private DevopsCheckLogMapper devopsCheckLogMapper;
    @Autowired
    private DevopsEnvApplicationService devopsEnvApplicationService;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;
    @Autowired
    private DevopsCdJobService devopsCdJobService;
    @Autowired
    private DevopsCdEnvDeployInfoService devopsCdEnvDeployInfoService;


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
            case FIX_APP_CENTER_DATA:
                devopsDeployAppCenterService.fixData();
                fixPipelineCdDeployData();
                break;
            default:
                LOGGER.info("version not matched");
                return;
        }
        devopsCheckLogDTO.setLog(task);
        devopsCheckLogDTO.setEndCheckDate(new Date());
        devopsCheckLogMapper.insert(devopsCheckLogDTO);
    }

    private void fixPipelineCdDeployData() {
        List<DevopsCdJobDTO> devopsCdJobDTOS = devopsCdJobService.listByType(JobTypeEnum.CD_DEPLOY);
        LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Start fix pipeline cdDeploy task! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        Set<Long> errorJobIds = new HashSet<>();
        for (DevopsCdJobDTO devopsCdJobDTO : devopsCdJobDTOS) {
            try {
                Long deployInfoId = devopsCdJobDTO.getDeployInfoId();
                if (deployInfoId != null) {
                    DevopsCdEnvDeployInfoDTO devopsCdEnvDeployInfoDTO = devopsCdEnvDeployInfoService.queryById(deployInfoId);
                    if (devopsCdEnvDeployInfoDTO != null) {

                        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = null;
                        // 实例id不为空就通过实例id查询应用
                        if (devopsCdEnvDeployInfoDTO.getInstanceId() != null) {
                            devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, devopsCdEnvDeployInfoDTO.getInstanceId());
                        } else {
                            // 实例id为空就通过环境id和实例名称查询应用
                            if (devopsCdEnvDeployInfoDTO.getEnvId() != null
                                    && devopsCdEnvDeployInfoDTO.getInstanceName() != null) {
                                devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByEnvIdAndCode(devopsCdEnvDeployInfoDTO.getEnvId(), devopsCdEnvDeployInfoDTO.getInstanceName());
                            }
                        }
                        DevopsDeployInfoVO devopsDeployInfoVO = new DevopsDeployInfoVO();
                        devopsDeployInfoVO.setAppCode(devopsCdEnvDeployInfoDTO.getInstanceName());
                        devopsDeployInfoVO.setAppName(devopsCdEnvDeployInfoDTO.getInstanceName());
                        devopsDeployInfoVO.setEnvId(devopsCdEnvDeployInfoDTO.getEnvId());
                        devopsDeployInfoVO.setValueId(devopsCdEnvDeployInfoDTO.getValueId());
                        devopsDeployInfoVO.setSkipCheckPermission(!devopsCdEnvDeployInfoDTO.getCheckEnvPermissionFlag());
                        // 找到了关联的应用，设置关联应用id，流水线执行时走更新实例逻辑
                        if (devopsDeployAppCenterEnvDTO != null) {
                            devopsCdJobDTO.setAppId(devopsDeployAppCenterEnvDTO.getId());
                        }
                        devopsCdJobDTO.setMetadata(JsonHelper.marshalByJackson(devopsDeployInfoVO));
                        devopsCdJobService.baseUpdate(devopsCdJobDTO);
                    }
                }
            } catch (Exception e) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>Fix pipeline cdDeploy task : {} failed! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", devopsCdJobDTO.getId());
                errorJobIds.add(devopsCdJobDTO.getId());
            }

        }
        if (CollectionUtils.isEmpty(errorJobIds)) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline cdDeploy task! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End fix pipeline cdDeploy task, but exist errors! Failed job ids is : {}<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<", JsonHelper.marshalByJackson(errorJobIds));
            }
        }


    }
}
