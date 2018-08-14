package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO;
import io.choerodon.devops.infra.dataobject.ApplicationInstancesDO;
import io.choerodon.mybatis.common.BaseMapper;


/**
 * Created by Zenger on 2018/4/15.
 */
public interface ApplicationInstanceMapper extends BaseMapper<ApplicationInstanceDO> {

    List<ApplicationInstanceDO> listApplicationInstance(@Param("projectId") Long projectId,
                                                        @Param("envId") Long envId,
                                                        @Param("versionId") Long versionId,
                                                        @Param("appId") Long appId,
                                                        @Param("searchParam") Map<String, Object> searchParam,
                                                        @Param("param") String param);

    List<ApplicationInstanceDO> listApplicationInstanceCode(@Param("projectId") Long projectId,
                                                            @Param("envId") Long envId,
                                                            @Param("versionId") Long versionId,
                                                            @Param("appId") Long appId);

    int checkOptions(@Param("envId") Long envId,
                     @Param("appId") Long appId,
                     @Param("appInstanceId") Long appInstanceId);

    String queryValueByEnvIdAndAppId(@Param("envId") Long envId, @Param("appId") Long appId);

    List<ApplicationInstancesDO> listApplicationInstances(@Param("projectId") Long projectId, @Param("appId") Long appId);
}
