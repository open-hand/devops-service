package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.DevopsSecretDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by n!Ck
 * Date: 18-12-4
 * Time: 上午10:13
 * Description:
 */
public interface DevopsSecretMapper extends Mapper<DevopsSecretDTO> {

    DevopsSecretDTO selectById(@Param("secretId") Long secretId);

    List<DevopsSecretDTO> listByOption(@Param("envId") Long envId,
                                       @Param("searchParam") Map<String, Object> searchParam,
                                       @Param("param") String param,
                                       @Param("appId") Long appId);
}
