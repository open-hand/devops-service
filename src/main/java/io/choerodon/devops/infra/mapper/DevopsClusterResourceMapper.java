package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsClusterResourceDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author zhaotianxin
 * @since 2019/10/29
 */
public interface DevopsClusterResourceMapper extends BaseMapper<DevopsClusterResourceDTO> {

    DevopsClusterResourceDTO queryByClusterIdAndType(@Param("clusterId") Long clusterId, @Param("type") String type);

    DevopsClusterResourceDTO queryPrometheusByClusterId(@Param("clusterId") Long clusterId);
}
