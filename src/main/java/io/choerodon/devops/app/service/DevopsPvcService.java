package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsPvcReqVO;
import io.choerodon.devops.api.vo.DevopsPvcRespVO;
import io.choerodon.devops.app.eventhandler.payload.PersistentVolumeClaimPayload;
import io.choerodon.devops.infra.dto.DevopsPvcDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

public interface DevopsPvcService {
    /**
     * 创建PVC
     *
     * @param projectId
     * @param devopsPvcReqVO
     * @return
     */
    DevopsPvcRespVO create(Long projectId, DevopsPvcReqVO devopsPvcReqVO);

    /**
     * 删除PVC
     *
     * @param envId
     * @param pvcId
     * @return
     */
    boolean delete(Long envId, Long pvcId);

    Page<DevopsPvcRespVO> pageByOptions(Long projectId, Long envId, PageRequest pageable, String params);

    /**
     * 检查PVC名称唯一性
     *
     * @param pvcName
     * @param envId
     */
    void baseCheckName(String pvcName, Long envId);

    /**
     * 通过环境id和名称查找pvc
     *
     * @param envId 环境id
     * @param name  pvc名称
     * @return pvc纪录
     */
    DevopsPvcDTO queryByEnvIdAndName(Long envId, String name);

    /**
     * 创建或者更新pvc
     *
     * @param userId         用户id
     * @param devopsPvcReqVO pvc相关信息
     */
    DevopsPvcDTO createOrUpdateByGitOps(Long userId, DevopsPvcReqVO devopsPvcReqVO);

    /**
     * GitOps逻辑中删除pvc
     *
     * @param pvcId pvc的ID
     */
    void deleteByGitOps(Long pvcId);

    void baseUpdate(DevopsPvcDTO devopsPvcDTO);

    DevopsPvcDTO queryById(Long pvcId);

    /**
     * 通过环境id查询所有的pvc纪录
     *
     * @param envId 环境id
     * @return pvc列表
     */
    List<DevopsPvcDTO> baseListByEnvId(Long envId);

    void operatePvcBySaga(PersistentVolumeClaimPayload persistentVolumeClaimPayload);

    DevopsPvcDTO queryByPvId(Long pvId);

    void baseDeleteByEnvId(Long envId);

    /**
     * 重试将PVC推向GitLab（pvc推向gitlab失败时可用，不失败时用了没有效果也不会报错）
     *
     * @param pvcId pvcId
     */
    void retryPushPvcToGitLab(Long pvcId);
}
