package io.choerodon.devops.infra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import io.choerodon.devops.infra.constant.GitOpsConstants;

/**
 * 用于异步同步用户的线程池
 *
 * @author zmf
 * @since 2020/12/31
 */
@Configuration
public class UserSyncThreadPoolConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncThreadPoolConfig.class);

    @Bean(name = GitOpsConstants.USER_SYNC_EXECUTOR)
    @Qualifier(GitOpsConstants.USER_SYNC_EXECUTOR)
    public AsyncTaskExecutor userSync() {
        LOGGER.info("Create UserExecutor to sync users");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(GitOpsConstants.USER_SYNC_EXECUTOR);
        // 设置2而不是1是因为，可能接到多个同步用户的请求，而可能需要一个线程长时间后台运行，
        // 另一个线程接收异步任务并获取锁失败然后返回，
        // 以免线程池中的任务过多或被拒绝
        executor.setMaxPoolSize(2);
        executor.setCorePoolSize(1);
        return executor;
    }
}
