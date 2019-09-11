package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.AppServiceInstanceInfoDTO;
import io.choerodon.devops.infra.dto.AppServiceInstanceDTO;
import io.choerodon.devops.infra.dto.AppServiceInstanceOverViewDTO;
import io.choerodon.devops.infra.dto.DeployDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * Created by Zenger on 2018/4/15.
 */
public interface AppServiceInstanceMapper extends Mapper<AppServiceInstanceDTO> {
    AppServiceInstanceInfoDTO queryInfoById(@Param("instanceId") Long instanceId);

    List<AppServiceInstanceInfoDTO> listInstanceInfoByEnvAndOptions(@Param("envId") Long envId,
                                                                    @Param("searchParam") Map<String, Object> searchParam,
                                                                    @Param("params") List<String> params);

    List<AppServiceInstanceDTO> listApplicationInstance(@Param("projectId") Long projectId,
                                                        @Param("envId") Long envId,
                                                        @Param("versionId") Long versionId,
                                                        @Param("appServiceId") Long appServiceId,
                                                        @Param("instanceId") Long instanceId,
                                                        @Param("searchParam") Map<String, Object> searchParam,
                                                        @Param("params") List<String> params);


    List<AppServiceInstanceDTO> listApplicationInstanceCode(@Param("projectId") Long projectId,
                                                            @Param("envId") Long envId,
                                                            @Param("versionId") Long versionId,
                                                            @Param("appServiceId") Long appServiceId);

    List<AppServiceInstanceDTO> listRunningAndFailedInstance(@Param("projectId") Long projectId,
                                                             @Param("envId") Long envId,
                                                             @Param("appServiceId") Long appServiceId);


    int countByOptions(@Param("envId") Long envId,
                       @Param("appServiceId") Long appServiceId,
                       @Param("appServiceInstanceCode") String appServiceInstanceCode);

    String queryValueByEnvIdAndAppId(@Param("envId") Long envId, @Param("appServiceId") Long appServiceId);

    List<AppServiceInstanceOverViewDTO> listApplicationInstanceOverView(@Param("projectId") Long projectId, @Param("appServiceId") Long appServiceId, @Param("envIds") List<Long> envIds);

    String queryByInstanceId(@Param("instanceId") Long instanceId);

    List<DeployDTO> listDeployTime(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("appServiceIds") Long[] appServiceIds, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<DeployDTO> listDeployFrequency(@Param("projectId") Long projectId, @Param("envIds") Long[] envIds, @Param("appServiceId") Long appServiceId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    String getInstanceResourceDetailJson(@Param("instanceId") Long instanceId, @Param("resourceName") String resourceName, @Param("resourceType") String resourceType);

    void deleteInstanceRelInfo(@Param("instanceId") Long instanceId);

    Boolean checkCodeExist(@Param("code") String code, @Param("envIds") List<Long> envIds);

    int countNonDeletedInstances(@Param("appServiceId") Long appServiceId);

    int countNonDeletedInstancesWithEnv(@Param("envId")Long envId,@Param("instanceId")Long instanceId);

    void updateStatus(@Param("instanceId")Long instanceId,@Param("status") String status);
}
