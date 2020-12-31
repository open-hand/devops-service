package io.choerodon.devops.app.task;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;

/**
 * 这个任务用于插入猪齿鱼的admin和gitlab的admin之间的用户关联关系
 * <p>
 * 因为0.22版本后admin的id可能不是1，所以从groovy插入的devops_user纪录(1,1)就可能不正确了，所以
 * 不再从groovy插入admin的用户关联数据，要服务启动之后，从iam服务查询得到
 * <p>
 * 这个问题修复在0.24.1，因为之前无反馈，且0.24.1发布在即
 *
 * @author zmf
 * @since 2020/12/29
 */
@Component
public class AdminUserTask implements CommandLineRunner {
    private static final Long GITLAB_ADMIN_ID = 1L;
    private static final String IAM_ADMIN_LOGIN_NAME = "admin";

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserTask.class);

    /**
     * 本地测试，可以将这个值设置为true
     */
    @Value("${local.test:false}")
    private Boolean localTest;

    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;

    @Override
    public void run(String... args) {
        if (Boolean.TRUE.equals(localTest)) {
            return;
        }
        try {
            // 先查询有么有
            UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(GITLAB_ADMIN_ID);
            if (userAttrDTO == null) {
                IamUserDTO iamUserDTO = baseServiceClientOperator.queryUserByLoginName(IAM_ADMIN_LOGIN_NAME);
                Long iamAdminId = Objects.requireNonNull(iamUserDTO.getId());

                // 查询下这个admin用户是否在devops_user表有数据了(但是gitlab user id 不是 1)
                UserAttrDTO adminUserInDb = userAttrService.baseQueryById(iamAdminId);
                // 有就跳过
                if (adminUserInDb != null) {
                    LOGGER.info("Abort inserting admin user relation data. The record with gitlab user id {} doesn't exist but the record with iam user id {} exists", GITLAB_ADMIN_ID, iamAdminId);
                    return;
                }

                userAttrDTO = new UserAttrDTO();
                userAttrDTO.setIamUserId(iamAdminId);
                userAttrDTO.setGitlabAdmin(Boolean.TRUE);
                userAttrDTO.setGitlabUserId(GITLAB_ADMIN_ID);
                userAttrService.baseInsert(userAttrDTO);
                LOGGER.info("Successfully insert admin user relation data iamUserId: {}, gitlabUserId: {}", iamAdminId, GITLAB_ADMIN_ID);
            } else {
                LOGGER.info("Admin user relation data exists. Skip...");
            }
        } catch (Exception ex) {
            LOGGER.warn("Failed to insert admin user relation data. ");
            LOGGER.warn("And the ex is:", ex);
        }
    }
}
