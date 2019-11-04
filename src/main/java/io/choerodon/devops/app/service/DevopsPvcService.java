package io.choerodon.devops.app.service;

import io.choerodon.devops.api.vo.DevopsPvcReqVO;
import io.choerodon.devops.infra.dto.DevopsPvcDTO;

public interface DevopsPvcService {
    void create(Long projectId, DevopsPvcReqVO devopsPvcReqVO);

    void baseCheckName(String PvcName, Long envId);

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

    void deleteByGitOps(Long pvcId);
}
