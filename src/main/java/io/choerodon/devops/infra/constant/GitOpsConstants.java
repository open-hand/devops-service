package io.choerodon.devops.infra.constant;

import java.io.File;

/**
 * 和GitOps有关的常量
 *
 * @author zmf
 * @since 10/28/19
 */
public class GitOpsConstants {
    private GitOpsConstants() {
    }

    /**
     * GitLab组的名称和path格式
     */
    public static final String GITLAB_GROUP_NAME_FORMAT = "%s-%s%s";
    public static final String APP_SERVICE_SUFFIX = "";
    public static final String ENV_GROUP_SUFFIX = "-gitops";
    /**
     * 集群环境库的组 ${orgCode}_${projectCode}-cluster_gitops
     * 这是集群环境库组的后缀
     */
    public static final String CLUSTER_ENV_GROUP_SUFFIX = "-cluster_gitops";

    /**
     * gitlab环境库的webhook url相对路径
     */
    public static final String GITOPS_WEBHOOK_RELATIVE_URL = "devops/webhook/git_ops";

    /**
     * choerodon系统配置库的项目名格式为: clusterCode-envCode
     */
    public static final String SYSTEM_ENV_GITLAB_PROJECT_CODE_FORMAT = "%s-%s";

    public static final String MASTER = "master";

    /**
     * local path to store env
     * gitops/${orgCode}/${proCode}/${clusterCode}/${envCode}/${envId}
     */
    public static final String LOCAL_ENV_PATH = "gitops" + File.separator + "%s" + File.separator + "%s" + File.separator + "%s" + File.separator + "%s" + File.separator + "%s";

    public static final String YAML_FILE_SUFFIX = ".yaml";

    /**
     * release文件对应的gitlab文件前缀
     */
    public static final String RELEASE_PREFIX = "release-";

    /**
     * service文件对应的gitlab文件前缀
     */
    public static final String SERVICE_PREFIX = "svc-";

    /**
     * ingress文件对应的gitlab文件前缀
     */
    public static final String INGRESS_PREFIX = "ing-";

    /**
     * 系统环境code
     */
    public static final String SYSTEM_NAMESPACE = "choerodon";

    /**
     * 0.20版本之前用于实现实例类型网络的注解键值
     */
    public static final String SERVICE_INSTANCE_ANNOTATION_KEY = "choerodon.io/network-service-instances";

    /**
     * 分支删除时的after字段会是这个值
     */
    public static final String NO_COMMIT_SHA = "0000000000000000000000000000000000000000";

    public static final String MASTER_REF = "refs/heads/master";

    public static final String BATCH_DEPLOYMENT_COMMIT_MESSAGE = "[ADD] batch deployment";

    public static final String GITLAB_CI_FILE_NAME = ".gitlab-ci.yml";

    public static final String CI_FILE_COMMIT_MESSAGE = "[UPD] update .gitlab-ci.yml";

    public static final String CI_IMAGE = "registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.9.1";

    /**
     * 换行符
     */
    public static final String NEW_LINE = System.getProperty("line.separator", "\n");

    /**
     * 匹配换行符的字符串
     */
    public static final String NEWLINE_REGEX = "\r\n|\n|\r";
    /**
     * 用于注释shell的字符
     */
    public static final String COMMENT_STRING = "#";

    /**
     * 猪齿鱼框架的应用服务跑CI前要执行的脚本
     */
    public static final String CHOERODON_BEFORE_SCRIPT = "http_status_code=`curl -o .auto_devops.sh -s -m 10 --connect-timeout 10 -w %{http_code} \"${CHOERODON_URL}/devops/ci?token=${Token}&type=microservice\"`\n" +
            "if [ \"$http_status_code\" != \"200\" ]; then\n" +
            "  cat ./.auto_devops.sh\n" +
            "  exit 1\n" +
            "fi\n" +
            "source ./.auto_devops.sh\n";
}
