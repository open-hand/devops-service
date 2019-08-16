package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.DevopsEnvPodInfoVO;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Creator: Runge
 * Date: 2018/4/17
 * Time: 11:53
 * Description:
 */
public interface DevopsEnvPodMapper extends Mapper<DevopsEnvPodDTO> {

    List<DevopsEnvPodDTO> listAppServicePod(@Param("projectId") Long projectId,
                                            @Param("envId") Long envId,
                                            @Param("appServiceId") Long appServiceId,
                                            @Param("instanceId") Long instanceId,
                                            @Param("searchParam") Map<String, Object> searchParam,
                                            @Param("params") List<String> params);

    List<DevopsEnvPodInfoVO> queryEnvPodIns(@Param("envId") Long envId);

    DevopsEnvPodDTO queryPodByEnvIdAndInstanceId(@Param("instanceId") Long instanceId,
                                                 @Param("envId") Long envId);
}
