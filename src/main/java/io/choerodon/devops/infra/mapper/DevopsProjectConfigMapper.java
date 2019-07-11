package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dataobject.ApplicationDTO;
import io.choerodon.devops.infra.dto.DevopsProjectConfigDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;



/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsProjectConfigMapper extends Mapper<DevopsProjectConfigDO> {
    List<ApplicationDTO> list(@Param("projectId") Long projectId,
                              @Param("searchParam") Map<String, Object> searchParam,
                              @Param("param") String param,
                              @Param("index") String index);

    List<DevopsProjectConfigDO> queryByIdAndType(@Param("projectId") Long projectId, @Param("type") String type);

    List<Integer> checkIsUsed(@Param("configId") Long configId);

    DevopsProjectConfigDO queryByNameWithNoProject(@Param("name") String name);
}
