package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.workload.CronJobInfoVO;
import io.choerodon.devops.api.vo.workload.DevopsCronjobVO;
import io.choerodon.devops.infra.dto.DevopsCronJobDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:22
 */
public interface DevopsCronJobService extends WorkloadBaseService<DevopsCronJobDTO, DevopsCronjobVO> {

    Page<CronJobInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance);
}
