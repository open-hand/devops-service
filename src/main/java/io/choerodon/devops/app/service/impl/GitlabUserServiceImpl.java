package io.choerodon.devops.app.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.netty.util.internal.IntegerHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.api.vo.GitlabUserRequestVO;
import io.choerodon.devops.app.service.GitlabUserService;
import io.choerodon.devops.app.service.SendNotificationService;
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.infra.config.GitlabConfigurationProperties;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabUserReqDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.LogUtil;
import io.choerodon.devops.infra.util.TypeUtil;

/**
 * Created by Zenger on 2018/3/28.
 */
@Service
public class GitlabUserServiceImpl implements GitlabUserService {
    private static final String SERVICE_PATTERN = "[a-zA-Z0-9_\\.][a-zA-Z0-9_\\-\\.]*[a-zA-Z0-9_\\-]|[a-zA-Z0-9_]";
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabUserService.class);
    private static final String ANONYMOUS_USER_LOGIN_NAME = "ANONYMOUS";
    /**
     * 创建的用户邮箱重复的失败信息
     */
    private static final String GITLAB_USER_EMAIL_DUPLICATED_MESSAGE = "Email has already been taken";
    /**
     * 分布式锁持有的时间
     */
    private static final long LOCK_HOLD_MINUTES = 10;
    /**
     * 进度的模板 已处理/总数
     */
    private static final String PROCESS_PATTERN = "%s/%s";
    /**
     * 分批处理用户时，一批用户的数量
     */
    private static final int USER_BATCH_SIZE = 50;
    /**
     * 设置用户处理的分布式锁时，锁的key
     */
    private static final String USER_SYNC_REDIS_KEY = "user-sync-key";
    /**
     * 用户请求失败的阈值, 超过阈值，睡眠一段时间
     */
    @Value("${devops.user.userFailureThreshold:10}")
    private int userFailureThreshold;
    /**
     * 达到阈值后，睡眠的时间
     */
    @Value("${devops.user.failureSleepSeconds:20}")
    private int failureSleepSeconds;

    @Autowired
    private GitlabConfigurationProperties gitlabConfigurationProperties;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;
    @Autowired
    private SendNotificationService sendNotificationService;
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public void createGitlabUser(GitlabUserRequestVO gitlabUserReqDTO) {

        checkGitlabUser(gitlabUserReqDTO);
        GitLabUserDTO gitLabUserDTO = gitlabServiceClientOperator.queryUserByUserName(gitlabUserReqDTO.getUsername());
        if (gitLabUserDTO == null) {
            String randomPassword = GenerateUUID.generateRandomGitlabPassword();

            gitLabUserDTO = gitlabServiceClientOperator.createUser(
                    randomPassword,
                    gitlabConfigurationProperties.getProjectLimit(),
                    ConvertUtils.convertObject(gitlabUserReqDTO, GitlabUserReqDTO.class));

            // 以通知形式告知默认密码
            sendNotificationService.sendForUserDefaultPassword(gitlabUserReqDTO.getExternUid(), randomPassword);
        }
        UserAttrDTO userAttrDTO = userAttrService.baseQueryByGitlabUserId(gitLabUserDTO.getId().longValue());
        if (userAttrDTO == null) {
            userAttrDTO = new UserAttrDTO();
            userAttrDTO.setIamUserId(Long.parseLong(gitlabUserReqDTO.getExternUid()));
            userAttrDTO.setGitlabUserId(gitLabUserDTO.getId().longValue());
            userAttrDTO.setGitlabUserName(gitLabUserDTO.getUsername());
            userAttrService.baseInsert(userAttrDTO);
        }
    }

    @Override
    public void updateGitlabUser(GitlabUserRequestVO gitlabUserReqDTO) {

        checkGitlabUser(gitlabUserReqDTO);
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(TypeUtil.objToLong(gitlabUserReqDTO.getExternUid()));
        if (userAttrDTO != null) {
            gitlabServiceClientOperator.updateUser(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()),
                    gitlabConfigurationProperties.getProjectLimit(),
                    ConvertUtils.convertObject(gitlabUserReqDTO, GitlabUserReqDTO.class));
        }
    }

    @Override
    public void syncAllUsers() {
        try {
            // 只需要一个实例进行处理就行了，并行处理可能导致gitlab不可用
            // 获取分布式锁
            Boolean ownLock = redisTemplate.opsForValue().setIfAbsent(USER_SYNC_REDIS_KEY, USER_SYNC_REDIS_KEY, LOCK_HOLD_MINUTES, TimeUnit.MINUTES);
            if (!Boolean.TRUE.equals(ownLock)) {
                return;
            }

            // 查询iam所有的用户的count
            long iamUserCount = baseServiceClientOperator.queryAllUserCount();
            long devopsUserCount = userAttrService.allUserCount();

            // 和devops-service的devops-user表的纪录进行对照
            // 如果数量不对，请求iam，查询所有的用户的id，
            if (iamUserCount <= devopsUserCount) {
                LOGGER.info("The iamUserCount {} is less than devopsUserCount {}, so skip syncing", iamUserCount, devopsUserCount);
            } else {
                LOGGER.info("The iamUserCount is {} and the devopsUserCount is {}", iamUserCount, devopsUserCount);
                Set<Long> devopsUsers = userAttrService.allUserIds();
                Set<Long> iamUsers = baseServiceClientOperator.queryAllUserIds();

                // 移除在devops中已有的数据
                iamUsers.removeAll(devopsUsers);
                LOGGER.info("There are {} users to be synced", iamUsers.size());

                // 在新事务中分批处理创建用户
                // 从未同步的用户，外层不开启事务，分批查找用户信息，对每一批用户开启一个新的事务进行同步，
                Iterator<Long> userIterator = iamUsers.iterator();
                // 已经处理的用户
                int processedSize = 0;
                int totalSize = iamUsers.size();
                // 设置进度
                redisTemplate.opsForValue().set(USER_SYNC_REDIS_KEY, processStringRepresentation(processedSize, totalSize), LOCK_HOLD_MINUTES, TimeUnit.MINUTES);
                while (userIterator.hasNext()) {
                    try {
                        ApplicationContextHelper.getContext().getBean(GitlabUserService.class).batchSyncUsersInNewTx(iamUsers.iterator(), USER_BATCH_SIZE, processedSize, totalSize);
                    } catch (Exception ex) {
                        LOGGER.info("User sync: ex occurred when calling batch method:", ex);
                    }
                    processedSize += USER_BATCH_SIZE;
                }
            }
        } finally {
            redisTemplate.delete(USER_SYNC_REDIS_KEY);
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void batchSyncUsersInNewTx(Iterator<Long> iamUserIds, int batchSize, int processedSize, int totalSize) {
        // 查询用户信息
        List<IamUserDTO> users = baseServiceClientOperator.listUsersByIds(readSomeElements(iamUserIds, batchSize), false);

        // 用于计数 前面请求连续失败（不包含邮箱导致的失败）的次数
        IntegerHolder consecutiveFailedCount = new IntegerHolder();
        consecutiveFailedCount.value = 0;
        int index = 0;
        for (IamUserDTO user : users) {
            index++;
            Objects.requireNonNull(user.getEnabled());
            Objects.requireNonNull(user.getAdmin());
            if (ANONYMOUS_USER_LOGIN_NAME.equals(user.getLoginName())) {
                // 跳过匿名用户
                return;
            }
            // 更新锁过期时间和进度
            redisTemplate.opsForValue().set(USER_SYNC_REDIS_KEY, processStringRepresentation(processedSize + index, totalSize), LOCK_HOLD_MINUTES, TimeUnit.MINUTES);
            LOGGER.info("Start to sync user {} with id {}", user.getLoginName(), user.getId());
            long start = System.currentTimeMillis();
            GitlabUserRequestVO gitlabUserReqDTO = new GitlabUserRequestVO();
            gitlabUserReqDTO.setProvider("oauth2_generic");
            gitlabUserReqDTO.setExternUid(user.getId().toString());
            gitlabUserReqDTO.setSkipConfirmation(true);
            gitlabUserReqDTO.setUsername(user.getLoginName());
            gitlabUserReqDTO.setEmail(Objects.requireNonNull(user.getEmail()));
            gitlabUserReqDTO.setName(user.getRealName());
            gitlabUserReqDTO.setCanCreateGroup(true);
            gitlabUserReqDTO.setProjectsLimit(100);
            try {
                // 创建用户
                createGitlabUser(gitlabUserReqDTO);
                UserAttrDTO userAttrDTO = userAttrService.baseQueryById(user.getId());
                // 如果用户是停用的，block gitlab 用户
                if (!user.getEnabled()) {
                    disEnabledGitlabUser(userAttrDTO);
                }
                // 如果用户是admin，为admin同步root权限
                if (user.getAdmin()) {
                    assignAdmin(userAttrDTO);
                }
                // 都成功则将计数清0
                consecutiveFailedCount.value = 0;
                if (LOGGER.isDebugEnabled()) {
                    // 输出成功调用同步这个用户的耗时
                    LOGGER.debug("User sync: {} ms used for user {}", System.currentTimeMillis() - start, user.getLoginName());
                }
            } catch (Exception ex) {
                handleExWhenSyncingUser(consecutiveFailedCount, ex, user);
            }
        }
    }

    private void handleExWhenSyncingUser(IntegerHolder consecutiveFailedCount, Exception ex, IamUserDTO user) {
        // 吞掉并打印异常
        // 获取异常
        String exTrace = LogUtil.readContentOfThrowable(ex);
        // 这个错误信息在gitlab版本改变后，可能会变
        if (exTrace.contains(GITLAB_USER_EMAIL_DUPLICATED_MESSAGE)) {
            LOGGER.warn("User sync: user with name {} and id {} failed due to duplicated email", user.getLoginName(), user.getId());
        } else {
            LOGGER.warn("Failed to sync user: {}, and the id is : {}", user.getLoginName(), user.getId());
            LOGGER.warn("The ex is : {}", exTrace);
            // 如果连续失败不是因为邮箱重复且超过一个阈值，认为某个服务不可用了，睡眠一会
            consecutiveFailedCount.value++;
            if (consecutiveFailedCount.value >= userFailureThreshold) {
                try {
                    LOGGER.info("User sync: After failed {} times, sleep {} seconds.", consecutiveFailedCount, failureSleepSeconds);
                    TimeUnit.SECONDS.sleep(failureSleepSeconds);
                    // 给两次机会
                    consecutiveFailedCount.value -= 2;
                } catch (InterruptedException e) {
                    LOGGER.info("InterruptedException: ", e);
                }
            }
        }
    }

    private static Long[] readSomeElements(Iterator<Long> it, int size) {
        int index = 0;
        Long[] userIds = new Long[size];
        while (it.hasNext() && index < size) {
            userIds[index] = it.next();
            index++;
        }
        // 如果迭代器中元素不够了，调整数组长度，去掉null元素
        if (index != size) {
            Long[] actualIds = new Long[index];
            System.arraycopy(userIds, 0, actualIds, 0, index);
            userIds = actualIds;
        }
        return userIds;
    }

    private static String processStringRepresentation(int current, int total) {
        return String.format(PROCESS_PATTERN, current, total);
    }

    @Override
    public void isEnabledGitlabUser(Long userId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);
        if (userAttrDTO != null) {
            gitlabServiceClientOperator.enableUser(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
    }

    @Override
    public void disEnabledGitlabUser(Long userId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);
        disEnabledGitlabUser(userAttrDTO);
    }

    @Override
    public void disEnabledGitlabUser(UserAttrDTO userAttrDTO) {
        if (userAttrDTO != null) {
            gitlabServiceClientOperator.disableUser(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        }
    }


    private void checkGitlabUser(GitlabUserRequestVO gitlabUserRequestVO) {
        String userName = gitlabUserRequestVO.getUsername();
        StringBuilder newUserName = new StringBuilder();
        for (int i = 0; i < userName.length(); i++) {
            if (!Pattern.matches(SERVICE_PATTERN, String.valueOf(userName.charAt(i)))) {
                newUserName.append("_");
            } else {
                newUserName.append(userName.charAt(i));
            }
        }
        gitlabUserRequestVO.setUsername(newUserName.toString());
    }

    @Override
    public Boolean doesEmailExists(String email) {
        return gitlabServiceClientOperator.checkEmail(email);
    }

    @Override
    public void assignAdmins(List<Long> iamUserIds) {
        if (CollectionUtils.isEmpty(iamUserIds)) {
            return;
        }

        iamUserIds.stream()
                .map(iamUserId -> userAttrService.checkUserSync(userAttrService.baseQueryById(iamUserId), iamUserId))
                .forEach(this::assignAdmin);
    }

    @Override
    public void assignAdmin(UserAttrDTO user) {
        gitlabServiceClientOperator.assignAdmin(user.getIamUserId(), TypeUtil.objToInteger(user.getGitlabUserId()));
        userAttrService.updateAdmin(user.getIamUserId(), Boolean.TRUE);
    }

    @Override
    public void deleteAdmin(Long iamUserId) {
        if (iamUserId == null) {
            return;
        }

        UserAttrDTO userAttrDTO = userAttrService.checkUserSync(userAttrService.baseQueryById(iamUserId), iamUserId);
        gitlabServiceClientOperator.deleteAdmin(iamUserId, TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        userAttrService.updateAdmin(iamUserId, Boolean.FALSE);
    }

    @Override
    public String resetGitlabPassword(Long userId) {
        // 校验这个用户是否是自己，目前只允许自己重置自己的gitlab密码
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (userDetails == null || !Objects.equals(Objects.requireNonNull(userId), userDetails.getUserId())) {
            throw new CommonException("error.reset.password.user.not.self");
        }

        // 校验用户是否同步
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);
        userAttrService.checkUserSync(userAttrDTO, userId);

        // 生成随机密码
        String randomPassword = GenerateUUID.generateRandomGitlabPassword();

        // 更新密码
        gitlabServiceClientOperator.updateUserPassword(TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()), randomPassword);

        // 返回密码
        return randomPassword;
    }
}
