package io.choerodon.devops.infra.constant;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 11:14
 */
public class PipelineCheckConstant {
    private PipelineCheckConstant() {

    }

    //
    public static final String DEVOPS_CANCEL_AUDITING_PIPELINE = "devops.cancel.auditing.pipeline";
    public static final String DEVOPS_CI_PIPELINE_EXISTS_FOR_APP_SERVICE = "devops.ci.pipeline.exists.for.app.service";

    // pipeline template
    public static final String DEVOPS_PIPELINE_TEMPLATE_ID_IS_NULL = "devops.pipeline.template.id.is.null";

    // pipeline
    public static final String DEVOPS_PIPELINE_ID_IS_NULL = "devops.pipeline.id.is.null";
    public static final String DEVOPS_PIPELINE_VERSION_ID_IS_NULL = "devops.pipeline.version.id.is.null";
    public static final String DEVOPS_PIPELINE_ID_INVALID = "devops.pipeline.id.invalid";
    public static final String DEVOPS_GITLAB_PIPELINE_ID_IS_NULL = "devops.gitlab.pipeline.id.is.null";
    public static final String DEVOPS_PIPELINE_RECORD_ID_IS_NULL = "devops.pipeline.record.id.is.null";
    public static final String DEVOPS_PIPELINE_TRIGGER_CONFIG_ID_IS_NULL = "devops.pipeline.trigger.config.id.is.null";

    // stage
    public static final String DEVOPS_STAGE_RECORD_ID_IS_NULL = "devops.stage.record.id.is.null";
    public static final String DEVOPS_STAGE_ID_IS_NULL = "devops.stage.id.is.null";
    public static final String DEVOPS_STAGE_PIPELINE_ID_INVALID = "devops.stage.pipeline.id.invalid";
    public static final String DEVOPS_STAGE_STATUS_IS_NULL = "devops.stage.status.is.null";

    // job
    public static final String DEVOPS_JOB_RECORD_ID_IS_NULL = "devops.job.record.id.is.null";
    public static final String DEVOPS_JOB_ID_IS_NULL = "devops.job.id.is.null";
    public static final String DEVOPS_JOB_PIPELINE_ID_INVALID = "devops.job.pipeline.id.invalid";
    public static final String DEVOPS_GITLAB_JOB_ID_IS_NULL = "devops.gitlab.job.id.is.null";
    public static final String DEVOPS_JOB_NAME_IS_NULL = "devops.job.name.is.null";
    public static final String DEVOPS_JOB_STATUS_IS_NULL = "devops.job.status.is.null";
    public static final String DEVOPS_CD_HOST_JOB_UNION_CI_JOB = "devops.cd.host.job.union.ci.job";
    public static final String DEVOPS_CI_CD_TRIGGER_TYPE_INVALID = "devops.ci.cd.trigger.type.invalid";
    public static final String DEVOPS_UNSUPPORTED_JOB_TYPE = "devops.unsupported.job.type";
    public static final String DEVOPS_APP_EXIST_IN_OTHER_ENV = "devops.app.exist.in.other.env";
    public static final String DEVOPS_APP_NOT_EXIST = "devops.app.not.exist";

    // step
    public static final String DEVOPS_STEP_TYPE_IS_INVALID = "devops.step.type.is.invalid";
    public static final String DEVOPS_UNSUPPORTED_STEP_TYPE = "devops.unsupported.step.type";
    public static final String DEVOPS_STEP_ID_IS_NULL = "devops.step.id.is.null";
    public static final String DEVOPS_STEP_NOT_COMPLETE = "devops.step.not.complete";
}
