package io.choerodon.devops.domain.application.repository;

import java.util.List;

import io.choerodon.devops.domain.application.entity.DevopsEnvResourceE;
import io.choerodon.devops.infra.enums.ResourceType;

/**
 * Created by younger on 2018/4/24.
 */
public interface DevopsEnvResourceRepository {

    void create(DevopsEnvResourceE devopsEnvResourceE);

    List<DevopsEnvResourceE> listByInstanceId(Long instanceId);

    List<DevopsEnvResourceE> listJobs(Long commandId);

    void update(DevopsEnvResourceE devopsEnvResourceE);

    void deleteByEnvIdAndKindAndName(Long envId, String kind, String name);

    List<DevopsEnvResourceE> listByEnvAndType(Long envId, String type);

    DevopsEnvResourceE queryLatestJob(String kind, String name);

    void deleteByKindAndNameAndInstanceId(String kind, String name, Long instanceId);

    DevopsEnvResourceE queryResource(Long instanceId, Long commandId, Long envId, String kind, String name);

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