package io.choerodon.devops.infra.constant;

/**
 * 不好归类的常量
 *
 * @author zmf
 * @since 5/29/20
 */
public final class MiscConstants {
    private MiscConstants() {
    }

    public static final String DEFAULT_INTERNAL_APP_SERVICE_REPO_URL = "none";

    /**
     * 默认的chart配置的名称
     */
    public static final String DEFAULT_CHART_NAME = "chart_default";
    /**
     * 默认的docker配置的名称
     */
    public static final String DEFAULT_HARBOR_NAME = "harbor_default";

    public static final String DEFAULT_SONAR_NAME = "sonar_default";

    public static final String APP_SERVICE = "appService";

    public static final String ERROR_OPERATING_RESOURCE_IN_OTHER_PROJECT = "error.operating.resource.in.other.project";

    /**
     * devops_branch表的last_commit_message的最大长度限制
     */
    public static final int DEVOPS_BRANCH_LAST_COMMIT_MESSAGE_MAX_LENGTH = 510;

    /**
     * 查看jmeter脚本的指令模板, 参数是 $JMETER_HOME
     */
    public static final String LS_JMETER_COMMAND = "ls %s/bin/jmeter";

    /**
     * 设置同步用户的分布式锁时，锁的key
     */
    public static final String USER_SYNC_REDIS_KEY = "devops-service:user-sync-key";

    /**
     * 设置同步用户的分布式锁时，锁的key
     */
    public static final String APP_INSTANCE_DELETE_REDIS_KEY = "devops-service:appInstances:%s:delete";

    /**
     * 同步hzero部署状态的锁
     */
    public static final String HZERO_DEPLOY_STATUS_SYNC_REDIS_KEY = "devops-service:hzeroDeploy:sync";

    /**
     * 用户同步失败的文件存放的桶名称
     * https://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html
     */
    public static final String USER_SYNC_ERROR_FILE_BUCKET_NAME = "devops-service.user-sync.error-file";

    /**
     * 用户同步失败的文件存放的桶名称
     * https://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html
     */
    public static final String DEVOPS_SERVICE_BUCKET_NAME = "devops-service";

    public static final String UNKNOWN_SERVICE = "UnknownService";

    public static final String CREATE_TYPE = "create";
    public static final String UPDATE_TYPE = "update";
    public static final String DELETE_TYPE = "delete";
    public static final String WORKFLOW_ADMIN_NAME = "admin";
    public static final Long WORKFLOW_ADMIN_ID = 1L;
    public static final Long WORKFLOW_ADMIN_ORG_ID = 0L;


}
