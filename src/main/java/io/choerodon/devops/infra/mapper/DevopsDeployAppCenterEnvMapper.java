package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.DevopsDeployAppCenterVO;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterEnvDTO;
import io.choerodon.mybatis.common.BaseMapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-18 17:07
 **/
public interface DevopsDeployAppCenterEnvMapper extends BaseMapper<DevopsDeployAppCenterEnvDTO> {

    List<DevopsDeployAppCenterVO> listAppFromEnv(@Param("projectId") Long projectId,
                                                 @Param("envId") Long envId,
                                                 @Param("name") String name,
                                                 @Param("rdupmType") String rdupmType,
                                                 @Param("operationType") String operationType,
                                                 @Param("params") String params);

    DevopsDeployAppCenterEnvDTO queryByEnvIdAndCode(@Param("environmentId") Long environmentId, @Param("appCode") String appCode);

    void deleteByEnvIdAndObjectIdAndRdupmType(@Param("envId") Long envId, @Param("objectId") Long objectId, @Param("rdupmType") String rdupmType);

    List<DevopsDeployAppCenterVO> listAppFromEnvByUserId(@Param("projectId") Long projectId,
                                                 @Param("envId") Long envId,
                                                 @Param("name") String name,
                                                 @Param("rdupmType") String rdupmType,
                                                 @Param("operationType") String operationType,
                                                 @Param("params") String params,
                                                 @Param("userId") Long userId);

    Boolean checkNameUnique(@Param("rdupmType") String rdupmType,
                            @Param("objectId") Long objectId,
                            @Param("envId") Long envId,
                            @Param("name") String name);

    Boolean checkCodeUnique(@Param("rdupmType") String rdupmType,
                            @Param("objectId") Long objectId,
                            @Param("envId") Long envId,
                            @Param("code") String code);

    /**
     * 根据项目id和环境id查询deployment的应用列表
     * @param projectId
     * @param envId
     * @return DevopsDeployAppCenterVO集合
     */
    List<DevopsDeployAppCenterVO> listByProjectIdAndEnvId(@Param("projectId") Long projectId,
                                                          @Param("envId") Long envId);

    /**
     * 根据项目id,环境id和应用服务id查询chart的应用列表
     * @param projectId
     * @param envId
     * @param appServiceId
     * @return DevopsDeployAppCenterVO集合
     */
    List<DevopsDeployAppCenterVO> listByProjectIdAndEnvIdAndAppId(@Param("projectId") Long projectId,
                                                          @Param("envId") Long envId,
                                                          @Param("appServiceId") Long appServiceId);

    Integer batchInsert(@Param("devopsDeployAppCenterEnvDTOList") List<DevopsDeployAppCenterEnvDTO> devopsDeployAppCenterEnvDTOList);
                                                                  @Param("envId") Long envId,
                                                                  @Param("appServiceId") Long appServiceId);

    /**
     * @Description 查询环境下的Chart应用
     * @Param projectId
     * @Param envId
     * @Param name
     * @Param operationType
     * @Param params
     * @Return DevopsDeployAppCenterVO集合
     */
    List<DevopsDeployAppCenterVO> listChart(@Param("projectId") Long projectId,
                                            @Param("envId") Long envId,
                                            @Param("name") String name,
                                            @Param("operationType") String operationType,
                                            @Param("params") String params);

    /**
     * @Description 查询环境下的Chart应用
     * @Param projectId
     * @Param envId
     * @Param params
     * @Return DevopsDeployAppCenterVO集合
     */
    List<DevopsDeployAppCenterVO> listChartByUserId(@Param("projectId") Long projectId,
                                                         @Param("envId") Long envId,
                                                         @Param("name") String name,
                                                         @Param("operationType") String operationType,
                                                         @Param("params") String params,
                                                         @Param("userId") Long userId);
}
