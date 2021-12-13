package io.choerodon.devops.infra.constant;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/7/2 11:14
 */
public class PipelineCheckConstant {
    private PipelineCheckConstant(){

    }
    //
    public static final String ERROR_CANCEL_AUDITING_PIPELINE = "error.cancel.auditing.pipeline";

    // pipeline
    public static final String ERROR_PIPELINE_IS_NULL = "error.pipeline.id.is.null";
    public static final String ERROR_GITLAB_PIPELINE_ID_IS_NULL = "error.gitlab.pipeline.id.is.null";
    public static final String ERROR_PIPELINE_RECORD_ID_IS_NULL = "error.pipeline.record.id.is.null";

    // stage
    public static final String ERROR_STAGE_RECORD_ID_IS_NULL = "error.stage.record.id.is.null";
    public static final String ERROR_STAGE_ID_IS_NULL = "error.stage.id.is.null";
    public static final String ERROR_STAGE_STATUS_IS_NULL = "error.stage.status.is.null";

    // job
    public static final String ERROR_JOB_RECORD_ID_IS_NULL = "error.job.record.id.is.null";
    public static final String ERROR_JOB_ID_IS_NULL = "error.job.id.is.null";
    public static final String ERROR_JOB_STATUS_IS_NULL = "error.job.status.is.null";

    // step
    public static final String ERROR_STEP_TYPE_IS_INVALID = "error.step.type.is.invalid";
}
