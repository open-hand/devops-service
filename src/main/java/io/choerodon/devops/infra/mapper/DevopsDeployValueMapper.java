package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsDeployValueDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:33 2019/4/10
 * Description:
 */
public interface DevopsDeployValueMapper extends Mapper<DevopsDeployValueDTO> {
    List<DevopsDeployValueDTO> listByOptions(@Param("projectId") Long projectId,
                                             @Param("appId") Long appId,
                                             @Param("envId") Long envId,
                                             @Param("userId") Long userId,
                                             @Param("searchParam") Map<String, Object> searchParam,
                                             @Param("param") String param);
}
