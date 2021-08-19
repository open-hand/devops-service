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
}
