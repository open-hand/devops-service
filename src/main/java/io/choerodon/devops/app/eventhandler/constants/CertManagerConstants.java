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
     * 创建chart的版本
     */
    public static final String CERT_MANAGER_CHART_VERSION = "0.1.0";

    /**
     * 使用的命名空间
     */
    public static final String CERT_MANAGER_NAME_SPACE = "kube-system";

    public static final String CERT_MANAGER_REALASE_NAME = "choerodon-cert-manager";
    public static final String CERT_MANAGER_REALASE_NAME_C7N = "choerodon";
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
