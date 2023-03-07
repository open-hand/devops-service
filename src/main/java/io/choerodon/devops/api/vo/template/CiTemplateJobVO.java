package io.choerodon.devops.api.vo.template;

import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.CiTemplateJobGroupDTO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;

/**
 * @author hao.wang08@hand-china.com
 * @since 2021-12-01 17:12:44
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CiTemplateJobVO {

    @Encrypt
    private Long id;
    @ApiModelProperty(value = "任务名称", required = true)
    private String name;
    @ApiModelProperty(value = "任务分组id", required = true)
    @Encrypt
    private Long groupId;
    @ApiModelProperty(value = "层级", required = true)
    private String sourceType;
    @ApiModelProperty(value = "层级Id", required = true)
    private Long sourceId;
    @ApiModelProperty(value = "是否上传到共享目录", required = true)
    @NotNull
    private Boolean toUpload;

    @ApiModelProperty(value = "是否下载到共享目录", required = true)
    @NotNull
    private Boolean toDownload;
    @ApiModelProperty(value = "任务镜像", required = true)
    private String image;

    private String type;

    @ApiModelProperty("任务标签")
    private String tags;

    @ApiModelProperty(value = "是否预置，1:预置，0:自定义", required = true)
    private Boolean builtIn;

    @Encrypt
    private Long relateStageId;
    private CiTemplateJobGroupDTO ciTemplateJobGroupDTO;

    @ApiModelProperty("创建者信息")
    private IamUserDTO creator;

//    @ApiModelProperty(value = "任务模板下面的步骤模板")
//    private List<CiTemplateStepVO> ciTemplateStepVOS;

    @ApiModelProperty("任务中的步骤列表")
    private List<CiTemplateStepVO> devopsCiStepVOList;


    @ApiModelProperty("所属任务分组名称")
    private String groupName;

    @ApiModelProperty(value = "含有步骤数")
    private Long stepNumber;

    @ApiModelProperty("创建者Id")
    private Long createdBy;

    private Date creationDate;

    private String script;

    @ApiModelProperty("job的并发数")
    private Integer parallel;

    @ApiModelProperty("是否开启并发")
    private Boolean openParallel;

    @ApiModelProperty("关联的任务配置id")
    @Encrypt
    private Long configId;
    @ApiModelProperty("任务模板是否可见")
    private Boolean visibility;
    @ApiModelProperty("触发类型对应的值")
    private String triggerValue;
    /**
     * {@link io.choerodon.devops.infra.enums.CiTriggerType}
     */
    @ApiModelProperty("触发类型")
    private String triggerType;

    @ApiModelProperty("chart部署时候的配置")
    private CiTplChartDeployCfgVO ciChartDeployConfig;

    @ApiModelProperty("api测试任务信息")
    private CiTplApiTestInfoCfgVO devopsCiApiTestInfoVO;

    @ApiModelProperty("deployment部署配置")
    private CiTplDeployDeployCfgVO ciDeployDeployCfg;

    @ApiModelProperty("主机部署的配置")
    private CiTplHostDeployInfoCfgVO devopsCiHostDeployInfoVO;

    @ApiModelProperty("审核信息")
    private CiTplAuditVO ciAuditConfig;

    @ApiModelProperty("顺序")
    private Integer sequence;

    @ApiModelProperty("分组类型")
    private String groupType;

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Boolean getOpenParallel() {
        return openParallel;
    }

    public void setOpenParallel(Boolean openParallel) {
        this.openParallel = openParallel;
    }

    public Integer getParallel() {
        return parallel;
    }

    public void setParallel(Integer parallel) {
        this.parallel = parallel;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }


    public List<CiTemplateStepVO> getDevopsCiStepVOList() {
        return devopsCiStepVOList;
    }

    public void setDevopsCiStepVOList(List<CiTemplateStepVO> devopsCiStepVOList) {
        this.devopsCiStepVOList = devopsCiStepVOList;
    }

    public Long getRelateStageId() {
        return relateStageId;
    }

    public void setRelateStageId(Long relateStageId) {
        this.relateStageId = relateStageId;
    }

    public CiTemplateJobGroupDTO getCiTemplateJobGroupDTO() {
        return ciTemplateJobGroupDTO;
    }

    public void setCiTemplateJobGroupDTO(CiTemplateJobGroupDTO ciTemplateJobGroupDTO) {
        this.ciTemplateJobGroupDTO = ciTemplateJobGroupDTO;
    }

    public Boolean getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Boolean builtIn) {
        this.builtIn = builtIn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public IamUserDTO getCreator() {
        return creator;
    }

    public void setCreator(IamUserDTO creator) {
        this.creator = creator;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Long stepNumber) {
        this.stepNumber = stepNumber;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
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

    public CiTplChartDeployCfgVO getCiChartDeployConfig() {
        return ciChartDeployConfig;
    }

    public void setCiChartDeployConfig(CiTplChartDeployCfgVO ciChartDeployConfig) {
        this.ciChartDeployConfig = ciChartDeployConfig;
    }

    public CiTplApiTestInfoCfgVO getDevopsCiApiTestInfoVO() {
        return devopsCiApiTestInfoVO;
    }

    public void setDevopsCiApiTestInfoVO(CiTplApiTestInfoCfgVO devopsCiApiTestInfoVO) {
        this.devopsCiApiTestInfoVO = devopsCiApiTestInfoVO;
    }

    public CiTplDeployDeployCfgVO getCiDeployDeployCfg() {
        return ciDeployDeployCfg;
    }

    public void setCiDeployDeployCfg(CiTplDeployDeployCfgVO ciDeployDeployCfg) {
        this.ciDeployDeployCfg = ciDeployDeployCfg;
    }

    public CiTplHostDeployInfoCfgVO getDevopsCiHostDeployInfoVO() {
        return devopsCiHostDeployInfoVO;
    }

    public void setDevopsCiHostDeployInfoVO(CiTplHostDeployInfoCfgVO devopsCiHostDeployInfoVO) {
        this.devopsCiHostDeployInfoVO = devopsCiHostDeployInfoVO;
    }

    public CiTplAuditVO getCiAuditConfig() {
        return ciAuditConfig;
    }

    public void setCiAuditConfig(CiTplAuditVO ciAuditConfig) {
        this.ciAuditConfig = ciAuditConfig;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }
}
