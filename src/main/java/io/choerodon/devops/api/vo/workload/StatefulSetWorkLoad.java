package io.choerodon.devops.api.vo.workload;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.DevopsStatefulSetService;
import io.choerodon.devops.app.service.WorkloadService;
import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;
import io.choerodon.devops.infra.dto.DevopsStatefulSetDTO;

/**
 * Created by wangxiang on 2021/7/14
 */
@Component
public class StatefulSetWorkLoad extends WorkLoad {


    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private DevopsStatefulSetService devopsStatefulSetService;


    @Override
    public void updateWorkLoad(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId, Map<String, Object> extraInfo) {
        workloadService.updateStatefulSet(devopsEnvCommandDTO, newName, resourceId);
    }

    @Override
    public Long createWorkload(String name, Long projectId, Long envId, Long commandId, Map<String, Object> extraInfo) {
        DevopsStatefulSetDTO devopsStatefulSetDTO = new DevopsStatefulSetDTO(name, projectId, envId, commandId);
        return devopsStatefulSetService.baseCreate(devopsStatefulSetDTO);
    }

    @Override
    public void checkWorkloadExist(Long envId, String name) {
        devopsStatefulSetService.checkExist(envId, name);
    }

    @Override
    public Long getWorkloadId(Long envId, String workloadName) {
        DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetService.baseQueryByEnvIdAndName(envId, workloadName);
        if (devopsStatefulSetDTO == null) {
            throw new CommonException("error.workload.resource.not.exist", workloadName, "StatefulSet");
        }
        return devopsStatefulSetDTO.getId();
    }

    @Override
    public void updateWorkLoadCommandId(Long resourceId, Long commandId) {
        DevopsStatefulSetDTO devopsStatefulSetDTO = devopsStatefulSetService.selectByPrimaryKey(resourceId);
        devopsStatefulSetDTO.setCommandId(commandId);
        devopsStatefulSetService.baseUpdate(devopsStatefulSetDTO);
    }

    @Override
    public void deleteWorkload(Long resourceId) {
        devopsStatefulSetService.baseDelete(resourceId);
    }
}
