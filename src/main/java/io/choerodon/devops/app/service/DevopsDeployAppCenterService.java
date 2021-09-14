package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
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
     * @param projectId
     * @param name
     * @return
     */
    Boolean checkNameUnique(Long projectId, String rdupmType, Long objectId, String name);

    /**
     * 校验名称环境下唯一,不唯一抛出异常
     *
     * @param projectId
     * @param name
     * @return
     */
    void checkNameUniqueAndThrow(Long projectId, String rdupmType, Long objectId, String name);

    /**
     * 校验code环境下唯一
     *
     * @param projectId
     * @param code
     * @return
     */
    Boolean checkCodeUnique(Long projectId, String rdupmType, Long objectId, String code);

    void checkNameAndCodeUniqueAndThrow(Long projectId, String rdupmType, Long objectId, String name, String code);

    /**
     * 根据环境id分页查询所有应用，不传环境id表示查出所有有权限环境下的应用
     *
     * @param envId 环境id
     * @return 应用服务列表
     */
    Page<DevopsDeployAppCenterVO> listApp(Long projectId, Long envId, String name, String rdupmType, String operationType, String params, PageRequest pageable);

    AppCenterEnvDetailVO envAppDetail(Long projectId, Long appCenterId);

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

    void fixData();


    DevopsDeployAppCenterEnvDTO selectByPrimaryKey(Long id);


    /**
     * 根据项目id和环境id查询deployment的应用列表
     * @param projectId
     * @param envId
     * @param pageRequest
     * @return DevopsDeployAppCenterVO集合
     */
    Page<DevopsDeployAppCenterVO> pageByProjectIdAndEnvId(Long projectId, Long envId, PageRequest pageRequest);

    /**
     * 根据项目id,环境id和应用服务id查询chart的应用列表
     * @param projectId
     * @param envId
     * @param appServiceId
     * @param pageRequest
     * @return DevopsDeployAppCenterVO集合
     */
    Page<DevopsDeployAppCenterVO> pageByProjectIdAndEnvIdAndAppId(Long projectId, Long envId, Long appServiceId, PageRequest pageRequest);

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
}
