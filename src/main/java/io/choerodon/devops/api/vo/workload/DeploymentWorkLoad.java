package io.choerodon.devops.api.vo.workload;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsDeploymentService;
import io.choerodon.devops.app.service.WorkloadService;
import io.choerodon.devops.infra.dto.DevopsDeploymentDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;

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
    public void updateWorkLoad(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId) {
        workloadService.updateDeployment(devopsEnvCommandDTO, newName, resourceId);
    }

    @Override
    public Long createWorkload(String name, Long projectId, Long envId, Long commandId) {
        DevopsDeploymentDTO devopsDeploymentDTO = new DevopsDeploymentDTO(name, projectId, envId, commandId);
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
