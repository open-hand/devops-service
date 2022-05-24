package io.choerodon.devops.app.service.impl;

import java.util.Date;
import java.util.List;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1beta2Deployment;
import io.kubernetes.client.models.V1beta2StatefulSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.AppExceptionRecordService;
import io.choerodon.devops.app.service.DevopsDeployAppCenterService;
import io.choerodon.devops.infra.dto.AppExceptionRecordDTO;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.devops.infra.mapper.AppExceptionRecordMapper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * chart应用异常信息记录表(AppExceptionRecord)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-05-10 11:17:14
 */
@Service
public class AppExceptionRecordServiceImpl implements AppExceptionRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppExceptionRecordServiceImpl.class);
    private static JSON json = new JSON();
    @Autowired
    private AppExceptionRecordMapper appExceptionRecordMapper;
    @Autowired
    private DevopsDeployAppCenterService devopsDeployAppCenterService;

    @Override
    @Transactional
    public void createOrUpdateExceptionRecord(String resourceType, String resource, AppServiceInstanceDTO appServiceInstanceDTO) {
        try {
            // 保存应用异常信息
            DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO = devopsDeployAppCenterService.queryByRdupmTypeAndObjectId(RdupmTypeEnum.CHART, appServiceInstanceDTO.getId());
            if (devopsDeployAppCenterEnvDTO == null || Boolean.FALSE.equals(devopsDeployAppCenterEnvDTO.getMetricDeployStatus())) {
                return;
            }
            int current = 1;
            int desired = 1;
            String resourceName = "";
            if (ResourceType.DEPLOYMENT.getType().equals(resourceType)) {
                V1beta2Deployment v1beta2Deployment = json.deserialize(resource, V1beta2Deployment.class);
                current = v1beta2Deployment.getStatus().getReadyReplicas() == null ? 0 : v1beta2Deployment.getStatus().getReadyReplicas();
                desired = v1beta2Deployment.getStatus().getReplicas() == null ? 0 : v1beta2Deployment.getStatus().getReplicas();
                resourceName = v1beta2Deployment.getMetadata().getName();
            } else if (ResourceType.STATEFULSET.getType().equals(resourceType)) {
                V1beta2StatefulSet v1beta2StatefulSet = json.deserialize(resource, V1beta2StatefulSet.class);
                current = v1beta2StatefulSet.getStatus().getReadyReplicas() == null ? 0 : v1beta2StatefulSet.getStatus().getReadyReplicas();
                desired = v1beta2StatefulSet.getStatus().getReplicas() == null ? 0 : v1beta2StatefulSet.getStatus().getReplicas();
                resourceName = v1beta2StatefulSet.getMetadata().getName();
            } else {
                return;
            }
            // current = desired 正常
            // current == 0 停机
            // current < desired 异常
            if (current == desired) {
                // 1. 查询现在是否存在异常记录,存在则将该记录标记为结束
                AppExceptionRecordDTO appExceptionRecordDTO = appExceptionRecordMapper.queryLatestExceptionRecord(devopsDeployAppCenterEnvDTO.getId(), resourceType, resourceName);
                if (appExceptionRecordDTO != null) {
                    appExceptionRecordDTO.setEndDate(new Date());
                    MapperUtil.resultJudgedUpdateByPrimaryKeySelective(appExceptionRecordMapper, appExceptionRecordDTO, "error.update.exception.record");
                }
            } else if (current == 0) {
                // 停机
                // 1. 查询现在是否存在异常记录
                AppExceptionRecordDTO appExceptionRecordDTO = appExceptionRecordMapper.queryLatestExceptionRecord(devopsDeployAppCenterEnvDTO.getId(), resourceType, resourceName);
                if (appExceptionRecordDTO == null) {
                    AppExceptionRecordDTO appExceptionRecordDTO1 = new AppExceptionRecordDTO(devopsDeployAppCenterEnvDTO.getProjectId(),
                            devopsDeployAppCenterEnvDTO.getId(),
                            devopsDeployAppCenterEnvDTO.getEnvId(),
                            resourceType,
                            resourceName,
                            new Date(),
                            true);
                    MapperUtil.resultJudgedInsertSelective(appExceptionRecordMapper, appExceptionRecordDTO1, "error.save.exception.record");
                } else {
                    if (Boolean.FALSE.equals(appExceptionRecordDTO.getDowntime())) {
                        appExceptionRecordDTO.setEndDate(new Date());
                        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(appExceptionRecordMapper, appExceptionRecordDTO, "error.update.exception.record");

                        AppExceptionRecordDTO appExceptionRecordDTO1 = new AppExceptionRecordDTO(devopsDeployAppCenterEnvDTO.getProjectId(),
                                devopsDeployAppCenterEnvDTO.getId(),
                                devopsDeployAppCenterEnvDTO.getEnvId(),
                                resourceType,
                                resourceName,
                                new Date(),
                                true);
                        MapperUtil.resultJudgedInsertSelective(appExceptionRecordMapper, appExceptionRecordDTO1, "error.save.exception.record");
                    }
                }
            } else {
                // 1. 查询现在是否存在异常记录
                AppExceptionRecordDTO appExceptionRecordDTO = appExceptionRecordMapper.queryLatestExceptionRecord(devopsDeployAppCenterEnvDTO.getId(), resourceType, resourceName);
                if (appExceptionRecordDTO == null) {
                    AppExceptionRecordDTO appExceptionRecordDTO1 = new AppExceptionRecordDTO(devopsDeployAppCenterEnvDTO.getProjectId(),
                            devopsDeployAppCenterEnvDTO.getId(),
                            devopsDeployAppCenterEnvDTO.getEnvId(),
                            resourceType,
                            resourceName,
                            new Date(),
                            false);
                    MapperUtil.resultJudgedInsertSelective(appExceptionRecordMapper, appExceptionRecordDTO1, "error.save.exception.record");
                } else {
                    if (Boolean.TRUE.equals(appExceptionRecordDTO.getDowntime())) {
                        appExceptionRecordDTO.setEndDate(new Date());
                        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(appExceptionRecordMapper, appExceptionRecordDTO, "error.update.exception.record");

                        AppExceptionRecordDTO appExceptionRecordDTO1 = new AppExceptionRecordDTO(devopsDeployAppCenterEnvDTO.getProjectId(),
                                devopsDeployAppCenterEnvDTO.getId(),
                                devopsDeployAppCenterEnvDTO.getEnvId(),
                                resourceType,
                                resourceName,
                                new Date(),
                                false);
                        MapperUtil.resultJudgedInsertSelective(appExceptionRecordMapper, appExceptionRecordDTO1, "error.save.exception.record");

                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Collect app exception info failed. resourceType: {}, resource: {}, appServiceInstanceDTO: {}. error: {}", resourceType, resource, appServiceInstanceDTO, e);
        }


    }

    @Override
    public List<AppExceptionRecordDTO> listByAppIdAndDate(Long appId, Date startTime, Date endTime) {
        return appExceptionRecordMapper.listByAppIdAndDate(appId,
                new java.sql.Date(startTime.getTime()),
                new java.sql.Date(endTime.getTime()));
    }
}

