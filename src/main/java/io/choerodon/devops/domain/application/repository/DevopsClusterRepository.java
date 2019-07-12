package io.choerodon.devops.domain.application.repository;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.iam.entity.DevopsClusterE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvPodE;

public interface DevopsClusterRepository {

    DevopsClusterE baseCreateCluster(DevopsClusterE devopsClusterE);

    void baseCheckName(DevopsClusterE devopsClusterE);

    void baseCheckCode(DevopsClusterE devopsClusterE);

    List<DevopsClusterE> baseListByProjectId(Long projectId, Long organizationId);

    DevopsClusterE baseQuery(Long clusterId);

    void baseUpdate(DevopsClusterE devopsClusterE);

    PageInfo<DevopsClusterE> basePageClustersByOptions(Long organizationId, Boolean doPage, PageRequest pageRequest, String params);

    void baseDelete(Long clusterId);

    DevopsClusterE baseQueryByToken(String token);

    List<DevopsClusterE> baseList();

    /**
     * 分页查询节点下的Pod
     * @param clusterId 集群id
     * @param nodeName 节点名称
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    PageInfo<DevopsEnvPodE> basePageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam);

    DevopsClusterE baseQueryByCode(Long organizationId, String code);

    void baseUpdateProjectId(Long orgId, Long proId);
}
