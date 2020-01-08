package io.choerodon.devops.infra.constant;

/**
 * 消息通知的code
 *
 * @author zmf
 * @since 12/6/19
 */
public class NoticeCodeConstants {
    private NoticeCodeConstants() {
    }

    public static final String APP_SERVICE_CREATION_FAILED = "appServiceCreationFailure";
    public static final String APP_SERVICE_ENABLED = "enableAppService";
    public static final String APP_SERVICE_DISABLE = "disableAppService";
    public static final String AUDIT_MERGE_REQUEST = "auditMergeRequest";
    public static final String CERTIFICATION_CREATION_FAILURE = "certificationFailure";
    public static final String GITLAB_CONTINUOUS_DELIVERY_FAILURE = "gitLabContinuousDeliveryFailure";
    public static final String INGRESS_CREATION_FAILURE = "ingressFailure";
    public static final String INSTANCE_CREATION_FAILURE = "instanceFailure";
    public static final String MERGE_REQUEST_CLOSED = "mergeRequestClosed";
    public static final String MERGE_REQUEST_PASSED = "mergeRequestPassed";
    public static final String SERVICE_CREATION_FAILURE = "serviceFailure";
    public static final String RESOURC_EDELETE_CONFIRMATION = "resourceDeleteConfirmation";
    public static final String INVITE_USER = "inviteUser";
    public static final String DELETE_APP_SERVICE = "deleteAppService";
}
