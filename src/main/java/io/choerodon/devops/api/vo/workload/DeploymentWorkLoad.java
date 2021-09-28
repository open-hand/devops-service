package io.choerodon.devops.api.vo.workload;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsDeploymentService;
import io.choerodon.devops.app.service.WorkloadService;
import io.choerodon.devops.app.service.impl.DevopsDeploymentServiceImpl;
import io.choerodon.devops.infra.dto.DevopsDeploymentDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.enums.WorkloadSourceTypeEnums;
import io.choerodon.devops.infra.enums.InstanceStatus;

/**
 * Created by wangxiang on 2021/7/14
 */
@Component
public class DeploymentWorkLoad extends WorkLoad {


    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private DevopsDeploymentService devopsDeploymentService;

    @Override
    public void updateWorkLoad(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId, Map<String, Object> extraInfo) {
        workloadService.updateDeployment(devopsEnvCommandDTO, newName, resourceId, extraInfo);
    }

    @Override
    public Long createWorkload(String name, Long projectId, Long envId, Long commandId, Map<String, Object> extraInfo) {
        DevopsDeploymentDTO devopsDeploymentDTO = new DevopsDeploymentDTO(name, projectId, envId, commandId, (String) extraInfo.get(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_SOURCE_TYPE), InstanceStatus.OPERATING.getStatus());
        if (WorkloadSourceTypeEnums.DEPLOY_GROUP.getType().equals(extraInfo.get(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_SOURCE_TYPE))) {
            devopsDeploymentDTO.setAppConfig((String) extraInfo.get(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_APP_CONFIG));
            devopsDeploymentDTO.setContainerConfig((String) extraInfo.get(DevopsDeploymentServiceImpl.EXTRA_INFO_KEY_CONTAINER_CONFIG));
            devopsDeploymentDTO.setSourceType(WorkloadSourceTypeEnums.DEPLOY_GROUP.getType());
        } else {
            devopsDeploymentDTO.setSourceType(WorkloadSourceTypeEnums.WORKLOAD.getType());
        }
        return devopsDeploymentService.baseCreate(devopsDeploymentDTO);
    }

    @Override
    public void checkWorkloadExist(Long envId, String name) {
        devopsDeploymentService.checkExist(envId, name);
    }

    @Override
    public Long getWorkloadId(Long envId, String workloadName) {
        DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.baseQueryByEnvIdAndName(envId, workloadName);
        if (devopsDeploymentDTO == null) {
            throw new CommonException("error.workload.resource.not.exist", workloadName, "Deployment");
        }
        return devopsDeploymentDTO.getId();
    }

    @Override
    public void updateWorkLoadCommandId(Long resourceId, Long commandId) {
        DevopsDeploymentDTO devopsDeploymentDTO = devopsDeploymentService.selectByPrimaryKey(resourceId);
        devopsDeploymentDTO.setCommandId(commandId);
        devopsDeploymentService.baseUpdate(devopsDeploymentDTO);
    }

    @Override
    public void deleteWorkload(Long resourceId) {
        devopsDeploymentService.baseDelete(resourceId);
    }
}
