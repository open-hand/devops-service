package io.choerodon.devops.api.vo.workload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsDaemonSetService;
import io.choerodon.devops.app.service.WorkloadService;
import io.choerodon.devops.infra.dto.DevopsDaemonSetDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;


/**
 * Created by wangxiang on 2021/7/14
 */

public class WorkLoadDaemonSet extends WorkLoad {

    public WorkLoadDaemonSet(WorkloadService workloadService, DevopsDaemonSetService devopsDaemonSetService) {
        this.workloadService = workloadService;
        this.devopsDaemonSetService = devopsDaemonSetService;
    }

    private WorkloadService workloadService;


    private DevopsDaemonSetService devopsDaemonSetService;

    @Override
    public Long createWorkload(String name, Long projectId, Long envId, Long commandId) {
        DevopsDaemonSetDTO devopsDaemonSetDTO = new DevopsDaemonSetDTO(name, projectId, envId, commandId);
        return devopsDaemonSetService.baseCreate(devopsDaemonSetDTO);
    }

    @Override
    public void updateWorkLoad(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId) {
        workloadService.updateDaemonSet(devopsEnvCommandDTO, newName, resourceId);
    }

    @Override
    public void checkWorkloadExist(Long envId, String name) {
        devopsDaemonSetService.checkExist(envId, name);
    }

    @Override
    public Long getWorkloadId(Long envId, String workloadName) {
        DevopsDaemonSetDTO devopsDaemonSetDTO = devopsDaemonSetService.baseQueryByEnvIdAndName(envId, workloadName);
        if (devopsDaemonSetDTO == null) {
            throw new CommonException("error.workload.resource.not.exist", workloadName, "DaemonSet");
        }
        return devopsDaemonSetDTO.getId();
    }

    @Override
    public void updateWorkLoadCommandId(Long resourceId, Long commandId) {
        DevopsDaemonSetDTO devopsDaemonSetDTO = devopsDaemonSetService.selectByPrimaryKey(resourceId);
        devopsDaemonSetDTO.setCommandId(commandId);
        devopsDaemonSetService.baseUpdate(devopsDaemonSetDTO);
    }

    @Override
    public void deleteWorkload(Long resourceId) {
        devopsDaemonSetService.baseDelete(resourceId);
    }
}
