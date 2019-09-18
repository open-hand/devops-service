package io.choerodon.devops.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsAppServiceResourceDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
public interface DevopsAppServiceResourceMapper extends Mapper<DevopsAppServiceResourceDTO> {
    /**
     * 查询传入的id在关系表中存在的id有哪些
     *
     * @param appServiceId 应用id
     * @param resourceType 资源类型
     * @param resourceIds  待查询的资源id
     * @return 存在的资源id
     */
    List<Long> queryResourceIdsInApp(@Param("appServiceId") Long appServiceId, @Param("resourceType") String resourceType, @Param("resourceIds") List<Long> resourceIds);


    /**
     * 查询应用服务关联的资源数量
     *
     * @param appServiceId 应用服务id
     * @return 资源数量
     */
    int countRelatedResource(@Param("appServiceId") Long appServiceId);
}
