package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DeploymentVO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:17
 */
public interface DevopsDeploymentService {

    Page<DeploymentVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name);
}
