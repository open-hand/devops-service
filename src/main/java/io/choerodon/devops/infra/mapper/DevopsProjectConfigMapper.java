package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.devops.infra.dataobject.DevopsProjectConfigDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author zongw.lee@gmail.com
 * @since 2019/03/11
 */
public interface DevopsProjectConfigMapper extends BaseMapper<DevopsProjectConfigDO> {
    List<ApplicationDO> list(@Param("projectId") Long projectId,
                             @Param("searchParam") Map<String, Object> searchParam,
                             @Param("param") String param,
                             @Param("index") String index);

    List<DevopsProjectConfigDO> queryByIdAndType(@Param("projectId") Long projectId, @Param("type") String type);

    List<Integer> checkIsUsed(@Param("configId") Long configId);
}
