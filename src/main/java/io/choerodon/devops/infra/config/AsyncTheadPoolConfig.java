package io.choerodon.devops.infra.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.choerodon.devops.infra.constant.GitOpsConstants;

/**
 * 配置专用的异步任务线程池
 *
 * @author zmf
 * @since 2020/6/9
 */
@Configuration
public class AsyncTheadPoolConfig {

    @Bean
    @Qualifier(GitOpsConstants.PIPELINE_EXECUTOR)
    public AsyncTaskExecutor syncPipeline() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(GitOpsConstants.PIPELINE_EXECUTOR);
        executor.setMaxPoolSize(GitOpsConstants.DEFAULT_PIPELINE_RECORD_SIZE + 3);
        executor.setCorePoolSize(GitOpsConstants.DEFAULT_PIPELINE_RECORD_SIZE);
        return executor;
    }
}
