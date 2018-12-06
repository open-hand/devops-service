package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsSecretDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:13
 * Description:
 */
public interface DevopsSecretMapper extends BaseMapper<DevopsSecretDO> {

    List<DevopsSecretDO> listByOption(@Param("envId") Long envId,
                                      @Param("searchParam") Map<String, Object> searchParam,
                                      @Param("param") String param);
}
