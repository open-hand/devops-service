package io.choerodon.devops.api.vo.workload;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsCronJobService;
import io.choerodon.devops.app.service.WorkloadService;
import io.choerodon.devops.infra.dto.DevopsCronJobDTO;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;

/**
 * Created by wangxiang on 2021/7/14
 */
@Component
public class CronJobWorkLoad extends WorkLoad {


    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private DevopsCronJobService devopsCronJobService;



    @Override
    public Long createWorkload(String name, Long projectId, Long envId, Long commandId) {
        DevopsCronJobDTO devopsCronJobDTO = new DevopsCronJobDTO(name, projectId, envId, commandId);
        return devopsCronJobService.baseCreate(devopsCronJobDTO);
    }

    @Override
    public void updateWorkLoad(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId) {
        workloadService.updateCronJob(devopsEnvCommandDTO, newName, resourceId);
    }

    @Override
    public void checkWorkloadExist(Long envId, String name) {
        devopsCronJobService.checkExist(envId, name);
    }

    @Override
    public Long getWorkloadId(Long envId, String workloadName) {
        DevopsCronJobDTO devopsCronJobDTO = devopsCronJobService.baseQueryByEnvIdAndName(envId, workloadName);
        if (devopsCronJobDTO == null) {
            throw new CommonException("error.workload.resource.not.exist", workloadName, "CronJob");
        }
        return devopsCronJobDTO.getId();
    }

    @Override
    public void updateWorkLoadCommandId(Long resourceId, Long commandId) {
        DevopsCronJobDTO devopsCronJobDTO = devopsCronJobService.selectByPrimaryKey(resourceId);
        devopsCronJobDTO.setCommandId(commandId);
        devopsCronJobService.baseUpdate(devopsCronJobDTO);
    }

    @Override
    public void deleteWorkload(Long resourceId) {
        devopsCronJobService.baseDelete(resourceId);
    }
}
