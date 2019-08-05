package io.choerodon.devops.infra.mapper;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsApplicationResourceDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
public interface DevopsAppServiceResourceMapper extends Mapper<DevopsApplicationResourceDTO> {
    /**
     * 查询传入的id在关系表中存在的id有哪些
     *
     * @param appServiceId        应用id
     * @param resourceType 资源类型
     * @param resourceIds  待查询的资源id
     * @return 存在的资源id
     */
    List<Long> queryResourceIdsInApp(@Param("appServiceId") Long appServiceId, @Param("resourceType") String resourceType, @Param("resourceIds") List<Long> resourceIds);
}
