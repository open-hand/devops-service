package io.choerodon.devops.app.service;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2022/11/23 17:26
 */
public interface CdTaskSchedulerService {
    void schedulePeriodically();

    void cleanTimeoutTask(Long timeoutDuration);

}
