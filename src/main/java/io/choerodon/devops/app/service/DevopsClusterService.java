package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvPodE;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;

public interface DevopsClusterService {

    String createCluster(Long organizationId, DevopsClusterReqDTO devopsClusterReqDTO);

    void updateCluster(Long clusterId, DevopsClusterReqDTO devopsClusterReqDTO);

    void checkName(Long organizationId, String name);

    PageInfo<ProjectReqVO> listProjects(Long organizationId, Long clusterId, PageRequest pageRequest, String[] params);

    String queryShell(Long clusterId);

    void checkCode(Long organizationId, String code);

    PageInfo<ClusterWithNodesDTO> pageClusters(Long organizationId, Boolean doPage, PageRequest pageRequest, String params);

    List<ProjectReqVO> listClusterProjects(Long organizationId, Long clusterId);

    void deleteCluster(Long clusterId);

    DevopsClusterRepDTO getCluster(Long clusterId);

    Boolean IsClusterRelatedEnvs(Long clusterId);

    /**
     * 分页查询节点下的Pod
     * @param clusterId 集群id
     * @param nodeName 节点名称
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    PageInfo<DevopsClusterPodVO> pageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam);


    /**
     *  根据组织Id和集群编码查询集群信息
     *
     * @param organizationId organization id
     * @param code      the cluster code
     * @return the node information
     */
    DevopsClusterRepDTO queryByCode(Long organizationId, String code);


    DevopsClusterDTO baseCreateCluster(DevopsClusterDTO devopsClusterDTO);

    void baseCheckName(DevopsClusterDTO devopsClusterDTO);

    void baseCheckCode(DevopsClusterDTO devopsClusterDTO);

    List<DevopsClusterDTO> baseListByProjectId(Long projectId, Long organizationId);

    DevopsClusterDTO baseQuery(Long clusterId);

    void baseUpdate(DevopsClusterDTO devopsClusterDTO);

    PageInfo<DevopsClusterDTO> basePageClustersByOptions(Long organizationId, Boolean doPage, PageRequest pageRequest, String params);

    void baseDelete(Long clusterId);

    DevopsClusterDTO baseQueryByToken(String token);

    List<DevopsClusterDTO> baseList();

    /**
     * 分页查询节点下的Pod
     * @param clusterId 集群id
     * @param nodeName 节点名称
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    PageInfo<DevopsEnvPodE> basePageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam);

    DevopsClusterDTO baseQueryByCode(Long organizationId, String code);

    void baseUpdateProjectId(Long orgId, Long proId);
}
