package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.workload.CronJobInfoVO;
import io.choerodon.devops.app.service.DevopsCornJobService;
import io.choerodon.devops.infra.mapper.DevopsCornJobMapper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/6/8 11:22
 */
@Service
public class DevopsCornJobServiceImpl implements DevopsCornJobService {
    @Autowired
    private DevopsCornJobMapper devopsCornJobMapper;

    @Override
    public Page<CronJobInfoVO> pagingByEnvId(Long projectId, Long envId, PageRequest pageable, String name, Boolean fromInstance) {
        return null;
    }
}
