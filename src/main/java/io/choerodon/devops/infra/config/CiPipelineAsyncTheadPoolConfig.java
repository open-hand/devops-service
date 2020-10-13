package io.choerodon.devops.infra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
public class CiPipelineAsyncTheadPoolConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(CiPipelineAsyncTheadPoolConfig.class);

    @Bean
    @Qualifier(GitOpsConstants.PIPELINE_EXECUTOR)
    public AsyncTaskExecutor syncPipeline(@Value("${devops.ci.pipeline.sync.executor.corePoolSize:5}") Integer corePoolSize,
                                          @Value("${devops.ci.pipeline.sync.executor.maxPoolSize:8}") Integer maxPoolSize) {
        LOGGER.info("Create AsyncTaskExecutor for ci-pipeline. The coreSize is {} and the maxSize is {}", corePoolSize, maxPoolSize);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(GitOpsConstants.PIPELINE_EXECUTOR);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        return executor;
    }
}
