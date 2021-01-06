package io.choerodon.devops.app.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.GitlabUserService;

/**
 * 用于启动时同步创建未同步到gitlab的用户
 *
 * @author zmf
 * @since 2020/12/30
 */
@ConditionalOnProperty(value = "local.test", havingValue = "false", matchIfMissing = true)
@Order(300)
@Component
public class UserSyncTask implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncTask.class);
    @Autowired
    private GitlabUserService gitlabUserService;

    @Override
    public void run(String... args) {
        LOGGER.info("Start task: try to handle users async...");
        try {
            gitlabUserService.asyncHandleAllUsers();
            LOGGER.info("Start task: submit task to handle users...");
        } catch (Exception ex) {
            LOGGER.warn("Failed to submit task to handle users. And the ex is:", ex);
        }
    }
}
