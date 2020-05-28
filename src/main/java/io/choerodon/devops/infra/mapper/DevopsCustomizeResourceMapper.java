package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsCustomizeResourceDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by Sheep on 2019/6/26.
 */
public interface DevopsCustomizeResourceMapper extends BaseMapper<DevopsCustomizeResourceDTO> {
    DevopsCustomizeResourceDTO queryDetail(@Param(value = "resourceId") Long resourceId);

    List<DevopsCustomizeResourceDTO> pageResources(
            @Param(value = "envId") Long envId,
            @Param(value = "searchParam") Map<String, Object> searchParam,
            @Param("params") List<String> params);

}
