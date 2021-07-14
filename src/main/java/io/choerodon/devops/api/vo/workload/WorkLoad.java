package io.choerodon.devops.api.vo.workload;

import io.choerodon.devops.infra.dto.DevopsEnvCommandDTO;

/**
 * Created by wangxiang on 2021/7/14
 */
public abstract class WorkLoad {

    public abstract void updateWorkLoad(DevopsEnvCommandDTO devopsEnvCommandDTO, String newName, Long resourceId);

    public abstract Long createWorkload(String name, Long projectId, Long envId, Long commandId);

    public abstract void checkWorkloadExist(Long envId, String name);

    public abstract Long getWorkloadId(Long envId, String workloadName);

    public abstract void updateWorkLoadCommandId(Long resourceId, Long commandId);

    public abstract void deleteWorkload(Long resourceId);
}
