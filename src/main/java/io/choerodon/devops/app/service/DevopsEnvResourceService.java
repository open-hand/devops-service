package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.DevopsEnvResourceVO;
import io.choerodon.devops.api.vo.InstanceEventVO;
import io.choerodon.devops.infra.dto.DevopsEnvResourceDTO;
import io.choerodon.devops.infra.enums.ResourceType;

/**
 * Created by younger on 2018/4/25.
 */
public interface DevopsEnvResourceService {

    /**
     * 方法只展示该应用的chart包中定义的资源，
     * 不包含前端页面之后创建的资源
     *
     * @param instanceId 实例id
     * @return 返回实例的资源
     */
    DevopsEnvResourceVO listResourcesInHelmRelease(Long instanceId);

    /**
     * 获取部署实例Event事件
     *
     * @param instanceId
     * @return
     */
    List<InstanceEventVO> listInstancePodEvent(Long instanceId);

    void baseCreate(DevopsEnvResourceDTO devopsEnvResourceDTO);

    List<DevopsEnvResourceDTO> baseListByInstanceId(Long instanceId);

    List<DevopsEnvResourceDTO> baseListByCommandId(Long commandId);

    void baseUpdate(DevopsEnvResourceDTO devopsEnvResourceDTO);

    void deleteByEnvIdAndKindAndName(Long envId, String kind, String name);

    List<DevopsEnvResourceDTO> baseListByEnvAndType(Long envId, String type);

    DevopsEnvResourceDTO baseQueryByKindAndName(String kind, String name);

    void deleteByKindAndNameAndInstanceId(String kind, String name, Long instanceId);

    DevopsEnvResourceDTO baseQueryOptions(Long instanceId, Long commandId, Long envId, String kind, String name);

    /**
     * get resource detail message
     *
     * @param instanceId   instance id
     * @param name         the resource name
     * @param resourceType the resource type
     * @return the detail message
     */
    String getResourceDetailByNameAndTypeAndInstanceId(Long instanceId, String name, ResourceType resourceType);

}
