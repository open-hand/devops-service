package io.choerodon.devops.app.service;


import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsPvPermissionUpdateVO;
import io.choerodon.devops.api.vo.DevopsPvReqVO;
import io.choerodon.devops.api.vo.DevopsPvVO;
import io.choerodon.devops.api.vo.ProjectReqVO;
import io.choerodon.devops.app.eventhandler.payload.PersistentVolumePayload;
import io.choerodon.devops.infra.dto.DevopsPvDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import java.util.List;


public interface DevopsPvService {

    /**
     * 查询根据Id查询单个PV信息
     */
    DevopsPvVO queryById(Long pvId);

    /**
     * 删除PV
     */
    Boolean deletePvById(Long projectId, Long pvId);

    /**
     * 根据条件分页查询PV
     */
    Page<DevopsPvDTO> basePagePvByOptions(Long projectId, PageRequest pageable, String params);

    /**
     * 分页查询pv以及关联的集群和PVC
     *
     * @param pageable
     * @param params
     * @return
     */
    Page<DevopsPvVO> pageByOptions(Long projectId, PageRequest pageable, String params);

    /**
     * 创建PV
     */
    void createPv(Long projectId, DevopsPvReqVO devopsPvReqVo);

    /**
     * 根据Pv名称和集群的Id校验唯一性
     */
    boolean isNameUnique(Long clusterId, String pvName);

    /**
     * 创建组织与PV的权限关联关系
     *
     * @param update
     */
    void assignPermission(Long projectId, DevopsPvPermissionUpdateVO update);

    /**
     * 更新PV表中的权限校验字段
     */
    void updateCheckPermission(DevopsPvPermissionUpdateVO update);

    /**
     * 更新PV表字段
     */
    void baseupdatePv(DevopsPvDTO devopsPvDTO);

    /**
     * 根据pvId查询pv
     */
    DevopsPvDTO baseQueryById(Long pvId);

    /**
     * 查询和PV没有绑定权限的项目
     *
     * @param projectId
     * @return
     */
    Page<ProjectReqVO> listNonRelatedProjects(Long projectId, Long pvId, Long selectedProjectId, PageRequest pageable, String params);

    /**
     * 根据项目id删除相对应的权限
     *
     * @param projectId        pv 所属项目id
     * @param pvId             pv id
     * @param relatedProjectId 关联项目id
     */
    void deleteRelatedProjectById(Long projectId, Long pvId, Long relatedProjectId);

    /**
     * 通过环境id和名称查找pvc
     *
     * @param envId 环境id
     * @param name  pv名称
     * @return pv纪录
     */
    DevopsPvDTO queryByEnvIdAndName(Long envId, String name);

    /**
     * 根据集群id和PV名称查询PV
     *
     * @param clusterId 集群id
     * @param name      pv名称
     * @return pv
     */
    DevopsPvDTO queryWithEnvByClusterIdAndName(Long clusterId, String name);

    DevopsPvDTO createOrUpdateByGitOps(DevopsPvReqVO devopsPvReqVO, Long userId);

    void deleteByGitOps(Long pvId);

    /**
     * PV跳过权限校验，查询所属集群下的所有项目
     *
     * @param projectId
     * @param pvId
     * @param pageable
     * @param params
     * @return
     */
    Page<ProjectReqVO> pageProjects(Long projectId, Long pvId, PageRequest pageable, String params);

    /**
     * PV不跳过权限校验，查询有关联的项目
     *
     * @param projectId
     * @param pvId
     * @param pageable
     * @param params
     * @return
     */
    Page<ProjectReqVO> pageRelatedProjects(Long projectId, Long pvId, PageRequest pageable, String params);

    /**
     * 根据环境id查询所有的PV
     *
     * @param envId 环境id
     * @return 列表
     */
    List<DevopsPvDTO> baseListByEnvId(Long envId);

    void baseUpdate(DevopsPvDTO devopsPvDTO);

    /**
     * 供创建pvc时查询可用pv时用
     *
     * @param projectId
     * @param envId
     * @param clusterId
     * @param params
     * @return
     */
    List<DevopsPvVO> queryPvcRelatedPv(Long projectId, Long envId, Long clusterId, String params, Integer mode);

    /**
     * 根据集群ID查询集群
     *
     * @param clusterId
     * @return
     */
    List<DevopsPvDTO> queryByClusterId(Long clusterId);

    void operatePvBySaga(PersistentVolumePayload persistentVolumePayload);
}
