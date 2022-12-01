package io.choerodon.devops.api.vo.cd;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author
 * @since 2022-11-24 16:12:50
 */
public class PipelineJobVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "所属流水线Id,devops_pipeline.id", required = true)
    @Encrypt
    private Long pipelineId;
    @ApiModelProperty(value = "所属版本Id,devops_pipeline_version.id", required = true)
    @Encrypt
    private Long versionId;
    @ApiModelProperty(value = "所属阶段Id,devops_pipeline_stage.id", required = true)
    @Encrypt
    private Long stageId;
    @ApiModelProperty(value = "名称", required = true)
    private String name;
    @ApiModelProperty(value = "任务类型", required = true)
    private String type;
    @ApiModelProperty(value = "关联任务配置Id")
    @Encrypt
    private Long configId;

    private PipelineChartDeployCfgVO chartDeployCfg;

    private PipelineAuditCfgVO auditConfig;

    public PipelineChartDeployCfgVO getChartDeployCfg() {
        return chartDeployCfg;
    }

    public void setChartDeployCfg(PipelineChartDeployCfgVO chartDeployCfg) {
        this.chartDeployCfg = chartDeployCfg;
    }

    public PipelineAuditCfgVO getAuditConfig() {
        return auditConfig;
    }

    public void setAuditConfig(PipelineAuditCfgVO auditConfig) {
        this.auditConfig = auditConfig;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }
}