package io.choerodon.devops.infra.constant;

/**
 * 消息通知的code
 *
 * @author zmf
 * @since 12/6/19
 */
public class MessageCodeConstants {
    private MessageCodeConstants() {
    }

    /**
     * 创建应用服务失败
     */
    public static final String APP_SERVICE_CREATION_FAILED = "APPSERVICECREATIONFAILURE";
    /**
     * 启用应用服务
     */
    public static final String APP_SERVICE_ENABLED = "ENABLEAPPSERVICE";
    /**
     * 应用服务停用
     */
    public static final String APP_SERVICE_DISABLE = "DISABLEAPPSERVICE";
    /**
     * 合并请求审核通知
     */
    public static final String AUDIT_MERGE_REQUEST = "AUDITMERGEREQUEST";
    /**
     * 证书创建失败
     */
    public static final String CERTIFICATION_CREATION_FAILURE = "CERTIFICATIONFAILURE";
    public static final String GITLAB_CONTINUOUS_DELIVERY_FAILURE = "GITLABCONTINUOUSDELIVERYFAILURE";
    /**
     * 创建域名失败
     */
    public static final String INGRESS_CREATION_FAILURE = "INGRESSFAILURE";
    public static final String INSTANCE_CREATION_FAILURE = "INSTANCEFAILURE";
    /**
     * 合并请求被关闭
     */
    public static final String MERGE_REQUEST_CLOSED = "MERGEREQUESTCLOSED";
    /**
     * 合格请求通过
     */
    public static final String MERGE_REQUEST_PASSED = "MERGEREQUESTPASSED";
    /**
     * 网络创建失败
     */
    public static final String SERVICE_CREATION_FAILURE = "SERVICEFAILURE";
    public static final String RESOURCE_DELETE_CONFIRMATION = "RESOURCEDELETECONFIRMATION";
    public static final String GITLAB_PSW = "GITLABPASSWORD";
    public static final String INVITE_USER = "INVITEUSER";
    /**
     * 删除应用服务
     */
    public static final String DELETE_APP_SERVICE = "DELETEAPPSERVICE";
    /**
     * 流水线执行失败
     */
    public static final String PIPELINE_FAILED = "PIPELINEFAILED";
    /**
     * 流水线执行成功
     */
    public static final String PIPELINE_SUCCESS = "PIPELINESUCCESS";
    /**
     * 流水线审核通知
     */
    public static final String PIPELINE_AUDIT = "PIPELINEAUDIT";
    /**
     * 流水线被终止通知
     */
    public static final String PIPELINE_STOP = "PIPELINESTOP";
    /**
     * 流水线或签任务通过通知
     */
    public static final String PIPELINE_PASS = "PIPELINEPASS";
    public static final String CREATE_INSTANCE_SUCCESS = "CREATEINSTANCESUCCESS";
    /**
     *  新建实例失败
     */
    public static final String CREATE_INSTANCE_FAIL = "CREATEINSTANCEFAIL";
    public static final String UPDATE_INSTANCE_SUCCESS = "UPDATEINSTANCESUCCESS";
    public static final String UPDATE_INSTANCE_FAIL = "UPDATEINSTANCEFAIL";
    public static final String ENABLE_INSTANCE = "ENABLEINSTANCE";
    public static final String STOP_INSTANCE = "STOPINSTANCE";
    public static final String PIPELINE_API_TEST_WARNING = "PIPELINE_API_TEST_WARNING";
    public static final String PIPELINE_API_SUITE_WARNING = "PIPELINE_API_SUITE_WARNING";


}
