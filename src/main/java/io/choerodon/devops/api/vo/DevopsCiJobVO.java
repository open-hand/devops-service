package io.choerodon.devops.api.vo;

import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.pipeline.CiAuditConfigVO;
import io.choerodon.devops.api.vo.pipeline.CiChartDeployConfigVO;
import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;

/**
 * @author wanghao
 * @since 2020/4/2 17:00
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DevopsCiJobVO {
    @Encrypt
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("任务名称")
    @NotEmpty(message = "{devops.job.name.cannot.be.null}")
    private String name;

    @ApiModelProperty("runner镜像地址")
    private String image;

    @ApiModelProperty("分组类型")
    private String groupType;

    @ApiModelProperty("job的并发数")
    private Integer parallel;

    @Encrypt
    @ApiModelProperty("阶段id")
    private Long ciStageId;

    @Encrypt
    @ApiModelProperty("流水线id")
    private Long ciPipelineId;

    @ApiModelProperty("任务类型")
    @NotEmpty(message = "{devops.job.type.cannot.be.null}")
    private String type;

    @ApiModelProperty("触发类型对应的值")
    @NotEmpty(message = "{devops.job.trigger.type.cannot.be.null}")
    private String triggerValue;

    /**
     * {@link io.choerodon.devops.infra.enums.CiTriggerType}
     */
    @ApiModelProperty("触发类型")
    private String triggerType;

    /**
     * {@link CiConfigVO}
     */
    @ApiModelProperty("详细信息 / 如果是自定义任务, 这个字段是base64加密过的")
    @NotEmpty(message = "{devops.job.metadata.cannot.be.null}")
    private String metadata;

    @ApiModelProperty("是否上传共享目录的内容 / 默认为false")
    private Boolean toUpload;

    @ApiModelProperty("是否下载共享目录的内容 / 默认为false")
    private Boolean toDownload;

    @ApiModelProperty("任务中的步骤列表")
    private List<DevopsCiStepVO> devopsCiStepVOList;

    @ApiModelProperty("ci阶段的构建类型")
    private List<String> configJobTypes;

    @ApiModelProperty("脚本类型任务的自定义脚本")
    private String script;

    @ApiModelProperty("任务信息是否完整")
    private Boolean completed = true;

    private String stageName;
    @ApiModelProperty("任务配置id")
    private Long configId;

    @ApiModelProperty("任务为人工卡点时需要，保存人工卡点相关配置信息")
    private CiAuditConfigVO ciAuditConfig;
    @ApiModelProperty("任务为chart部署时需要，保存chart部署相关配置信息")
    private CiChartDeployConfigVO ciChartDeployConfig;

    public CiChartDeployConfigVO getCiChartDeployConfig() {
        return ciChartDeployConfig;
    }

    public void setCiChartDeployConfig(CiChartDeployConfigVO ciChartDeployConfig) {
        this.ciChartDeployConfig = ciChartDeployConfig;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public CiAuditConfigVO getCiAuditConfig() {
        return ciAuditConfig;
    }

    public void setCiAuditConfig(CiAuditConfigVO ciAuditConfig) {
        this.ciAuditConfig = ciAuditConfig;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    private CiTemplateJobGroupDTO ciTemplateJobGroupDTO;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public CiTemplateJobGroupDTO getCiTemplateJobGroupDTO() {
        return ciTemplateJobGroupDTO;
    }

    public void setCiTemplateJobGroupDTO(CiTemplateJobGroupDTO ciTemplateJobGroupDTO) {
        this.ciTemplateJobGroupDTO = ciTemplateJobGroupDTO;
    }

    public List<DevopsCiStepVO> getDevopsCiStepVOList() {
        return devopsCiStepVOList;
    }

    public void setDevopsCiStepVOList(List<DevopsCiStepVO> devopsCiStepVOList) {
        this.devopsCiStepVOList = devopsCiStepVOList;
    }

    public List<String> getConfigJobTypes() {
        return configJobTypes;
    }

    public void setConfigJobTypes(List<String> configJobTypes) {
        this.configJobTypes = configJobTypes;
    }

    private Long objectVersionNumber;

    @JsonIgnore
    @Transient
    @ApiModelProperty("类型为build的job的metadata转为json后的对象")
    private CiConfigVO configVO;

    public Integer getParallel() {
        return parallel;
    }

    public void setParallel(Integer parallel) {
        this.parallel = parallel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCiStageId() {
        return ciStageId;
    }

    public void setCiStageId(Long ciStageId) {
        this.ciStageId = ciStageId;
    }

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(String triggerValue) {
        this.triggerValue = triggerValue;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getToUpload() {
        return toUpload;
    }

    public void setToUpload(Boolean toUpload) {
        this.toUpload = toUpload;
    }

    public Boolean getToDownload() {
        return toDownload;
    }

    public void setToDownload(Boolean toDownload) {
        this.toDownload = toDownload;
    }

    public CiConfigVO getConfigVO() {
        return configVO;
    }

    public void setConfigVO(CiConfigVO configVO) {
        this.configVO = configVO;
    }
}
