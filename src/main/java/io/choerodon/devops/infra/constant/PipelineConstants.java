package io.choerodon.devops.infra.constant;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/14 21:27
 */
public class PipelineConstants {

    public static final String SONAR_NAME = "sonar_default";

    public static final String EXPORT_VAR_TPL = "export %s=%s";
    public static final String DEVOPS_CI_MAVEN_REPOSITORY_TYPE = "devops.ci.maven.repository.type";

    public static final String DEVOPS_UPDATE_CI_JOB_RECORD = "devops.update.ci.job.record";
    public static final String DEVOPS_CREATE_OR_UPDATE_GITLAB_CI = "devops.create.or.update.gitlab.ci";

    public static final String DEVOPS_SAVE_PIPELINE_BRANCH_REL = "devops.save.pipeline.branch.rel";
    public static final String DEVOPS_DOCKER_AUTH_CONFIG_INVALID = "devops.docker.auth.config.invalid";
    public static final String DEVOPS_PIPELINE_NOT_EXIST = "devops.pipeline.not.exist";
    public static final String DEVOPS_DELETE_GITLAB_CI_FILE = "devops.delete.gitlab-ci.file";


    private PipelineConstants() {

    }

    public static final Integer GITLAB_ADMIN_ID = 1;


    public static final Long DEFAULT_CI_PIPELINE_FUNCTION_ID = 0L;
}
