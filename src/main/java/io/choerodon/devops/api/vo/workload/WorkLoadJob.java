package io.choerodon.devops.api.vo.workload;

import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsJobService;
import io.choerodon.devops.app.service.WorkloadService;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsJobDTO;

/**
 * Created by wangxiang on 2021/7/14
 */
@Component
public class WorkLoadJob extends WorkLoad {


    private WorkloadService workloadService;

    private DevopsJobService devopsJobService;

    public WorkLoadJob(WorkloadService workloadService, DevopsJobService devopsJobService) {
        this.workloadService = workloadService;
        this.devopsJobService = devopsJobService;
    }


    @Override
    public Long createWorkload(String name, Long projectId, Long envId, Long commandId) {
        DevopsJobDTO devopsJobDTO = new DevopsJobDTO(name, projectId, envId, commandId);
        return devopsJobService.baseCreate(devopsJobDTO);
    }

    @Override
    public void updateWorkLoad(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId) {
        workloadService.updateJob(devopsEnvCommandDTO, newName, resourceId);
    }

    @Override
    public void checkWorkloadExist(Long envId, String name) {
        devopsJobService.checkExist(envId, name);
    }

    @Override
    public Long getWorkloadId(Long envId, String workloadName) {
        DevopsJobDTO devopsJobDTO = devopsJobService.baseQueryByEnvIdAndName(envId, workloadName);
        if (devopsJobDTO == null) {
            throw new CommonException("error.workload.resource.not.exist", workloadName, "Job");
        }
        return devopsJobDTO.getId();
    }

    @Override
    public void updateWorkLoadCommandId(Long resourceId, Long commandId) {
        DevopsJobDTO devopsJobDTO = devopsJobService.selectByPrimaryKey(resourceId);
        devopsJobDTO.setCommandId(commandId);
        devopsJobService.baseUpdate(devopsJobDTO);
    }

    @Override
    public void deleteWorkload(Long resourceId) {
        devopsJobService.baseDelete(resourceId);
    }
}
