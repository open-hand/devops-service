package io.choerodon.devops.api.vo;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.api.vo.pipeline.*;
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
    @Encrypt
    private Long configId;

    @ApiModelProperty("启动延时,单位默认为分")
    private Integer startIn;

    private String tags;


    @Column(name = "is_enabled")
    private Boolean enabled;

    @ApiModelProperty("是否有权限修改cd的job,默认有")
    private boolean edit = true;

    @ApiModelProperty("任务为人工卡点时需要，保存人工卡点相关配置信息")
    private CiAuditConfigVO ciAuditConfig;
    @ApiModelProperty("任务为chart部署时需要，保存chart部署相关配置信息")
    private CiChartDeployConfigVO ciChartDeployConfig;
    @ApiModelProperty("任务为deploment部署时需要，保存deployment部署相关配置信息")
    private CiDeployDeployCfgVO ciDeployDeployCfg;

    @ApiModelProperty("任务为api_test类型，保存api测试相关配置信息")
    private DevopsCiApiTestInfoVO devopsCiApiTestInfoVO;

    @ApiModelProperty("任务为主机部署类型，保存主机部署相关配置信息")
    private DevopsCiHostDeployInfoVO devopsCiHostDeployInfoVO;

    @ApiModelProperty("触发其它流水线配置信息")
    private DevopsCiPipelineTriggerConfigVO devopsCiPipelineTriggerConfigVO;

    private List<ConfigFileRelVO> configFileRelList;

    public List<ConfigFileRelVO> getConfigFileRelList() {
        return configFileRelList;
    }

    public void setConfigFileRelList(List<ConfigFileRelVO> configFileRelList) {
        this.configFileRelList = configFileRelList;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public DevopsCiPipelineTriggerConfigVO getDevopsCiPipelineTriggerConfigVO() {
        return devopsCiPipelineTriggerConfigVO;
    }

    public void setDevopsCiPipelineTriggerConfigVO(DevopsCiPipelineTriggerConfigVO devopsCiPipelineTriggerConfigVO) {
        this.devopsCiPipelineTriggerConfigVO = devopsCiPipelineTriggerConfigVO;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public CiDeployDeployCfgVO getCiDeployDeployCfg() {
        return ciDeployDeployCfg;
    }

    public void setCiDeployDeployCfg(CiDeployDeployCfgVO ciDeployDeployCfg) {
        this.ciDeployDeployCfg = ciDeployDeployCfg;
    }

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

    public DevopsCiHostDeployInfoVO getDevopsCiHostDeployInfoVO() {
        return devopsCiHostDeployInfoVO;
    }

    public void setDevopsCiHostDeployInfoVO(DevopsCiHostDeployInfoVO devopsCiHostDeployInfoVO) {
        this.devopsCiHostDeployInfoVO = devopsCiHostDeployInfoVO;
    }

    public DevopsCiApiTestInfoVO getDevopsCiApiTestInfoVO() {
        return devopsCiApiTestInfoVO;
    }

    public void setDevopsCiApiTestInfoVO(DevopsCiApiTestInfoVO devopsCiApiTestInfoVO) {
        this.devopsCiApiTestInfoVO = devopsCiApiTestInfoVO;
    }

    public Integer getStartIn() {
        return startIn;
    }

    public void setStartIn(Integer startIn) {
        this.startIn = startIn;
    }
}
