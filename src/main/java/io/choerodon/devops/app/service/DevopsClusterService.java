package io.choerodon.devops.app.service;

import java.util.List;
import javax.annotation.Nullable;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.devops.infra.enums.ClusterStatusEnum;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsClusterService {
    /**
     * 将集群一些信息存在redis中
     *
     * @param clusterId            集群id
     * @param clusterSummaryInfoVO 集群的一些信息
     */
    void saveClusterSummaryInfo(Long clusterId, ClusterSummaryInfoVO clusterSummaryInfoVO);

    /**
     * 查询集群的一些总结信息，主要由健康检查界面需要
     *
     * @param clusterId 集群id
     * @return 信息
     */
    @Nullable
    ClusterSummaryInfoVO queryClusterSummaryInfo(Long clusterId);

    /**
     * 创建集群
     *
     * @param projectId          项目id
     * @param devopsClusterReqVO 集群信息
     * @return 本次操作id
     */
    String createCluster(Long projectId, DevopsClusterReqVO devopsClusterReqVO) throws Exception;

    /**
     * 激活集群
     */
    String activateCluster(Long projectId, DevopsClusterReqVO devopsClusterReqVO);

    /**
     * 更新集群
     *
     * @param clusterId             集群id
     * @param devopsClusterUpdateVO 集群信息
     */
    void updateCluster(Long projectId, Long clusterId, DevopsClusterUpdateVO devopsClusterUpdateVO);

    /**
     * 判断集群名唯一性
     *
     * @param projectId 项目id
     * @param name      集群名称
     * @return true表示是
     */
    boolean isNameUnique(Long projectId, String name);


    String queryShell(Long clusterId);

    /**
     * 判断集群编码唯一性
     *
     * @param projectId 项目id
     * @param code      集群code
     * @return true表示唯一
     */
    boolean isCodeUnique(Long projectId, String code);


    /**
     * 集群列表查询
     *
     * @param projectId 项目id
     * @param doPage    是否分页
     * @param pageable  分页参数
     * @param params    查询参数
     * @return 集群列表
     */
    Page<ClusterWithNodesVO> pageClusters(Long projectId, Boolean doPage, PageRequest pageable, String params);


    /**
     * 列出组织下所有项目中在数据库中没有权限关联关系的项目(不论当前数据库中是否跳过权限检查)
     *
     * @param projectId 项目ID
     * @param clusterId 集群ID
     * @param params    搜索参数
     * @return 组织下所有项目中在数据库中没有权限关联关系的项目
     */
    Page<ProjectReqVO> listNonRelatedProjects(Long projectId, Long clusterId, Long selectedProjectId, PageRequest pageable, String params);

    /**
     * 分配权限
     *
     * @param devopsClusterPermissionUpdateVO 集群权限信息
     */
    void assignPermission(Long projectId, DevopsClusterPermissionUpdateVO devopsClusterPermissionUpdateVO);

    /**
     * 删除该项目对该集群的权限
     *
     * @param clusterId        集群id
     * @param relatedProjectId 项目id
     */
    void deletePermissionOfProject(Long projectId, Long clusterId, Long relatedProjectId);

    /**
     * 查询项目下的集群以及所有节点信息
     *
     * @param projectId 项目id
     * @return 集群列表
     */
    List<DevopsClusterBasicInfoVO> queryClustersAndNodes(Long projectId);

    /**
     * 分页查询组织下在数据库中已有关联关系项目列表
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @param pageable  分页参数
     * @param params    查询参数
     * @return List
     */
    Page<ProjectReqVO> pageRelatedProjects(Long projectId, Long clusterId, PageRequest pageable, String params);

    /**
     * 删除集群
     *
     * @param clusterId 集群id
     */
    void deleteCluster(Long projectId, Long clusterId);

    /**
     * 查询单个集群信息
     *
     * @param clusterId 集群id
     */
    DevopsClusterRepVO query(Long clusterId);

    /**
     * 查询集群下是否关联已连接环境
     *
     * @param clusterId 集群id
     * @return
     */
    ClusterMsgVO checkConnectEnvsAndPV(Long clusterId);

    /**
     * 分页查询节点下的Pod
     *
     * @param clusterId   集群id
     * @param nodeName    节点名称
     * @param pageable    分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    Page<DevopsEnvPodVO> pagePodsByNodeName(Long clusterId, String nodeName, PageRequest pageable, String searchParam);


    /**
     * 根据组织Id和集群编码查询集群信息
     *
     * @param projectId project id
     * @param code      the cluster code
     * @return the node information
     */
    DevopsClusterRepVO queryByCode(Long projectId, String code);


    DevopsClusterDTO baseCreateCluster(DevopsClusterDTO devopsClusterDTO);

    List<DevopsClusterDTO> baseListByProjectId(Long projectId, Long organizationId);

    DevopsClusterDTO baseQuery(Long clusterId);

    void baseUpdate(Long projectId, DevopsClusterDTO devopsClusterDTO);

    Page<DevopsClusterDTO> basePageClustersByOptions(Long organizationId, Boolean doPage, PageRequest pageable, String params);

    void baseDelete(Long clusterId);

    DevopsClusterDTO baseQueryByToken(String token);

    List<DevopsClusterDTO> baseList();

    /**
     * 分页查询节点下的Pod
     *
     * @param clusterId   集群id
     * @param nodeName    节点名称
     * @param pageable    分页参数
     * @param searchParam 查询参数
     * @return pods
     */
    Page<DevopsEnvPodDTO> basePageQueryPodsByNodeName(Long clusterId, String nodeName, PageRequest pageable, String searchParam);

    DevopsClusterDTO baseQueryByCode(Long organizationId, String code);

    void baseUpdateProjectId(Long orgId, Long proId);

    Boolean checkUserClusterPermission(Long clusterId, Long userId);

    ClusterOverViewVO getOrganizationClusterOverview(Long organizationId);

    /**
     * 获取平台级别的集群概览数据
     *
     * @return 平台的集群概览数据
     */
    ClusterOverViewVO getSiteClusterOverview();

    /**
     * 检查是否还能创建集群
     *
     * @param projectId
     * @return
     */
    Boolean checkEnableCreateCluster(Long projectId);

    /**
     * 重试安装k8s
     */
    void retryInstallK8s(Long projectId, Long clusterId);

    /**
     * 获得节点检查进度
     *
     * @param projectId 项目id
     * @param key       redisKey
     */
    DevopsNodeCheckResultVO checkProgress(Long projectId, String key);

    /**
     * 获得agent安装命令
     *
     * @param devopsClusterDTO 集群dto
     * @param userEmail        用户信息
     * @return 安装命令
     */
    String getInstallString(DevopsClusterDTO devopsClusterDTO, String userEmail);

    /**
     * 保存集群信息
     *
     * @param projectId          项目id
     * @param devopsClusterReqVO 集群信息
     * @param type               类型
     * @return
     */
    DevopsClusterDTO insertClusterInfo(Long projectId, DevopsClusterReqVO devopsClusterReqVO, String type);

    /**
     * 更新集群操作状态为操作中
     * @param clusterId
     */
    void updateClusterStatusToOperating(Long clusterId);
    void updateClusterStatusToOperatingInNewTrans(Long clusterId);

    /**
     * 更新集群状态
     * @param clusterId
     * @param disconnect
     */
    void updateStatusById(Long clusterId, ClusterStatusEnum disconnect);

    void updateStatusByIdInNewTrans(Long clusterId, ClusterStatusEnum disconnect);

    Long countClusterByOptions(Long projectId);

    String disconnectionHost(Long clusterId);

    void restartAgent(Long projectId, Long clusterId);
}
