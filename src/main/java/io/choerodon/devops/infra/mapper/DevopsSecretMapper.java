package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.DevopsSecretDO;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:13
 * Description:
 */
public interface DevopsSecretMapper extends Mapper<DevopsSecretDO> {

    DevopsSecretDO selectById(@Param("secretId") Long secretId);

    List<DevopsSecretDO> listByOption(@Param("envId") Long envId,
                                      @Param("searchParam") Map<String, Object> searchParam,
                                      @Param("param") String param);

    List<DevopsSecretDO> listSecretByApp(@Param("secretIds") List<Long> secretIds,
                                           @Param("searchParam") Map<String, Object> searchParam,
                                           @Param("param") String param);
}
