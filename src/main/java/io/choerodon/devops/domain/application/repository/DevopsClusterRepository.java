package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.domain.application.entity.DevopsClusterE;
import io.choerodon.devops.domain.application.entity.DevopsEnvPodE;

public interface DevopsClusterRepository {

    DevopsClusterE create(DevopsClusterE devopsClusterE);

    void checkName(DevopsClusterE devopsClusterE);

    void checkCode(DevopsClusterE devopsClusterE);

    List<DevopsClusterE> listByProjectId(Long projectId, Long organizationId);

    DevopsClusterE query(Long clusterId);

    void update(DevopsClusterE devopsClusterE);

    PageInfo<DevopsClusterE> pageClusters(Long organizationId, Boolean doPage, PageRequest pageRequest, String params);

    void delete(Long clusterId);

    DevopsClusterE queryByToken(String token);

    List<DevopsClusterE> list();

    /**
     * 分页查询节点下的Pod
     * @param clusterId 集群id
     * @param nodeName 节点名称
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    PageInfo<DevopsEnvPodE> pageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam);

    DevopsClusterE queryByCode(Long organizationId, String code);
}
