package io.choerodon.devops.app.eventhandler.constants;

/**
 * 此类放CertManager常量
 *
 * @author zhaotianxin
 * @since 2019/11/1
 */
public class CertManagerConstants {
    private CertManagerConstants() {
    }

    /**
     * 旧的CertManager的chart的版本, 不再安装这个版本
     */
    public static final String OLD_V1_CERT_MANAGER_CHART_VERSION = "0.1.0";
    /**
     * 新的certManager的chart版本
     */
    public static final String NEW_V1_CERT_MANAGER_CHART_VERSION = "1.1.1";
    /**
     * 第一个版本的 cert-manager 的名称
     */
    @Deprecated
    public static final String V1_CERT_MANAGER_RELEASE_NAME = "choerodon-cert-manager";
    @Deprecated
    public static final String V1_CERT_MANAGER_NAMESPACE = "choerodon";
    /**
     * 证书管理的标志
     */
    public static final String CERT_MANAGER_STATUS = "cert_manager_status";

    /**
     * 证书管理安装成功的标志
     */
    public static final String RUNNING = "running";

    /**
     * 证书管理卸载成功的标志
     */
    public static final String DELETED = "deleted";

    public static final String RELEASE_NAME = "releaseName";

    public static final String NAMESPACE = "namespace";
}
