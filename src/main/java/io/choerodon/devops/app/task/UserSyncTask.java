package io.choerodon.devops.app.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.GitlabUserService;

/**
 * 用于启动时同步创建未同步到gitlab的用户
 *
 * @author zmf
 * @since 2020/12/30
 */
@Component
public class UserSyncTask implements CommandLineRunner {
    /**
     * 本地测试，可以将这个值设置为true
     */
    @Value("local.test:false")
    private Boolean localTest;

    @Autowired
    private GitlabUserService gitlabUserService;

    @Override
    public void run(String... args) {
        if (Boolean.TRUE.equals(localTest)) {
            return;
        }
        gitlabUserService.syncAllUsers();
    }
}
