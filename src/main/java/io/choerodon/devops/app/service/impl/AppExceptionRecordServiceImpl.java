package io.choerodon.devops.app.service.impl;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1beta2Deployment;
import io.kubernetes.client.models.V1beta2StatefulSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.AppExceptionRecordService;
import io.choerodon.devops.app.service.DevopsDeployAppCenterService;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.mapper.AppExceptionRecordMapper;

/**
 * chart应用异常信息记录表(AppExceptionRecord)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-05-10 11:17:14
 */
@Service
public class AppExceptionRecordServiceImpl implements AppExceptionRecordService {
    @Autowired
    private AppExceptionRecordMapper appExceptionRecordMapper;
    private static JSON json = new JSON();
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;

    @Override
    @Transactional
    public void createOrUpdateExceptionRecord(String resourceType, String resource, AppServiceInstanceDTO appServiceInstanceDTO) {
        // 保存应用异常信息
        DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, appServiceInstanceDTO.getId());
        if (devopsDeployAppCenterEnvDTO == null || Boolean.FALSE.equals(devopsDeployAppCenterEnvDTO.getMetricDeployStatus())) {
            return;
        }
        Integer current = 1;
        Integer desired = 1;
        if (ResourceType.DEPLOYMENT.getType().equals(resourceType)) {
            V1beta2Deployment v1beta2Deployment = json.deserialize(resource, V1beta2Deployment.class);
            current = v1beta2Deployment.getStatus().getReadyReplicas();
            desired = v1beta2Deployment.getStatus().getReplicas();
        } else if (ResourceType.STATEFULSET.getType().equals(resourceType)) {
            V1beta2StatefulSet v1beta2StatefulSet = json.deserialize(resource, V1beta2StatefulSet.class);
            current = v1beta2StatefulSet.getStatus().getReadyReplicas();
            desired = v1beta2StatefulSet.getStatus().getReplicas();
        } else {
            return;
        }
        // current == 0 停机
        // current < desired 异常
        if (current == 0) {
            // 1. 查询现在是否存在异常记录

        }


    }
}

