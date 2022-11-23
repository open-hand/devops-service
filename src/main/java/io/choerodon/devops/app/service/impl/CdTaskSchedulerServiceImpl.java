package io.choerodon.devops.app.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.app.service.CdTaskSchedulerService;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/23 17:26
 */
@Service
public class CdTaskSchedulerServiceImpl implements CdTaskSchedulerService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void schedulePeriodically() {

    }
}
