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
}
