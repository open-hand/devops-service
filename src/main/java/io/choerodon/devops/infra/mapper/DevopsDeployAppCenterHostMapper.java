package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.api.vo.DevopsDeployAppCenterVO;
import io.choerodon.devops.infra.dto.DevopsDeployAppCenterHostDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: shanyu
 * @DateTime: 2021-08-18 17:06
 **/
public interface DevopsDeployAppCenterHostMapper extends BaseMapper<DevopsDeployAppCenterHostDTO> {

    List<DevopsDeployAppCenterVO> listAppFromHost(@Param("projectId") Long projectId,
                                                  @Param("hostId") Long hostId,
                                                  @Param("name") String name,
                                                  @Param("rdupmType") String rdupmType);
}
