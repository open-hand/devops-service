package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsConfigDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;



/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsConfigMapper extends Mapper<DevopsConfigDTO> {
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
}
