package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.mybatis.common.BaseMapper;


/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsConfigMapper extends BaseMapper<DevopsConfigDTO> {
    List<DevopsConfigDTO> listByOptions(@Param("projectId") Long projectId,
                                        @Param("searchParam") Map<String, Object> searchParam,
                                        @Param("params") List<String> params,
                                        @Param("index") String index);

    List<DevopsConfigDTO> listByIdAndType(@Param("projectId") Long projectId, @Param("type") String type);

    List<Integer> checkIsUsed(@Param("configId") Long configId);

    DevopsConfigDTO queryByNameWithNoProject(@Param("name") String name);

    void deleteByProject();

    List<DevopsConfigDTO> existAppServiceConfig();

    DevopsConfigDTO queryDefaultConfig(@Param("type") String type);

    void updateResourceId(@Param("configId") Long configId);

    /**
     * 更新 name, app_service, project_id, organization_id 为null
     * @param configId 配置id
     */
    void updateConfigFieldsNull(@Param("configId") Long configId);

    List<DevopsConfigDTO> listByConfigs(@Param("configIds") Set<Long> configIds);

    List<DevopsConfigDTO> listAllChart();

}
