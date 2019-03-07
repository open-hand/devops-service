package io.choerodon.devops.domain.application.repository;

import java.util.Date;
import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.domain.application.entity.ApplicationInstanceE;
import io.choerodon.devops.infra.common.util.enums.ResourceType;
import io.choerodon.devops.infra.dataobject.ApplicationInstancesDO;
import io.choerodon.devops.infra.dataobject.DeployDO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/12.
 */
public interface ApplicationInstanceRepository {

    Page<ApplicationInstanceE> listApplicationInstance(Long projectId, PageRequest pageRequest,
                                                       Long envId, Long versionId, Long appId, String params);

    ApplicationInstanceE selectByCode(String code, Long envId);

    ApplicationInstanceE create(ApplicationInstanceE applicationInstanceE);

    ApplicationInstanceE selectById(Long id);

    List<ApplicationInstanceE> listByOptions(Long projectId, Long appId, Long appVersionId, Long envId);

    List<ApplicationInstanceE> listByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    List<ApplicationInstanceE> getByAppIdAndEnvId(Long projectId, Long appId, Long envId);

    int checkOptions(Long envId, Long appId, String appInstanceCode);

    String queryValueByEnvIdAndAppId(Long envId, Long appId);

    void update(ApplicationInstanceE applicationInstanceE);

    List<ApplicationInstanceE> selectByEnvId(Long envId);

    List<ApplicationInstancesDO> getDeployInstances(Long projectId, Long appId, List<Long> envIds);

    List<ApplicationInstanceE> list();

    String queryValueByInstanceId(Long instanceId);

    void deleteById(Long id);

    List<DeployDO> listDeployTime(Long projectId, Long envId, Long[] appIds, Date startTime, Date endTime);


    List<DeployDO> listDeployFrequency(Long projectId, Long[] envIds, Long appId, Date startTime, Date endTime);

    Page<DeployDO> pageDeployFrequencyDetail(Long projectId, PageRequest pageRequest, Long[] envIds, Long appId,
                                             Date startTime, Date endTime);

    Page<DeployDO> pageDeployTimeDetail(Long projectId, PageRequest pageRequest, Long envId, Long[] appIds,
                                        Date startTime, Date endTime);

    List<ApplicationInstanceE> listByAppId(Long appId);

    void deleteAppInstanceByEnvId(Long envId);

    void checkName(String instanceName);


    /**
     * 根据实例id获取更多资源详情(json格式）
     *
     * @param instanceId   实例id
     * @param resourceName 资源(Deployment, DaemonSet, StatefulSet等)的name
     * @param resourceType 资源类型
     * @return 详情json字符串
     */
    String getInstanceResourceDetailJson(Long instanceId, String resourceName, ResourceType resourceType);
}
