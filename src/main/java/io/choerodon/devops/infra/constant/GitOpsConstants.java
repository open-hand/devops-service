package io.choerodon.devops.infra.constant;

import java.io.File;
import java.util.regex.Pattern;

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

    public static final String CI_FILE_COMMIT_MESSAGE = "[ci skip] update .gitlab-ci.yml";

    /**
     * 操作
     */
    public static final String COMMIT = "commit";
    public static final String DELETE = "delete";
    public static final String CREATE = "create";
    public static final String TAG_PUSH = "tag_push";

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
    public static final String CHOERODON_BEFORE_SCRIPT = "http_status_code=`curl -o .auto_devops.sh -s -m 10 --connect-timeout 10 -w %{http_code} \"${CHOERODON_URL}/devops/ci?token=${Token}\"`\n" +
            "if [ \"$http_status_code\" != \"200\" ]; then\n" +
            "  cat ./.auto_devops.sh\n" +
            "  exit 1\n" +
            "fi\n" +
            "source ./.auto_devops.sh\n";

    /**
     * 默认的sonar命令
     */
    public static final String DEFAULT_SONAR_TEMPLATE = "mvn --batch-mode clean org.jacoco:jacoco-maven-plugin:prepare-agent verify sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN} -Dsonar.gitlab.project_id=$CI_PROJECT_PATH -Dsonar.gitlab.commit_sha=$CI_COMMIT_REF_NAME -Dsonar.gitlab.ref_name=$CI_COMMIT_REF_NAME -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dmaven.test.failure.ignore=true -DskipTests=%s";

    /**
     * 默认的sonar scanner命令
     */
    public static final String DEFAULT_SONAR_SCANNNER_TEMPLATE = "sonar-scanner -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN} -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.sourceEncoding=UTF-8 -Dsonar.sources=%s";

    /**
     * 使用Token认证的sonar命令
     * SonarUrl
     * Token
     */
    public static final String SONAR_TOKEN_TEMPLATE = "mvn --batch-mode clean org.jacoco:jacoco-maven-plugin:prepare-agent verify sonar:sonar -Dsonar.host.url=%s -Dsonar.login=%s -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dmaven.test.failure.ignore=true -DskipTests=%s";
    /**
     * 使用Token认证的sonar命令
     */
    public static final String SONAR_TOKEN_SONAR_SCANNNER_TEMPLATE = "sonar-scanner -Dsonar.host.url=%s -Dsonar.login=%s -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.sourceEncoding=UTF-8 -Dsonar.sources=%s -Dsonar.qualitygate.wait=%s";

    /**
     * 使用用户名密码认证的sonar命令
     * SonarUrl
     * SonarUsername sonar的用户名
     * SonarPassword
     */
    public static final String SONAR_USER_PSW_TEMPLATE = "mvn --batch-mode clean org.jacoco:jacoco-maven-plugin:prepare-agent verify sonar:sonar -Dsonar.host.url=%s -Dsonar.login=%s -Dsonar.password=%s -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${SONAR_GROUP_NAME}:${PROJECT_NAME} -Dmaven.test.failure.ignore=true -DskipTests=%s";
    /**
     * 使用用户名密码认证的sonar scanner命令
     * SonarUrl
     * SonarUsername sonar的用户名
     * SonarPassword
     */
    public static final String SONAR_USER_PSW_SONAR_SCANNNER_TEMPLATE = "sonar-scanner -Dsonar.host.url=%s -Dsonar.login=%s -Dsonar.password=%s -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${SONAR_GROUP_NAME}:${PROJECT_NAME} -Dsonar.sourceEncoding=UTF-8 -Dsonar.sources=%s -Dsonar.qualitygate.wait=%s";

    public static final String COMMA = ",";

    public static final String RELEASE = "release";

    public static final String SNAPSHOT = "snapshot";

    /**
     * 在ci流水线中用于共享文件的目录
     */
    public static final String CHOERODON_CI_CACHE_DIR = "choerodon-ci-cache";

    /**
     * gitlab ci的 cache key, commit相同的流水线会共享
     */
    public static final String GITLAB_CI_DEFAULT_CACHE_KEY = "${CI_COMMIT_SHA}";

    /**
     * http或者https的地址正则表达式
     */
    public static final Pattern HTTP_URL_PATTERN = Pattern.compile("^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$");

    /**
     * 获取流水线ci内容的url，有三个参数：
     * 网关地址
     * 项目id
     * 流水线token
     */
    public static final String CI_CONTENT_URL_TEMPLATE = "%s/devops/v1/projects/%s/ci_contents/pipelines/%s/content.yaml";

    /**
     * 镜像的地址正则
     * 如： registry.gitlab.com/gitlab-org/gitlab-docs:11.6
     */
    public static final Pattern IMAGE_REGISTRY = Pattern.compile("^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}(/.+)*:.+$");

    public static final String STAGE = "stage";

    /**
     * 分页插件的第一页的页码
     */
    public static final int FIRST_PAGE_INDEX = 0;

    /**
     * 不带.git后缀的仓库名模板
     * {带/的gitlab地址}{组织code}-{项目code}/{应用服务code}
     */
    public static final String REPO_URL_TEMPLATE_WITHOUT_SUFFIX = "%s%s-%s/%s";

    /**
     * 流水线异步数据更新的执行器的名称
     * 不用完整单词作为名称的原因是, 长了在日志中会被截取后半部分显示, 反而会更不完整
     */
    public static final String PIPELINE_EXECUTOR = "ci-p-executor";

    /**
     * 流水线异步数据更新的执行器的名称
     * 不用完整单词作为名称的原因是, 长了在日志中会被截取后半部分显示, 反而会更不完整
     */
    public static final String PIPELINE_EXEC_EXECUTOR = "pipeline-exec-executor";


    /**
     * DevOps的流水线的redis的key的模板, 用于控制后台刷新gitlab流水线数据的频率
     * 变量是 gitlabPipelineId
     */
    public static final String CI_PIPELINE_REDIS_KEY_TEMPLATE = "devops-service:ci-pipeline:%s";

    /**
     * 查询流水线时, 默认的流水线纪录大小
     */
    public static final int DEFAULT_PIPELINE_RECORD_SIZE = 5;

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final int THREE_MINUTE_MILLISECONDS = 3 * 60 * 1000;

    /**
     * 发布jar包的命令的变量锚点, 仓库名称
     */
    public static final String CHOERODON_MAVEN_REPO_ID = "${CHOERODON_MAVEN_REPOSITORY_ID}";
    /**
     * 发布jar包的命令的变量锚点, 仓库地址
     */
    public static final String CHOERODON_MAVEN_REPO_URL = "${CHOERODON_MAVEN_REPO_URL}";


    /**
     * ip的正则表达式
     */
    public static final String IP_PATTERN = "^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$";

    public static final Pattern IP_REG_PATTERN = Pattern.compile(IP_PATTERN);

    /**
     * 同步用户的的线程池名称
     */
    public static final String USER_SYNC_EXECUTOR = "user-executor";

    /**
     * gitlab默认的ci文件的位置
     */
    public static final String DEFAULT_CI_CONFIG_PATH = ".gitlab-ci.yml";

    /**
     * 更改默认仓库的ci文件为这个，避免导入应用时跑ci，导入完成后改回默认
     */
    public static final String TEMP_CI_CONFIG_PATH = "choerodon-ci.yaml";
}
