package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsEnvPodInfoVO;
import io.choerodon.devops.api.vo.DevopsEnvPodVO;
import io.choerodon.devops.infra.dto.DevopsEnvPodDTO;
import io.choerodon.devops.infra.dto.PodResourceDetailsDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by Zenger on 2018/4/17.
 */
public interface DevopsEnvPodService {

    /**
     * @param projectId
     * @param envId
     * @param appServiceId
     * @param instanceId
     * @param pageable
     * @param searchParam
     * @return PageInfp
     */
    Page<DevopsEnvPodVO> pageByOptions(Long projectId, Long envId, Long appServiceId, Long instanceId, PageRequest pageable, String searchParam);

    void fillContainers(DevopsEnvPodVO devopsEnvPodVO);

    DevopsEnvPodDTO baseQueryById(Long id);

    DevopsEnvPodDTO baseQueryByEnvIdAndName(Long envId, String name);

    DevopsEnvPodDTO baseQueryByPod(DevopsEnvPodDTO devopsEnvPodDTO);

    void baseCreate(DevopsEnvPodDTO devopsEnvPodDTO);

    List<DevopsEnvPodDTO> baseListByInstanceId(Long instanceId);

    void baseUpdate(DevopsEnvPodDTO devopsEnvPodDTO);

    Page<DevopsEnvPodDTO> basePageByIds(Long projectId, Long envId, Long appServiceId, Long instanceId, PageRequest pageable, String searchParam);

    void baseDeleteByName(String name, String namespace);

    void baseDeleteByNameAndEnvId(String name, Long envId);

    void baseDeleteById(Long id);

    DevopsEnvPodDTO queryByNameAndEnvName(String name, String namespace);

    /**
     * 按资源用量列出环境下Pod信息
     *
     * @param envId 环境id
     * @param sort  排序条件
     * @return 环境下相关资源的数量
     */
    List<DevopsEnvPodInfoVO> queryEnvPodInfo(Long envId, String sort);

    void deleteEnvPodById(Long projectId, Long envId, Long podId);

    List<PodResourceDetailsDTO> queryResourceDetailsByInstanceId(Long instanceId);

    /**
     * 校验通过websocket对pod进行log或者exec指令有没有权限
     *
     * @param projectId 项目id
     * @param clusterId 集群id
     * @param envCode   环境code
     * @param userId    用户id
     * @param podName   pod名称
     * @return true表示有权限
     */
    boolean checkLogAndExecPermission(Long projectId, Long clusterId, String envCode, Long userId, String podName);

    Page<DevopsEnvPodVO> pageByKind(Long projectId, Long envId, String kind, String name, PageRequest pageable, String searchParam);
}
