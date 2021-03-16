package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsMiddlewareDTO;
import io.choerodon.mybatis.common.BaseMapper;

public interface DevopsMiddlewareMapper extends BaseMapper<DevopsMiddlewareDTO> {
    /**
     * 检查名称唯一
     *
     * @param projectId 项目id
     * @param name      中间键名称
     * @param type      中间件类型
     * @return
     */
    Integer checkNameUnique(@Param("projectId") Long projectId, @Param("name") String name, @Param("type") String type);
}
