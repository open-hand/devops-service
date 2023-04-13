package io.choerodon.devops.api.vo;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.devops.infra.dto.CiDockerAuthConfigDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineFunctionDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineVariableDTO;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ApiModel(value = "cicd流水线定义VO")
public class CiCdPipelineVO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("流水线名称")
    @NotEmpty(message = "{devops.pipeline.name.cannot.be.null}")
    private String name;
    @ApiModelProperty("项目id")
    private Long projectId;
    @Encrypt
    @ApiModelProperty("流水线关联应用服务id")
    @NotNull(message = "{devops.pipeline.appSvc.id.cannot.be.null}")
    private Long appServiceId;
    @ApiModelProperty("流水线关联应用服务名称/nullable")
    private String appServiceName;
    @ApiModelProperty("流水线关联应用服务类型")
    private String appServiceType;
    @ApiModelProperty("流水线关联应用服务编码")
    private String appServiceCode;
    @ApiModelProperty("gitlab仓库地址")
    private String gitlabUrl;
    @ApiModelProperty("gitlab项目id/nullable")
    private Long gitlabProjectId;
    @ApiModelProperty("runner镜像地址")
    private String image;
    @ApiModelProperty("自定义版本名称")
    private String versionName;
    @ApiModelProperty("是否启用/nullable")
    private Boolean enabled;
    @ApiModelProperty("最近执行时间/nullable")
    private Date latestExecuteDate;
    @ApiModelProperty("最近执行记录状态/nullable")
    private String latestExecuteStatus;
    @ApiModelProperty(name = "流水线是否含有执行记录的标志")
    private Boolean hasRecords = false;

    @ApiModelProperty("是否可中断")
    @Column(name = "is_interruptible")
    private Boolean interruptible;
    @ApiModelProperty(name = "是否拥有流水线编辑权限")
    private Boolean edit;
    @ApiModelProperty(name = "流程耗时")
    private Long time;
    @ApiModelProperty(name = "流水线是否启用定时任务")
    private Boolean enableSchedule;
    @ApiModelProperty(name = "乐观锁版本号")
    private Long objectVersionNumber;
    @ApiModelProperty(name = "最近更新时间")
    private Date lastUpdateDate;
    @ApiModelProperty(name = "创建时间")
    private Date creationDate;
    @ApiModelProperty(name = "创建者")
    private String createUserName;
    @ApiModelProperty(name = "创建者id")
    private Long createdBy;

    @ApiModelProperty(name = "ci阶段信息")
    @Valid
    private List<DevopsCiStageVO> devopsCiStageVOS;
    @ApiModelProperty(name = "流水线变量")
    private List<DevopsCiPipelineVariableDTO> devopsCiPipelineVariableDTOList;
    @ApiModelProperty(name = "cicd 流水线下的执行记录")
    private List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS;
    @ApiModelProperty(name = "流水线关联的分支")
    private Set<String> relatedBranches;
    @ApiModelProperty(name = "流水线中定义的函数")
    private List<DevopsCiPipelineFunctionDTO> devopsCiPipelineFunctionDTOList;
    @ApiModelProperty(name = "Docker认证配置")
    private List<CiDockerAuthConfigDTO> ciDockerAuthConfigDTOList;

    public String getGitlabUrl() {
        return gitlabUrl;
    }

    public void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl;
    }

    public Boolean getInterruptible() {
        return interruptible;
    }

    public void setInterruptible(Boolean interruptible) {
        this.interruptible = interruptible;
    }

    public Boolean getEnableSchedule() {
        return enableSchedule;
    }

    public void setEnableSchedule(Boolean enableSchedule) {
        this.enableSchedule = enableSchedule;
    }

    public List<CiDockerAuthConfigDTO> getCiDockerAuthConfigDTOList() {
        return ciDockerAuthConfigDTOList;
    }

    public void setCiDockerAuthConfigDTOList(List<CiDockerAuthConfigDTO> ciDockerAuthConfigDTOList) {
        this.ciDockerAuthConfigDTOList = ciDockerAuthConfigDTOList;
    }

    public List<DevopsCiPipelineVariableDTO> getDevopsCiPipelineVariableDTOList() {
        return devopsCiPipelineVariableDTOList;
    }

    public void setDevopsCiPipelineVariableDTOList(List<DevopsCiPipelineVariableDTO> devopsCiPipelineVariableDTOList) {
        this.devopsCiPipelineVariableDTOList = devopsCiPipelineVariableDTOList;
    }

    public List<DevopsCiPipelineFunctionDTO> getDevopsCiPipelineFunctionDTOList() {
        return devopsCiPipelineFunctionDTOList;
    }

    public void setDevopsCiPipelineFunctionDTOList(List<DevopsCiPipelineFunctionDTO> devopsCiPipelineFunctionDTOList) {
        this.devopsCiPipelineFunctionDTOList = devopsCiPipelineFunctionDTOList;
    }

    public Set<String> getRelatedBranches() {
        return relatedBranches;
    }

    public void setRelatedBranches(Set<String> relatedBranches) {
        this.relatedBranches = relatedBranches;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getAppServiceType() {
        return appServiceType;
    }

    public void setAppServiceType(String appServiceType) {
        this.appServiceType = appServiceType;
    }

    public List<CiCdPipelineRecordVO> getCiCdPipelineRecordVOS() {
        return ciCdPipelineRecordVOS;
    }

    public void setCiCdPipelineRecordVOS(List<CiCdPipelineRecordVO> ciCdPipelineRecordVOS) {
        this.ciCdPipelineRecordVOS = ciCdPipelineRecordVOS;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public String getAppServiceCode() {
        return appServiceCode;
    }

    public void setAppServiceCode(String appServiceCode) {
        this.appServiceCode = appServiceCode;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAppServiceId() {
        return appServiceId;
    }

    public void setAppServiceId(Long appServiceId) {
        this.appServiceId = appServiceId;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public void setAppServiceName(String appServiceName) {
        this.appServiceName = appServiceName;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getLatestExecuteDate() {
        return latestExecuteDate;
    }

    public void setLatestExecuteDate(Date latestExecuteDate) {
        this.latestExecuteDate = latestExecuteDate;
    }

    public String getLatestExecuteStatus() {
        return latestExecuteStatus;
    }

    public void setLatestExecuteStatus(String latestExecuteStatus) {
        this.latestExecuteStatus = latestExecuteStatus;
    }

    public List<DevopsCiStageVO> getDevopsCiStageVOS() {
        return devopsCiStageVOS;
    }

    public void setDevopsCiStageVOS(List<DevopsCiStageVO> devopsCiStageVOS) {
        this.devopsCiStageVOS = devopsCiStageVOS;
    }
    public Boolean getHasRecords() {
        return hasRecords;
    }

    public void setHasRecords(Boolean hasRecords) {
        this.hasRecords = hasRecords;
    }


    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

}
