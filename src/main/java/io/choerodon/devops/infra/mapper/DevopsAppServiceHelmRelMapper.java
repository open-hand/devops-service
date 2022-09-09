package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsAppServiceHelmRelDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface DevopsAppServiceHelmRelMapper extends BaseMapper<DevopsAppServiceHelmRelDTO> {
    DevopsAppServiceHelmRelDTO queryByAppServiceId(@Param("appServiceId") Long appServiceId);
}
