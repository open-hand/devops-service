package io.choerodon.devops.infra.config;

import java.util.concurrent.ThreadPoolExecutor;

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
public class PipelineAsyncTheadPoolConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineAsyncTheadPoolConfig.class);

    /**
     * ci流水线定时同步记录状态线程池
     *
     * @param corePoolSize
     * @param maxPoolSize
     * @return
     */
    @Bean
    @Qualifier(GitOpsConstants.PIPELINE_EXECUTOR)
    public AsyncTaskExecutor syncPipeline(@Value("${devops.ci.pipeline.sync.executor.corePoolSize:1}") Integer corePoolSize,
                                          @Value("${devops.ci.pipeline.sync.executor.maxPoolSize:8}") Integer maxPoolSize) {
        LOGGER.info("Create AsyncTaskExecutor for ci-pipeline. The coreSize is {} and the maxSize is {}", corePoolSize, maxPoolSize);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(GitOpsConstants.PIPELINE_EXECUTOR);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        return executor;
    }

    /**
     * 持续集成流水线任务消费线程池
     *
     * @param corePoolSize
     * @param maxPoolSize
     * @return
     */
    @Bean
    @Qualifier(GitOpsConstants.PIPELINE_EXEC_EXECUTOR)
    public AsyncTaskExecutor pipelineExecExecutor(@Value("${devops.pipeline.task.executor.corePoolSize:5}") Integer corePoolSize,
                                                  @Value("${devops.pipeline.task.executor.maxPoolSize:20}") Integer maxPoolSize) {
        LOGGER.info("Create AsyncTaskExecutor for pipeline exec. The coreSize is {} and the maxSize is {}", corePoolSize, maxPoolSize);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(GitOpsConstants.PIPELINE_EXEC_EXECUTOR);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(10000);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }
}
