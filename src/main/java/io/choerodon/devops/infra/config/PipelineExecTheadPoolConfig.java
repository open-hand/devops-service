package io.choerodon.devops.infra.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.choerodon.devops.infra.constant.GitOpsConstants;

@Configuration
public class PipelineExecTheadPoolConfig {

    @Bean
    @Qualifier(GitOpsConstants.PIPELINE_EXEC_EXECUTOR)
    public AsyncTaskExecutor pipelineExecExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(GitOpsConstants.PIPELINE_EXEC_EXECUTOR);
        executor.setMaxPoolSize(20);
        executor.setCorePoolSize(5);
        executor.setQueueCapacity(10000);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }
}
