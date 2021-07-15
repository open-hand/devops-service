package io.choerodon.devops.api.vo.workload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.app.service.*;

/**
 * Created by wangxiang on 2021/7/14
 */
@Component
public class WorkLoadFactoryImpl implements WorkLoadFactory {

    @Autowired
    private WorkloadService workloadService;
    @Autowired
    private DevopsCronJobService devopsCronJobService;
    @Autowired
    private DevopsDaemonSetService devopsDaemonSetService;
    @Autowired
    private DevopsDeploymentService devopsDeploymentService;
    @Autowired
    private DevopsStatefulSetService devopsStatefulSetService;
    @Autowired
    private DevopsJobService devopsJobService;

    @Override
    public WorkLoad getWorkLoad(String resourceType) {
        switch (resourceType) {
            case "Deployment":
                return new WorkLoadDeployment(workloadService, devopsDeploymentService);
            case "StatefulSet":
                return new WorkLoadStatefulSet(workloadService, devopsStatefulSetService);
            case "Job":
                return new WorkLoadJob(workloadService, devopsJobService);
            case "DaemonSet":
                return new WorkLoadDaemonSet(workloadService, devopsDaemonSetService);
            case "CronJob":
                return new WorkLoadCronJob(workloadService, devopsCronJobService);
            default:
                throw new CommonException("error.workload.resource.not.supported", resourceType);
        }
    }
}
