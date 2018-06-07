package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationMapper extends BaseMapper<ApplicationDO> {
    List<ApplicationDO> list(@Param("projectId") Long projectId,
                             @Param("isActive") Boolean isActive,
                             @Param("searchParam") Map<String, Object> searchParam,
                             @Param("param") String param);

    List<ApplicationDO> listByEnvId(@Param("projectId") Long projectId,
                                    @Param("envId") Long envId,
                                    @Param("status") String status);

    List<ApplicationDO> listByActiveAndPubAndVersion(@Param("projectId") Long projectId,
                                                     @Param("active") Boolean active,
                                                     @Param("searchParam") Map<String, Object> searchParam,
                                                     @Param("param") String param);

    ApplicationDO queryByToken(@Param("token") String token);

    List<ApplicationDO> listActive(@Param("projectId") Long projectId);
}
