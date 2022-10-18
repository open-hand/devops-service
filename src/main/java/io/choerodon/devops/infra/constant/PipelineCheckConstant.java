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

    // pipeline template
    public static final String DEVOPS_PIPELINE_TEMPLATE_ID_IS_NULL = "devops.pipeline.template.id.is.null";

    // pipeline
    public static final String DEVOPS_PIPELINE_ID_IS_NULL = "devops.pipeline.id.is.null";
    public static final String DEVOPS_GITLAB_PIPELINE_ID_IS_NULL = "devops.gitlab.pipeline.id.is.null";
    public static final String DEVOPS_PIPELINE_RECORD_ID_IS_NULL = "devops.pipeline.record.id.is.null";

    // stage
    public static final String DEVOPS_STAGE_RECORD_ID_IS_NULL = "devops.stage.record.id.is.null";
    public static final String DEVOPS_STAGE_ID_IS_NULL = "devops.stage.id.is.null";
    public static final String DEVOPS_STAGE_STATUS_IS_NULL = "devops.stage.status.is.null";

    // job
    public static final String DEVOPS_JOB_RECORD_ID_IS_NULL = "devops.job.record.id.is.null";
    public static final String DEVOPS_JOB_ID_IS_NULL = "devops.job.id.is.null";
    public static final String DEVOPS_JOB_NAME_IS_NULL = "devops.job.name.is.null";
    public static final String DEVOPS_JOB_STATUS_IS_NULL = "devops.job.status.is.null";

    // step
    public static final String DEVOPS_STEP_TYPE_IS_INVALID = "devops.step.type.is.invalid";
}
