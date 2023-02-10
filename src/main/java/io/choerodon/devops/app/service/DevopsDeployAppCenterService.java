package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.AppServiceInstanceInfoDTO;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.enums.deploy.RdupmTypeEnum;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-18 15:27
 **/
public interface DevopsDeployAppCenterService {
    /**
     * 校验名称环境下唯一
     *
     * @param envId
     * @param name
     * @return
     */
    Boolean checkNameUnique(Long envId, String rdupmType, Long objectId, String name);

    /**
     * 校验code环境下唯一
     *
     * @param envId
     * @param code
     * @return
     */
    Boolean checkCodeUnique(Long envId, String rdupmType, Long objectId, String code);

    /**
     * 校验名称环境下唯一,并抛出异常
     *
     * @param envId
     * @param name
     * @return
     */
    void checkNameUniqueAndThrow(Long envId, String rdupmType, Long objectId, String name);

    /**
     * 校验code环境下唯一,并抛出异常
     *
     * @param envId
     * @param name
     * @return
     */
    void checkCodeUniqueAndThrow(Long envId, String rdupmType, Long objectId, String name);

    void checkNameAndCodeUniqueAndThrow(Long projectId, String rdupmType, Long objectId, String name, String code);

    /**
     * 根据环境id分页查询所有应用，不传环境id表示查出所有有权限环境下的应用
     *
     * @param envId 环境id
     * @return 应用服务列表
     */
    Page<DevopsDeployAppCenterVO> listApp(Long projectId, Long envId, String name, String rdupmType, String operationType, String params, PageRequest pageable);

    AppCenterEnvDetailVO envAppDetail(Long projectId, Long appCenterId);

    void calculatePodStatus(List<DevopsEnvPodDTO> devopsEnvPodDTOS, AppCenterEnvDetailVO detailVO);

    List<InstanceEventVO> envAppEvent(Long projectId, Long appCenterId);

    Page<DevopsEnvPodVO> envAppPodsPage(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam);

    DevopsEnvResourceVO envAppRelease(Long projectId, Long appCenterId);

    Page<DevopsServiceVO> envChartService(Long projectId, Long appCenterId, PageRequest pageRequest, String searchParam);

    /**
     * 创建应用
     *
     * @param devopsDeployAppCenterEnvDTO
     */
    void baseCreate(DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO);

    /**
     * 更新应用
     *
     * @param devopsDeployAppCenterEnvDTO
     */
    void baseUpdate(DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO);

    DevopsDeployAppCenterEnvDTO baseCreate(String name, String code, Long projectId, Long objectId, Long envId, String operationType, String chartSource, String rdupmType);

    DevopsDeployAppCenterEnvDTO queryByEnvIdAndCode(Long environmentId, String appCode);

    DevopsDeployAppCenterEnvDTO queryByRdupmTypeAndObjectId(RdupmTypeEnum rdupmTypeEnum, Long objectId);

    /**
     * 根据环境id、关联对象类型、关联对象id删除记录
     *
     * @param envId
     * @param objectId
     * @param rdupmType
     */
    void deleteByEnvIdAndObjectIdAndRdupmType(Long envId, Long objectId, String rdupmType);


    DevopsDeployAppCenterEnvDTO selectByPrimaryKey(Long id);


    /**
     * 根据项目id和环境id查询deployment的应用列表
     *
     * @param projectId
     * @param envId
     * @param pageRequest
     * @return DevopsDeployAppCenterVO集合
     */
    Page<DevopsDeployAppCenterVO> pageByProjectIdAndEnvId(Long projectId, Long envId, PageRequest pageRequest);

    /**
     * 根据项目id,环境id和应用服务id查询chart的应用列表
     *
     * @param projectId
     * @param envId
     * @param appServiceId
     * @param pageRequest
     * @return DevopsDeployAppCenterVO集合
     */
    Page<DevopsDeployAppCenterVO> pageByProjectIdAndEnvIdAndAppId(Long projectId, Long envId, Long appServiceId, PageRequest pageRequest);

    void delete(DevopsDeployAppCenterEnvDTO devopsDeployAppCenterEnvDTO);

    void setAppInstanceInfoToAppCenter(Long projectId, DevopsDeployAppCenterVO devopsDeployAppCenterVO, AppServiceInstanceInfoDTO appServiceInstanceInfoDTO);

    Map<Long, DevopsEnvironmentDTO> combineDevopsEnvironmentDTOMap(List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList);

    Map<Long, AppServiceInstanceInfoDTO> devopsInstanceDTOMap(List<DevopsDeployAppCenterVO> devopsDeployAppCenterVOList);

    /**
     * @Description 批量插入DevopsDeployAppCenterEnvDTO
     * @Param devopsDeployAppCenterEnvDTOList
     * @Return Integer
     */
    Integer batchInsert(List<DevopsDeployAppCenterEnvDTO> devopsDeployAppCenterEnvDTOList);

    /**
     * @Description 查询环境下的Chart应用
     * @Param projectId
     * @Param envId
     * @Param name
     * @Param operationType
     * @Param params
     * @Return DevopsDeployAppCenterVO集合
     */
    Page<DevopsDeployAppCenterVO> pageChart(Long projectId, Long envId, String name, String operationType, String params, PageRequest pageable);

    List<PipelineInstanceReferenceVO> queryPipelineReference(Long projectId, Long appId);

    void checkEnableDeleteAndThrowE(Long projectId, RdupmTypeEnum rdupmTypeEnum, Long instanceId);

    List<DevopsDeployAppCenterVO> listByAppServiceIds(Long envId, Set<Long> appServiceIds);

    /**
     * 启用chart应用监控
     *
     * @param projectId 项目id
     * @param appId     应用id
     */
    void enableMetric(Long projectId, Long appId);

    /**
     * 关闭chart应用监控
     *
     * @param projectId 项目id
     * @param appId     应用id
     */
    void disableMetric(Long projectId, Long appId);

    /**
     * @param projectId 项目id
     * @param appId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return ExceptionTimesVO 停机次数折线图所需坐标信息
     */
    ExceptionTimesVO queryExceptionTimesChartInfo(Long projectId, Long appId, Date startTime, Date endTime);

    /**
     * @param projectId 项目id
     * @param appId     应用id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return ExceptionDurationVO 异常时长散点图所需坐标信息
     */
    ExceptionDurationVO queryExceptionDurationChartInfo(Long projectId, Long appId, Date startTime, Date endTime);
}
