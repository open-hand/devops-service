package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.AppInstanceInfoDTO;
import io.choerodon.devops.infra.dto.ApplicationInstanceDTO;
import io.choerodon.devops.infra.dto.ApplicationInstanceOverViewDTO;
import io.choerodon.devops.infra.dto.DeployDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * Created by Zenger on 2018/4/15.
 */
public interface ApplicationInstanceMapper extends Mapper<ApplicationInstanceDTO> {
    AppInstanceInfoDTO queryInfoById(@Param("instanceId") Long instanceId);

    List<AppInstanceInfoDTO> listInstanceInfoByEnvAndOptions(@Param("envId") Long envId,
                                                             @Param("searchParam") Map<String, Object> searchParam,
                                                             @Param("param") String param);

    List<ApplicationInstanceDTO> listApplicationInstance(@Param("projectId") Long projectId,
                                                         @Param("envId") Long envId,
                                                         @Param("versionId") Long versionId,
                                                         @Param("appServiceId") Long appServiceId,
                                                         @Param("instanceId") Long instanceId,
                                                         @Param("searchParam") Map<String, Object> searchParam,
                                                         @Param("param") String param);


    List<ApplicationInstanceDTO> listApplicationInstanceCode(@Param("projectId") Long projectId,
                                                             @Param("envId") Long envId,
                                                             @Param("versionId") Long versionId,
                                                             @Param("appServiceId") Long appServiceId);

    List<ApplicationInstanceDTO> listRunningAndFailedInstance(@Param("projectId") Long projectId,
                                                              @Param("envId") Long envId,
                                                              @Param("appServiceId") Long appServiceId);


    int countByOptions(@Param("envId") Long envId,
                       @Param("appServiceId") Long appServiceId,
                       @Param("appInstanceCode") String appInstanceCode);

    String queryValueByEnvIdAndAppId(@Param("envId") Long envId, @Param("appServiceId") Long appServiceId);

    List<ApplicationInstanceOverViewDTO> listApplicationInstanceOverView(@Param("projectId") Long projectId, @Param("appServiceId") Long appServiceId, @Param("envIds") List<Long> envIds);

    String queryByInstanceId(@Param("instanceId") Long instanceId);

    List<DeployDTO> listDeployTime(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("appIds") Long[] appIds, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<DeployDTO> listDeployFrequency(@Param("projectId") Long projectId, @Param("envIds") Long[] envIds, @Param("appServiceId") Long appServiceId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    String getInstanceResourceDetailJson(@Param("instanceId") Long instanceId, @Param("resourceName") String resourceName, @Param("resourceType") String resourceType);

    void deleteInstanceRelInfo(@Param("instanceId") Long instanceId);
}
