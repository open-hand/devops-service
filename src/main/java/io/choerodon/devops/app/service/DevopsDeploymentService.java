package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DeploymentInfoVO;
import io.choerodon.devops.api.vo.DevopsDeploymentVO;
import io.choerodon.devops.infra.dto.DevopsDeploymentDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:17
 */
public interface DevopsDeploymentService extends WorkloadBaseService {
    DevopsDeploymentDTO baseQueryByEnvIdAndName(Long envId, String name);

    DevopsDeploymentDTO baseQuery(Long resourceId);

    void deleteByGitOps(Long id);

    DevopsDeploymentVO createOrUpdateByGitOps(DevopsDeploymentVO devopsDeploymentVO, Long userId,String content);

    Page<DeploymentInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance);

    void baseDelete(Long id);
}
