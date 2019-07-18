package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;

public interface DevopsClusterService {

    /**
     * 创建证书
     *
     * @param organizationId
     * @param devopsClusterReqVO
     * @return
     */
    String createCluster(Long organizationId, DevopsClusterReqVO devopsClusterReqVO);

    /**
     * 更新集群
     *
     * @param clusterId
     * @param devopsClusterReqVO
     */
    void updateCluster(Long clusterId, DevopsClusterReqVO devopsClusterReqVO);

    /**
     * 校验集群名唯一性
     *
     * @param organizationId
     * @param name
     */
    void checkName(Long organizationId, String name);

    /**
     * 分页查询项目列表
     *
     * @param organizationId
     * @param clusterId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<ProjectReqVO> pageProjects(Long organizationId, Long clusterId, PageRequest pageRequest, String[] params);

    String queryShell(Long clusterId);

    /**
     * 校验集群编码唯一性
     *
     * @param organizationId
     * @param code
     */
    void checkCode(Long organizationId, String code);

    /**
     * 集群列表查询
     *
     * @param organizationId
     * @param doPage
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<ClusterWithNodesVO> pageClusters(Long organizationId, Boolean doPage, PageRequest pageRequest, String params);

    /**
     * 查询集群下已有权限的项目列表
     *
     * @param organizationId
     * @param clusterId
     * @return
     */
    List<ProjectReqVO> listClusterProjects(Long organizationId, Long clusterId);

    /**
     * 删除集群
     *
     * @param clusterId
     */
    void deleteCluster(Long clusterId);

    /**
     * 查询单个集群信息
     *
     * @param clusterId
     */
    DevopsClusterRepVO query(Long clusterId);

    /**
     * 查询集群下是否关联已连接环境
     *
     * @param clusterId
     * @return
     */
    Boolean checkConnectEnvs(Long clusterId);

    /**
     * 分页查询节点下的Pod
     * @param clusterId 集群id
     * @param nodeName 节点名称
     * @param pageRequest 分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    PageInfo<DevopsClusterPodVO> pagePodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam);


    /**
     *  根据组织Id和集群编码查询集群信息
     *
     * @param organizationId organization id
     * @param code      the cluster code
     * @return the node information
     */
    DevopsClusterRepVO queryByCode(Long organizationId, String code);


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
    PageInfo<DevopsEnvPodDTO> basePageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageRequest, String searchParam);

    DevopsClusterDTO baseQueryByCode(Long organizationId, String code);

    void baseUpdateProjectId(Long orgId, Long proId);
}
