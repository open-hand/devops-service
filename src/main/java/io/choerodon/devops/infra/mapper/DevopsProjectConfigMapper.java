package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsProjectConfigDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;



/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsProjectConfigMapper extends Mapper<DevopsProjectConfigDTO> {
    List<AppServiceDTO> listByOptions(@Param("projectId") Long projectId,
                                      @Param("searchParam") Map<String, Object> searchParam,
                                      @Param("param") String param,
                                      @Param("index") String index);

    List<DevopsProjectConfigDTO> listByIdAndType(@Param("projectId") Long projectId, @Param("type") String type);

    List<Integer> checkIsUsed(@Param("configId") Long configId);

    DevopsProjectConfigDTO queryByNameWithNoProject(@Param("name") String name);
}
