package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.JobInfoVO;
import io.choerodon.devops.infra.dto.DevopsJobDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:21
 */
public interface DevopsJobService extends WorkloadBaseService<DevopsJobDTO> {

    Page<JobInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance);

}
