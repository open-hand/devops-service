package io.choerodon.devops.infra.mapper;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.iam.entity.DevopsEnvApplicationE;
import io.choerodon.devops.infra.dto.ApplicationInstanceDTO;
import io.choerodon.devops.infra.dto.ApplicationInstancesDO;
import io.choerodon.devops.infra.dto.DeployDO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * Created by Zenger on 2018/4/15.
 */
public interface ApplicationInstanceMapper extends Mapper<ApplicationInstanceDTO> {

    List<ApplicationInstanceDTO> listApplicationInstance(@Param("projectId") Long projectId,
                                                         @Param("envId") Long envId,
                                                         @Param("versionId") Long versionId,
                                                         @Param("appId") Long appId,
                                                         @Param("instanceId") Long instanceId,
                                                         @Param("searchParam") Map<String, Object> searchParam,
                                                         @Param("param") String param);

    /**
     * 查询所有应用部署的appId和envId
     * 用于应用环境关联数据修复
     */
    List<DevopsEnvApplicationE> listAllEnvApp();

    List<ApplicationInstanceDTO> listApplicationInstanceCode(@Param("projectId") Long projectId,
                                                             @Param("envId") Long envId,
                                                             @Param("versionId") Long versionId,
                                                             @Param("appId") Long appId);

    List<ApplicationInstanceDTO> listRunningAndFailedInstance(@Param("projectId") Long projectId,
                                                              @Param("envId") Long envId,
                                                              @Param("appId") Long appId);

    int checkOptions(@Param("envId") Long envId,
                     @Param("appId") Long appId,
                     @Param("appInstanceCode") String appInstanceCode);

    String queryValueByEnvIdAndAppId(@Param("envId") Long envId, @Param("appId") Long appId);

    List<ApplicationInstancesDO> listApplicationInstances(@Param("projectId") Long projectId, @Param("appId") Long appId, @Param("envIds") List<Long> envIds);

    String queryByInstanceId(@Param("instanceId") Long instanceId);

    List<DeployDO> listDeployTime(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("appIds") Long[] appIds, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<DeployDO> listDeployFrequency(@Param("projectId") Long projectId, @Param("envIds") Long[] envIds, @Param("appId") Long appId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    String getInstanceResourceDetailJson(@Param("instanceId") Long instanceId, @Param("resourceName") String resourceName, @Param("resourceType") String resourceType);

    void deleteInstanceRelInfo(@Param("instanceId") Long instanceId);
}
