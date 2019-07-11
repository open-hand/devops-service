package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DevopsAppResourceDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lizongwei
 * @date 2019/7/3
 */
public interface DevopsAppResourceMapper extends Mapper<DevopsAppResourceDO> {

    /**
     * 查询传入的id在关系表中存在的id有哪些
     *
     * @param appId        应用id
     * @param resourceType 资源类型
     * @param resourceIds  待查询的资源id
     * @return 存在的资源id
     */
    List<Long> queryResourceIdsInApp(@Param("appId") Long appId, @Param("resourceType") String resourceType, @Param("resourceIds") List<Long> resourceIds);
}
